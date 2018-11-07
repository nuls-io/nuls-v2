package io.nuls.eventbus.event;

/**
 * Created by wangkun23 on 2018/10/16.
 */
public class DeadEvent {

    private final Object source;
    private final Object event;


    public DeadEvent(Object source, Object event) {
        this.source = source;
        this.event = event;
    }

}
