package io.nuls.network.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MessageTestUtil {
    public static Map<String, AtomicLong> sendMsgCountMap = new HashMap<>();
    public static Map<String, AtomicLong> recMsgCountMap = new HashMap<>();


    public static void sendMessage(String cmd) {
        AtomicLong count = sendMsgCountMap.computeIfAbsent(cmd, k -> new AtomicLong(0));
        count.addAndGet(1);
    }

    public static void recievedMessage(String cmd) {
        AtomicLong count = recMsgCountMap.computeIfAbsent(cmd, k -> new AtomicLong(0));
        count.addAndGet(1);
    }
}
