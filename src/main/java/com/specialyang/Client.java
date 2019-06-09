package com.specialyang;

import com.specialyang.util.ByteUtil;
import com.specialyang.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by SpecialYang on 2019/6/7 12:37.
 */
public class Client {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        SocketChannel socketChannel = null;
        FileChannel fileChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            // 阻塞模式，则阻塞等待连接完成或者抛出异常
            socketChannel.connect(new InetSocketAddress("localhost", 9000));
            if (!socketChannel.finishConnect()) {
                log.error("Can not connect to server.");
            }
            String fileName = "/Users/specialyang/Movies/test1";
            FileInputStream fileInputStream = new FileInputStream(new File(fileName));
            long totalCount = fileInputStream.available();
            // begin send file size to server.
            log.info("Send file {}, size {}", fileName, totalCount);
            byte[] b = ByteUtil.long2ByteArray(totalCount);
            ByteBuffer buf = ByteBuffer.wrap(b);
            socketChannel.write(buf);
            // end send file size to server.
            buf = ByteBuffer.allocate(10240);
            fileChannel = fileInputStream.getChannel();
            int rCount = 0;
            while ((rCount = fileChannel.read(buf)) > 0) {
                log.info("Read {} bytes from file", rCount);
                buf.flip();
                while (buf.hasRemaining() && (rCount = socketChannel.write(buf)) > 0) {
                    log.info("Write {} bytes to server", rCount);
                }
                buf.clear();
            }
            while ((rCount = socketChannel.read(buf)) > 0) {
                log.info("Read {} bytes from server.", rCount);
            }
            buf.flip();
            log.info(Charset.forName("utf-8").decode(buf).toString());
        } catch (IOException e) {
            log.error("Error on send file", e);
        } finally {
            StreamUtil.close(fileChannel);
            StreamUtil.close(socketChannel);
        }
    }
}
