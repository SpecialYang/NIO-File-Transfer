package com.specialyang.util;

import java.nio.ByteBuffer;

/**
 * Created by SpecialYang on 2019/6/6 22:59.
 */
public class ByteUtil {

    public static byte[] int2ByteArray(int num) {
        return new byte[] {
                (byte)((num >> 24) & 0xFF),
                (byte)((num >> 16) & 0xFF),
                (byte)((num >> 8 ) & 0xFF),
                (byte)((num      ) & 0xFF)
        };
    }

    public static byte[] long2ByteArray(long num) {
        return new byte[] {
                (byte)((num >> 56) & 0xFF),
                (byte)((num >> 48) & 0xFF),
                (byte)((num >> 40) & 0xFF),
                (byte)((num >> 32) & 0xFF),
                (byte)((num >> 24) & 0xFF),
                (byte)((num >> 16) & 0xFF),
                (byte)((num >> 8 ) & 0xFF),
                (byte)((num      ) & 0xFF)
        };
    }

    public static int byteArray2Int(byte[] b) {
        int num = 0;
        return b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }

    public static long byteArray2Long(byte[] b) {
        long num = 0;
        for (int i = 0; i < 8; i++) {
            num = (num << 8) | (b[i] & 0xFF);
        }
        return num;
    }

    public static int byteBuffer2Int(ByteBuffer buf) {
        return byteBuffer2Int(buf, 0);
    }

    public static int byteBuffer2Int(ByteBuffer buf, int offset) {
        int result = 0;
        for (int i = offset; i < offset + 4; i++) {
            result = (result << 8) | (buf.get(i) & 0xFF);
        }
        return result;
    }

    public static long byteBuffer2Long(ByteBuffer buf) {
        return byteBuffer2Long(buf, 0);
    }

    public static long byteBuffer2Long(ByteBuffer buf, int offset) {
        long result = 0;
        for (int i = offset; i < offset + 8; i++) {
            result = (result << 8) | (buf.get(i) & 0xFF);
        }
        return result;
    }

    public static void main(String[] args) {
        int num = 2422424;
        byte[] b = int2ByteArray(num);
        System.out.println(byteArray2Int(b));

        ByteBuffer buf = ByteBuffer.wrap(b);
        System.out.println(byteBuffer2Int(buf));

        long a = 1527103488;
        byte[] aB = long2ByteArray(a);
        System.out.println(byteArray2Long(aB));

        buf = ByteBuffer.wrap(aB);
        System.out.println(byteBuffer2Long(buf));
    }
}
