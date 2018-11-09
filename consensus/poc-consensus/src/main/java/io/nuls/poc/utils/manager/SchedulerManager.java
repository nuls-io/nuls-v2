package io.nuls.poc.utils.manager;

import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SchedulerManager {
    public static void main(String[] args){
        ThreadFactory tf = new ThreadFactory() {

            AtomicLong counter = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "scheduler" + counter.getAndIncrement());
            }
        };
        /*Map<Integer, ScheduledThreadPoolExecutor> scheduleMap = new HashMap<>();
        scheduleMap.put(1, ThreadUtils.createScheduledThreadPool(3,new NulsThreadFactory("consensus1")));
        scheduleMap.put(2, ThreadUtils.createScheduledThreadPool(3,new NulsThreadFactory("consensus2")));
        scheduleMap.get(1).scheduleAtFixedRate(new TestRunnable(),100L,100L, TimeUnit.MILLISECONDS);
        scheduleMap.get(2).scheduleAtFixedRate(new TestRunnable(),100L,100L, TimeUnit.MILLISECONDS);*/
        //ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1,tf);
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(2,new NulsThreadFactory("test"));
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new TestRunnable(),50L,100L,TimeUnit.MILLISECONDS);
        //scheduledThreadPoolExecutor=new ScheduledThreadPoolExecutor(1);
    }
}
