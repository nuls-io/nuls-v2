package io.nuls.block.thread;

import io.nuls.block.message.HashListMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
public class TxGroupTask implements Delayed {
    @Getter
    @Setter
    private long id;
    @Getter
    @Setter
    private HashListMessage request;
    @Getter
    @Setter
    private String nodeId;
    /**
     * 延迟时长，这个是必须的属性因为要按照这个判断延时时长。
     */
    @Getter
    @Setter
    private long excuteTime;

    public TxGroupTask(long id, HashListMessage request, long delayTime) {
        this.id = id;
        this.request = request;
        this.excuteTime = TimeUnit.NANOSECONDS.convert(delayTime, TimeUnit.MILLISECONDS) + System.nanoTime();
    }

    // 自定义实现比较方法返回 1 0 -1三个参数
    @Override
    public int compareTo(Delayed delayed) {
        TxGroupTask msg = (TxGroupTask) delayed;
        return Long.compare(this.id, msg.id);
    }

    // 延迟任务是否到时就是按照这个方法判断如果返回的是负数则说明到期否则还没到期
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.excuteTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }
}
