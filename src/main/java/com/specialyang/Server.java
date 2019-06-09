package com.specialyang;

import com.specialyang.util.ByteUtil;
import com.specialyang.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by SpecialYang on 2019/6/8 22:23.
 */
public class Server implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private Thread thread = new Thread(this);

    private volatile boolean live = true;

    public void start() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(9000));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (live && !Thread.interrupted()) {
                if (selector.select(1000) == 0) {
                    continue;
                }
                Set<SelectionKey> set = selector.selectedKeys();
                Iterator<SelectionKey> iterator = set.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isValid() && key.isAcceptable()) {
                        this.onAcceptable(key);
                    }
                    if (key.isValid() && key.isReadable()) {
                        this.onReadable(key);
                    }
                    if (key.isValid() && key.isWritable()) {
                        this.onWritable(key);
                    }
                 }
            }
        } catch (IOException e) {
            log.error("Error on socket I/O", e);
        }
    }

    public void onAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = null;
        try {
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            // 为新连接关联一个缓冲区
            socketChannel.register(key.selector(), SelectionKey.OP_READ, new Message());
        } catch (IOException e) {
            log.error("Error on accept connection.", e);
            StreamUtil.close(socketChannel);
            throw e;
        }
    }

    public void onReadable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        // 获得与该通道关联的头部数据 (此处说的是要发送的文件长度)
        Message message = (Message) key.attachment();
        FileChannel fileChannel = null;
        try {
            int rCount = 0;
            if (message.getTotalCount() == -1) {
                ByteBuffer buf = ByteBuffer.allocate(8);
                // read为非阻塞，利用hasRemaining确保读满缓冲数组
                while (buf.hasRemaining() && (rCount = socketChannel.read(buf)) > 0);
                message.setTotalCount(ByteUtil.byteBuffer2Long(buf));
            }
            InetSocketAddress isa = (InetSocketAddress) socketChannel.getRemoteAddress();
            String fileName = String.format("/Users/specialyang/Movies/test1-%d",
                    isa.getPort());
            // 因为是非阻塞模式，所以要采用追加的形式，否则下一次select周期会覆盖内容
            fileChannel = new FileOutputStream(new File(fileName), true).getChannel();
            long totalCount = message.getTotalCount();
            ByteBuffer buf = ByteBuffer.allocate(10240);
            buf.clear();
            // 即使内核中socket的读缓冲区有数据，但是这里可能也什么都读不到，从而返回0
            while ((rCount = socketChannel.read(buf)) > 0) {
                log.info("Received {} bytes from {}", rCount, isa);
                totalCount -= rCount;
                buf.flip();
                rCount = fileChannel.write(buf);
                log.info("Write {} bytes into file {}", rCount, fileName);
                buf.clear();
            }
            message.setTotalCount(totalCount);
            // 数据确实读完了，注册可写事件
            if (totalCount == 0) {
                socketChannel.register(key.selector(), SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            log.error("Error on read socket.", e);
            StreamUtil.close(socketChannel);
            throw e;
        } finally {
            StreamUtil.close(fileChannel);
        }
    }

    public void onWritable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            byte[] bytes = "ok".getBytes("UTF-8");
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            int rCount = 0;
            log.info("buf position {} limit{}", buf.position(), buf.limit());
            // 非阻塞模式下，可能只写入部分字节数或者0字节，所以要调用hasRemaining来确保全部写进入
            while (buf.hasRemaining() && (rCount = socketChannel.write(buf)) > 0) {
                log.info("Write {} bytes to {}", rCount, socketChannel.getRemoteAddress());
            }
        } catch (IOException e) {
            log.error("Error on write socket", e);
            throw e;
        } finally {
            StreamUtil.close(socketChannel);
            log.info("socket is closed");
        }
    }

    public void close() {
        live = false;
        try {
            // 等待服务器线程结束
            thread.join();
        } catch (InterruptedException e) {
            log.error("Be interrupted on join.", e);
        } finally {
            StreamUtil.close(serverSocketChannel);
            StreamUtil.close(selector);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start();
            Scanner input = new Scanner(System.in);
            while (input.hasNext()) {
                String content = input.nextLine();
                if (content.equalsIgnoreCase("exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Error on start server.", e);
        } finally {
            server.close();
        }
        log.info("done.");
    }
}
