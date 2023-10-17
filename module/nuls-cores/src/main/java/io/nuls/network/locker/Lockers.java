package io.nuls.network.locker;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lan
 * @description 全局锁, 用于同步代码片段
 * Lockers
 * @date 2018/11/13
 **/
public class Lockers {
    /**
     * 用于同步锁定协议注册与清理
     * Used for synchronous lock protocol registration and cleanup
     */
    public final static Lock PROTOCOL_HANDLERS_REGISTER_LOCK = new ReentrantLock();
}
