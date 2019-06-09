package com.specialyang.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by SpecialYang on 2019/6/4 23:59.
 */
public class StreamUtil {

    private static final Logger log = LoggerFactory.getLogger(StreamUtil.class);

    public static void close(Closeable p) {
        if (p == null) {
            return;
        }
        try {
            p.close();
        } catch (IOException e) {
            log.error("Error on close {}", p.getClass().getName(), e);
        }
    }
}
