package io.nuls.tools.thread.commom;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author  tag
 * */
public class ThreadCache {
    private static ThreadCache INSTANCE = null;

    /**
     * key  :  poolName
     * value:  pool
     */
    private final Map<String, ThreadPoolExecutor> POOL_EXECUTOR_MAP = new HashMap<>();

    /**
     * key  :  threadName
     * value:  thread
     */
    private final Map<String, Thread> THREAD_MAP = new HashMap<>();

    /**
     * key  :  poolName
     * value:  Set<threadName>
     */
    private final Map<String, Set<String>> POOL_THREAD_MAP = new HashMap<>();

    private ThreadCache() { }

    public static final ThreadCache getInstance() {
        if(INSTANCE == null){
            synchronized (ThreadCache.class){
                if(INSTANCE == null){
                    INSTANCE = new ThreadCache();
                }
            }
        }
        return INSTANCE;
    }

    public final void putPool( String poolName, ThreadPoolExecutor pool) {
        POOL_EXECUTOR_MAP.put(poolName, pool);
    }

    public final void putThread(String poolName, String threadName, Thread thread) {
        THREAD_MAP.put(threadName, thread);
        Set<String> set = POOL_THREAD_MAP.get(poolName);
        if (null == set) {
            set = new HashSet<>();
        }
        set.add(threadName);
        POOL_THREAD_MAP.put(poolName, set);
    }

    public final Thread getThread(String threadName) {
        return THREAD_MAP.get(threadName);
    }

    public final ThreadPoolExecutor getPool(String poolName) {
        return POOL_EXECUTOR_MAP.get(poolName);
    }

    public final List<Thread> getThreadList() {
        return new ArrayList<Thread>(THREAD_MAP.values());
    }

    public final List<Thread> getThreadList(String poolName) {
        Set<String> set = POOL_THREAD_MAP.get(poolName);
        if (null == set) {
            return null;
        }
        List<Thread> list = new ArrayList<>();
        for (String threadName : set) {
            list.add(THREAD_MAP.get(threadName));
        }
        return list;
    }

    public final List<ThreadPoolExecutor> getPoolList() {
        return new ArrayList<ThreadPoolExecutor>(POOL_EXECUTOR_MAP.values());
    }

    public void removeAllThread() {
        THREAD_MAP.clear();
        POOL_EXECUTOR_MAP.clear();
        POOL_THREAD_MAP.clear();
    }

    public void removePoolThread(String poolName) {
        POOL_THREAD_MAP.remove(poolName);
    }

    public void removeThread(String threadName) {
        THREAD_MAP.remove(threadName);
        for (Set<String> tset : POOL_THREAD_MAP.values()) {
            tset.remove(threadName);
        }
    }
}
