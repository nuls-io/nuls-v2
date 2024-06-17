package io.nuls.network.locker;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lan
 * @description Global lock, Used to synchronize code snippets
 * Lockers
 * @date 2018/11/13
 **/
public class Lockers {
    /**
     * Used for synchronizing lock protocol registration and cleaning
     * Used for synchronous lock protocol registration and cleanup
     */
    public final static Lock PROTOCOL_HANDLERS_REGISTER_LOCK = new ReentrantLock();
}
