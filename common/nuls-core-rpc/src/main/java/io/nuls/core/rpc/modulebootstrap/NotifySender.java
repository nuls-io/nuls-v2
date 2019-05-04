package io.nuls.core.rpc.modulebootstrap;

import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 14:47
 * @Description: 模块依赖管理状态通知
 * 通过重试的方式确保通知成功。若失败等待1秒重新通知，直到成功为止
 */
@Component
public class NotifySender implements Runnable, InitializingBean {

    private class Sender {

        String key;

        int retry;

        int retryTotal;

        Callable<Boolean> caller;

        void retry(){
            this.retry++;
        }

        boolean canRetry(){
            return retry < retryTotal;
        }

        public Sender(String key,int retryTotal,Callable<Boolean> caller){
            this.retry = 0;
            this.retryTotal = retryTotal;
            this.key = key;
            this.caller = caller;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Sender)) {
                return false;
            }

            Sender sender = (Sender) o;

            return key != null ? key.equals(sender.key) : sender.key == null;
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }
    }

    ScheduledThreadPoolExecutor executor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("notify-sender"));

    Queue<Sender> notifyQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
        while (true) {
            Queue<Sender> temp = new ConcurrentLinkedQueue<>();
            while (!notifyQueue.isEmpty()) {
                Sender sender = notifyQueue.poll();
                Callable<Boolean> caller = sender.caller;
                try {
                    Boolean success = caller.call();
                    if (!success) {
                        retry(temp,sender);
                    }
                } catch (Exception e) {
                    retry(temp,sender);
                }
            }
            while(!temp.isEmpty()){
                notifyQueue.offer(temp.poll());
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                Log.error("notify sender thread error", e);
            }
        }
    }

    private void retry(Queue<Sender>temp,Sender sender){
        if(sender.canRetry()){
            Log.warn("notify {} fail, retry {}",sender.key,sender.retry);
            sender.retry();
            temp.offer(sender);
        }else{
            Log.error("rpc module notify fail ：{}",sender.key);
        }
    }

    public void send(String  key,int retryTotal,Callable<Boolean> caller) {
        this.notifyQueue.offer(new Sender(key,retryTotal,caller));
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        executor.execute(this);
    }


}
