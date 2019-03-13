package io.nuls.rpc.modulebootstrap;

import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 14:47
 * @Description:
 *    模块依赖管理状态通知
 *    通过重试的方式确保通知成功。若失败等待1秒重新通知，直到成功为止
 */
@Component
@Slf4j
public class NotifySender implements Runnable, InitializingBean {

    ScheduledThreadPoolExecutor executor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("notify-sender"));

    Queue<Callable<Boolean>> notifyQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
        while(true){
            while(!notifyQueue.isEmpty())     {
                Callable<Boolean> caller = notifyQueue.poll();
                try {
                    Boolean success = caller.call();
                    if(!success){
                        notifyQueue.offer(caller);
                    }
                } catch (Exception e) {
                    notifyQueue.offer(caller);
                }
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                log.error("notify sender thread error",e);
            }
        }
    }

    public void send(Callable<Boolean> caller){
        this.notifyQueue.offer(caller);
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        executor.execute(this);
    }
}
