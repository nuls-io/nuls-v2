package io.nuls.tools.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionUtils {
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 获取距初始容量
     * @param cap 初始容量
     * @return 按规则转换后的容量
     * */
    public static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * 创建一个指定容量的HashMap
     * @param cap 容量
     * @return 生成的HashMap
     * */
    public static HashMap createHashMap(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return new HashMap<>(capacity);
    }

    /**
     * 创建一个指定容量的LinkedHashMap
     * @param cap 容量
     * @return 生成的LinkedHashMap
     * */
    public static LinkedHashMap createLinkedHashMap(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return new LinkedHashMap<>(capacity);
    }

    /**
     * 创建一个指定容量的ConcurrentHashMap
     * @param cap 容量
     * @return 生成的ConcurrentHashMap
     * */
    public static ConcurrentHashMap createConcurrentHashMap(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return new ConcurrentHashMap<>(capacity);
    }

    /**
     * 创建一个指定容量的HashSet
     * @param cap 容量
     * @return 生成的HashSet
     * */
    public static HashSet createHashSet(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return new HashSet<>(capacity);
    }

    /**
     * 创建一个指定容量的ConcurrentHashMap的keySet
     * @param cap 容量
     * @return 生成的HashSet
     * */
    public static Set createConcurrentHashSet(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return ConcurrentHashMap.newKeySet(capacity);
    }

    /**
     * 将对象集合转为String
     * @param list 对象集合
     * @return     集合里的对象toString拼接后得到的字符串
     * */
    public static String join(List<? extends Object> list) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Object object : list) {
            if (sb.length() != 0) {
                sb.append(" ");
            }
            sb.append(object.toString());
        }
        return sb.toString();
    }

    /**
     * 获取固定大小的map
     *
     * @param size  map元素上限
     * @return Map  map对象
     */
    public static <K, V> Map<K, V> getSizedMap(int size) {
        return Collections.synchronizedMap(new LinkedHashMap<>(size) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > size;
            }
        });
    }
}
