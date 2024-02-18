package io.nuls.block.model;

import io.nuls.block.message.HashListMessage;

import java.util.StringJoiner;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * TxGroupRequest thread
 *
 * @author captain
 * @version 1.0
 * @date 18-12-4 afternoon8:29
 */
public class TxGroupTask implements Delayed {
    private long id;
    private HashListMessage request;
    private String nodeId;
    /**
     * Delay duration,This is a necessary attribute because the delay duration needs to be determined based on it.
     */
    private long excuteTime;

    public TxGroupTask() {
    }

    public TxGroupTask(long id, HashListMessage request, long delayTime) {
        this.id = id;
        this.request = request;
        this.excuteTime = TimeUnit.NANOSECONDS.convert(delayTime, TimeUnit.MILLISECONDS) + System.nanoTime();
    }

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

    /**
     * Custom implementation comparison method return 1 0 -1Three parameters
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
     * Will the delayed task arrive
     * If a negative number is returned, it indicates expiration
     * Otherwise, it hasn't expired yet
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
