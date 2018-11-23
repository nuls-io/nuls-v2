package io.nuls.eventbus;


import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 在事件总线上注册订阅者<p>
 * Registry of subscribers to a single event bus.<p>
 * Created by wangkun23 on 2018/10/16.
 */
public class SubscriberRegistry {

    private final ConcurrentMap<Class<?>, CopyOnWriteArraySet<Subscriber>> subscribers = new ConcurrentHashMap();

    /**
     * 该注册管理器所在的事件总线
     * The event bus this registry belongs to.
     */
    private final EventBus bus;

    SubscriberRegistry(EventBus bus) {
        this.bus = bus;
    }

    /**
     * 注册订阅者
     * Registers all subscriber
     */
    void register(Object listener) {

    }

    /**
     * 取消注册订阅者
     * Unregisters all subscriber
     */
    void unregister(Object listener) {

    }

    Iterator<Subscriber> getSubscribers(Object event) {
        //TODO.. 查询订阅该事件的订阅者，如果没有订阅就不发事件
        return null;
    }
}