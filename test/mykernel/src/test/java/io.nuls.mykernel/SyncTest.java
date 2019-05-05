package io.nuls.mykernel;

import java.util.Map;
import java.util.concurrent.*;

public class SyncTest {

    public static void main(String[] args) {
        long time = System.nanoTime();
        int count = 100000;

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        final Map<String, CountDownLatch> maps = new ConcurrentHashMap<>();

        for(int i = 0 ; i < count ; i ++) {
            final String c = String.valueOf(i);

            CountDownLatch countDownLatch = new CountDownLatch(1);
            maps.put(c, countDownLatch);

            executorService.submit(new Thread(() -> {
//                System.out.println(c);
                CountDownLatch cd = maps.get(c);
                cd.countDown();
            }));

            try {
                countDownLatch.await(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            maps.remove(c);
        }

        try {
            executorService.shutdown();
            executorService.awaitTermination(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("总耗时：" + ((System.nanoTime() - time) / 10000) / 100f + " ms");

    }
}
