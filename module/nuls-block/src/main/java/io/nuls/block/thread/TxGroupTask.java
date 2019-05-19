package io.nuls.block.thread;

import io.nuls.block.message.HashListMessage;

import java.util.StringJoiner;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * TxGroup请求线程
 *
 * @author captain
 * @version 1.0
 * @date 18-12-4 下午8:29
 */
public class TxGroupTask implements Delayed {
    private long id;
    private HashListMessage request;
    private String nodeId;
    /**
     * 延迟时长,这个是必须的属性因为要按照这个判断延时时长。
     */
    private long excuteTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public HashListMessage getRequest() {
        return request;
    }

    public void setRequest(HashListMessage request) {
        this.request = request;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public long getExcuteTime() {
        return excuteTime;
    }

    public void setExcuteTime(long excuteTime) {
        this.excuteTime = excuteTime;
    }

    public TxGroupTask() {
    }

    public TxGroupTask(long id, HashListMessage request, long delayTime) {
        this.id = id;
        this.request = request;
        this.excuteTime = TimeUnit.NANOSECONDS.convert(delayTime, TimeUnit.MILLISECONDS) + System.nanoTime();
    }


    /**
     * 自定义实现比较方法返回 1 0 -1三个参数
     *
     * @param delayed
     * @return
     */
    @Override
    public int compareTo(Delayed delayed) {
        TxGroupTask msg = (TxGroupTask) delayed;
        return Long.compare(this.id, msg.id);
    }


    /**
     * 延迟任务是否到时
     * 如果返回的是负数则说明到期
     * 否则还没到期
     *
     * @param unit
     * @return
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.excuteTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TxGroupTask.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("request=" + request)
                .add("nodeId='" + nodeId + "'")
                .add("excuteTime=" + excuteTime)
                .toString();
    }
}
