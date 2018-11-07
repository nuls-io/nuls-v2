package io.nuls.eventbus.dispatcher;

import io.nuls.eventbus.Subscriber;

import java.util.Iterator;

/**
 * Handler for dispatching events to subscribers
 * Created by wangkun23 on 2018/10/16.
 */
public abstract class Dispatcher {

    /**
     * Dispatches the given {@code event} to the given {@code subscribers}.
     */
    public abstract void dispatch(Object event, Iterator<Subscriber> subscribers);

}
