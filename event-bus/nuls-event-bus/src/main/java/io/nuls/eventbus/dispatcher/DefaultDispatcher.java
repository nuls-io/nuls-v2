package io.nuls.eventbus.dispatcher;

import io.nuls.eventbus.Subscriber;

import java.util.Iterator;

/**
 * 默认dispatcher的实现
 * <p>
 * Created by wangkun23 on 2018/10/16.
 */
public class DefaultDispatcher extends Dispatcher {
    /**
     * Dispatches the given {@code event} to the given {@code subscribers}.
     *
     * @param event
     * @param subscribers
     */
    @Override
    public void dispatch(Object event, Iterator<Subscriber> subscribers) {

    }
}
