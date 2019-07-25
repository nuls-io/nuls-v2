package io.nuls.network.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试调试使用
 */
public class MessageTestUtil {
    public static Map<String, AtomicLong> sendMsgCountMap = new HashMap<>();
    public static Map<String, AtomicLong> recMsgCountMap = new HashMap<>();
    public static Map<String, Integer> lowerLeverCmd = new HashMap<>();

    static {
        lowerLeverCmd.put("newHash", 1);
        lowerLeverCmd.put("askTx", 1);
        lowerLeverCmd.put("receiveTx", 1);
    }

    public static boolean isLowerLeverCmd(String cmd) {
        return (lowerLeverCmd.get(cmd) != null);
    }

    public static void sendMessage(String cmd) {
        AtomicLong count = sendMsgCountMap.computeIfAbsent(cmd, k -> new AtomicLong(0));
        count.addAndGet(1);
    }

    public static void recievedMessage(String cmd) {
        AtomicLong count = recMsgCountMap.computeIfAbsent(cmd, k -> new AtomicLong(0));
        count.addAndGet(1);
    }
}
