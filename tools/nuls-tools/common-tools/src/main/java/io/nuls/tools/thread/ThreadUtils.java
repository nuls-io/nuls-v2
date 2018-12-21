package io.nuls.tools.thread;

import io.nuls.tools.log.Log;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import io.nuls.tools.thread.commom.ThreadCache;

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
     * 想线程缓存中添加一个线程
     *
     * @param poolName   线程工厂名称
     * @param threadName 线程名称
     * @param newThread  线程
     */
    public static final void putThread(String poolName, String threadName, Thread newThread) {
        THREAD_DATA_CACHE.putThread(poolName, threadName, newThread);
    }

    /**
     * 创建线程池
     *
     * @param threadCount 线程池的核心线程数
     * @param queueSize   线程池所使用的缓冲队列长度
     * @param factory     线程池创建线程使用的工厂
     * @return ThreadPoolExecutor 线程池对象
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
     * 创建一个ScheduledThreadPoolExecutor（定时执行的线程池）
     *
     * @param threadCount 线程池的核心线程数
     * @param factory     线程池创建线程使用的工厂
     * @return ScheduledThreadPoolExecutor 线程池对象
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
     * 创建一个核心线程数为1的ScheduledThreadPoolExecutor（定时执行的线程池）
     *
     * @param factory 线程池创建线程使用的工厂
     * @return ScheduledThreadPoolExecutor 线程池对象
     */
    public static final ScheduledThreadPoolExecutor createScheduledThreadPool(NulsThreadFactory factory) {
        return createScheduledThreadPool(1, factory);
    }

    /**
     * 通过线程池执行任务
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
     * 通过线程池执行任务
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
     * 创建一个守望线程并执行
     *
     * @param threadName 线程名称
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
     * 获取线程池中所有的线程
     *
     * @return List<Thread>
     */
    public static final List<Thread> getThreadList() {
        return THREAD_DATA_CACHE.getThreadList();
    }

    /**
     * 根据线程名称获取对应的线程
     *
     * @param threadName 线程名称
     * @return Thread
     */
    public static Thread getThread(String threadName) {
        Thread thread = THREAD_DATA_CACHE.getThread(threadName);
        return thread;
    }

    /**
     * 获取指定线程工厂名称创建的线程
     *
     * @param poolName 线程工厂名称
     * @return List<Thread>
     */
    public static List<Thread> getPoolThread(String poolName) {
        List<Thread> threadList = THREAD_DATA_CACHE.getThreadList(poolName);
        return threadList;
    }

    /**
     * 停止当前正在执行的所有线程
     */
    public static void stopAllThread() {
        List<ThreadPoolExecutor> poolList = THREAD_DATA_CACHE.getPoolList();
        if (null != poolList) {
            for (ThreadPoolExecutor pool : poolList) {
                pool.shutdown();
            }
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
     * 停止指定名称的线程
     *
     * @param threadName 线程名称
     */
    public static void stopThread(String threadName) {
        Thread thread = THREAD_DATA_CACHE.getThread(threadName);
        if (thread.getState() == Thread.State.RUNNABLE) {
            thread.interrupt();
        }
        THREAD_DATA_CACHE.removeThread(threadName);
    }
}
