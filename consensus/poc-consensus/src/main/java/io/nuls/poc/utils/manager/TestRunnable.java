package io.nuls.poc.utils.manager;

public class TestRunnable implements Runnable{
    private int perio = 1;

    @Override
    public void run() {
        //为周期任务捕获异常，避免异常影响下一周期的任务执行
        try {
            System.out.println("---------------第 " + perio + " 周期-------------");
            System.out.println("begin = " + System.currentTimeMillis() / 1000);//秒
            //任务执行时间
            Thread.sleep(2000);
            System.out.println("end =   " + System.currentTimeMillis() / 1000);
            perio++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
