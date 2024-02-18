package io.nuls.core.thread;

import io.nuls.core.log.Log;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.core.thread.commom.ThreadCache;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author tag
 */
public class ThreadUtils {
    private static final ThreadCache THREAD_DATA_CACHE = ThreadCache.getInstance();
    private static final String TEMPORARY_THREAD_POOL_NAME = "temporary";
    private static final int TEMPORARY_THREAD_POOL_COUNT = 4;
    private static final int TEMPORARY_THREAD_POOL_QUEUE_SIZE = 1000;
    private static final ThreadPoolExecutor TEMPORARY_THREAD_POOL;

    /**
     * Initializing a temporary thread pool
     */
    static {
        TEMPORARY_THREAD_POOL = createThreadPool(TEMPORARY_THREAD_POOL_COUNT, TEMPORARY_THREAD_POOL_QUEUE_SIZE, new NulsThreadFactory(TEMPORARY_THREAD_POOL_NAME));
    }

    /**
     * Add a thread to the thread cache
     *
     * @param poolName   Thread Factory Name
     * @param threadName Thread Name
     * @param newThread  thread
     */
    public static final void putThread(String poolName, String threadName, Thread newThread) {
        THREAD_DATA_CACHE.putThread(poolName, threadName, newThread);
    }

    /**
     * Create thread pool
     *
     * @param threadCount The number of core threads in the thread pool
     * @param queueSize   The buffer queue length used by the thread pool
     * @param factory     Factory used by thread pool to create threads
     * @return ThreadPoolExecutor Thread Pool Object
     */
    public static final ThreadPoolExecutor createThreadPool(int threadCount, int queueSize, NulsThreadFactory factory) {
        if (threadCount == 0) {
            throw new RuntimeException("thread count cannot be 0!");
        }
        if (factory == null) {
            throw new RuntimeException("thread factory cannot be null!");
        }
        LinkedBlockingQueue<Runnable> queue;
        if (queueSize > 0) {
            queue = new LinkedBlockingQueue<>(queueSize);
        } else {
            queue = new LinkedBlockingQueue<>();
        }
        ThreadPoolExecutor pool = new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, queue, factory);
        THREAD_DATA_CACHE.putPool(factory.getPoolName(), pool);
        return pool;
    }

    /**
     * Create aScheduledThreadPoolExecutor（Timed thread pool for execution）
     *
     * @param threadCount The number of core threads in the thread pool
     * @param factory     Factory used by thread pool to create threads
     * @return ScheduledThreadPoolExecutor Thread Pool Object
     */
    public static final ScheduledThreadPoolExecutor createScheduledThreadPool(int threadCount, NulsThreadFactory factory) {
        if (factory == null) {
            throw new RuntimeException("thread factory cannot be null!");
        }
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(threadCount, factory);
        THREAD_DATA_CACHE.putPool(factory.getPoolName(), pool);
        return pool;
    }


    /**
     * Create a core thread count of1ofScheduledThreadPoolExecutor（Timed thread pool for execution）
     *
     * @param factory Factory used by thread pool to create threads
     * @return ScheduledThreadPoolExecutor Thread Pool Object
     */
    public static final ScheduledThreadPoolExecutor createScheduledThreadPool(NulsThreadFactory factory) {
        return createScheduledThreadPool(1, factory);
    }

    /**
     * Execute tasks through thread pool
     *
     * @param callable
     * @param <V>
     * @return
     */
    public static final <V> Future<V> asynExecuteCallable(Callable<V> callable) {
        if (null == callable) {
            throw new RuntimeException("runnable is null");
        }
        if (TEMPORARY_THREAD_POOL == null) {
            throw new RuntimeException("temporary thread pool not initialized yet");
        }
        BlockingQueue<Runnable> blockingQueue = TEMPORARY_THREAD_POOL.getQueue();
        if (blockingQueue.size() > 200) {
            Log.info("Task Queue 100 Size Warning!!! Task info is " + callable.toString());
        }
        Future<V> future = TEMPORARY_THREAD_POOL.submit(callable);
        int i = TEMPORARY_THREAD_POOL.getQueue().size();
        if (i > 10) {
            System.out.println("thread pool size:" + i);
        }
        return future;
    }

    /**
     * Execute tasks through thread pool
     *
     * @param runnable Runnable
     */
    public static final void asynExecuteRunnable(Runnable runnable) {
        if (null == runnable) {
            throw new RuntimeException("runnable is null");
        }
        if (TEMPORARY_THREAD_POOL == null) {
            throw new RuntimeException("temporary thread pool not initialized yet");
        }
        BlockingQueue<Runnable> blockingQueue = TEMPORARY_THREAD_POOL.getQueue();
        if (blockingQueue.size() > 200) {
            Log.info("Task Queue 100 Size Warning!!! Task info is " + runnable.toString());
        }
        TEMPORARY_THREAD_POOL.execute(runnable);
        int i = TEMPORARY_THREAD_POOL.getQueue().size();
        if (i > 10) {
            System.out.println("thread pool size:" + i);
        }
    }

    /**
     * Create a watchful thread and execute it
     *
     * @param threadName Thread Name
     * @param runnable   Runnable
     */
    public static final void createAndRunThread(String threadName, Runnable runnable) {
        createAndRunThread(threadName, runnable, true);
    }

    public static final void createAndRunThread(String threadName, Runnable runnable, boolean deamon) {
        NulsThreadFactory factory = new NulsThreadFactory(threadName);
        Thread thread = factory.newThread(runnable);
        thread.setDaemon(deamon);
        thread.start();
    }

    /**
     * Get all threads in the thread pool
     *
     * @return List<Thread>
     */
    public static final List<Thread> getThreadList() {
        return THREAD_DATA_CACHE.getThreadList();
    }

    /**
     * Obtain the corresponding thread based on its name
     *
     * @param threadName Thread Name
     * @return Thread
     */
    public static Thread getThread(String threadName) {
        return THREAD_DATA_CACHE.getThread(threadName);
    }

    /**
     * Retrieve the thread created by the specified thread factory name
     *
     * @param poolName Thread Factory Name
     * @return List<Thread>
     */
    public static List<Thread> getPoolThread(String poolName) {
        return THREAD_DATA_CACHE.getThreadList(poolName);
    }

    /**
     * Stop all currently executing threads
     */
    public static void stopAllThread() {
        List<ThreadPoolExecutor> poolList = THREAD_DATA_CACHE.getPoolList();
        for (ThreadPoolExecutor pool : poolList) {
            pool.shutdown();
        }
        List<Thread> threadList = THREAD_DATA_CACHE.getThreadList();
        if (threadList.size() > 0) {
            for (Thread thread : threadList) {
                if (thread.getState() == Thread.State.RUNNABLE) {
                    thread.interrupt();
                }
            }
        }
        THREAD_DATA_CACHE.removeAllThread();
    }

    /**
     * Stop the thread with the specified name
     *
     * @param threadName Thread Name
     */
    public static void stopThread(String threadName) {
        Thread thread = THREAD_DATA_CACHE.getThread(threadName);
        if (thread.getState() == Thread.State.RUNNABLE) {
            thread.interrupt();
        }
        THREAD_DATA_CACHE.removeThread(threadName);
    }
}
