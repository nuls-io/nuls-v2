package io.nuls.core.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collection tool class
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/22 13:08
 */
public class CollectionUtils {
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Obtain distance from initial capacity
     * @param cap Initial capacity
     * @return Capacity converted according to rules
     * */
    private static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * Create a specified capacityHashMap
     * @param cap capacity
     * @return GeneratedHashMap
     * */
    public static HashMap createHashMap(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return new HashMap<>(capacity);
    }

    /**
     * Create a specified capacityLinkedHashMap
     * @param cap capacity
     * @return GeneratedLinkedHashMap
     * */
    public static LinkedHashMap createLinkedHashMap(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return new LinkedHashMap<>(capacity);
    }

    /**
     * Create a specified capacityConcurrentHashMap
     * @param cap capacity
     * @return GeneratedConcurrentHashMap
     * */
    public static ConcurrentHashMap createConcurrentHashMap(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return new ConcurrentHashMap<>(capacity);
    }

    /**
     * Create a specified capacityHashSet
     * @param cap capacity
     * @return GeneratedHashSet
     * */
    public static HashSet createHashSet(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return new HashSet<>(capacity);
    }

    /**
     * Create a specified capacityConcurrentHashMapofkeySet
     * @param cap capacity
     * @return GeneratedHashSet
     * */
    public static Set createConcurrentHashSet(int cap) {
        int capacity = tableSizeFor(cap) << 1;
        return ConcurrentHashMap.newKeySet(capacity);
    }

    /**
     * Convert object collection toString
     * @param list Object Collection
     * @return     Objects in the collectiontoStringThe concatenated string
     * */
    public static String join(List<?> list) {
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
     * Get thread safe fixed sizemap
     *
     * @param size  mapElement upper limit
     * @return Map  mapobject
     */
    public static <K, V> Map<K, V> getSynSizedMap(int size) {
        return Collections.synchronizedMap(new LinkedHashMap<>(size) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > size;
            }
        });
    }

    /**
     * Get thread safe fixed sizeset
     *
     * @param size  setElement upper limit
     * @return Set  setobject
     */
    public static <T> Set<T> getSynSizedSet(int size) {
        return Collections.synchronizedSet(new TreeSet<>(){
            @Override
            public boolean add(T t) {
                if (size() >= size) {
                    pollLast();
                }
                return super.add(t);
            }
        });
    }

    /**
     * Get thread safe fixed sizelist
     *
     * @return List  listobject
     */
    public static <T> List<T> getSynList() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * thanList.removeAllefficient
     *
     * @param source
     * @param destination
     * @param <T>
     * @return
     */
    public static <T> List<T> removeAll(List<T> source, List<T> destination) {
        List<T> result = new LinkedList<>();
        Set<T> destinationSet = new HashSet<>(destination);
        for (T t : source) {
            if (!destinationSet.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }
}
