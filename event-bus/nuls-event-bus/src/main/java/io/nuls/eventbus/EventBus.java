package io.nuls.eventbus;

import io.nuls.eventbus.dispatcher.Dispatcher;
import io.nuls.eventbus.event.DeadEvent;
import io.nuls.eventbus.handler.SubscriberExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.Executor;

/**
 * 事件总线
 * <p>
 * 转发事件到每个订阅者
 * Event Bus
 * Dispatches events to subscriber
 * Created by wangkun23 on 2018/10/16.
 */
public class EventBus {
    final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private final SubscriberRegistry subscribers = new SubscriberRegistry(this);

    private final Executor executor;
    private final SubscriberExceptionHandler exceptionHandler;
    private final Dispatcher dispatcher;

    EventBus(Executor executor,
             Dispatcher dispatcher,
             SubscriberExceptionHandler exceptionHandler) {
        this.executor = executor;
        this.dispatcher = dispatcher;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * 订阅者注册后，可以接口所有事件
     * Registers all subscriber to receive events.
     *
     * @param object object whose subscriber should be registered.
     */
    public void register(Object object) {
        logger.info("register new subscriber");
    }

    /**
     * 订阅者取消注册
     * Unregisters all subscriber
     */
    public void unregister(Object object) {
        logger.info("subscriber has been unregistered");
    }

    /**
     * 发送事件
     * Posts an event to all registered subscribers.
     *
     * @param event event to post.
     */
    public void post(Object event) {
        Iterator<Subscriber> eventSubscribers = subscribers.getSubscribers(event);
        if (eventSubscribers.hasNext()) {
            dispatcher.dispatch(event, eventSubscribers);
        } else if (!(event instanceof DeadEvent)) {
            // the event had no subscribers and was not itself a DeadEvent
            post(new DeadEvent(this, event));
        }
    }

}
