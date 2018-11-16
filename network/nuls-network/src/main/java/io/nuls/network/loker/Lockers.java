package io.nuls.network.loker;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Lockers {
    public final static Lock NODE_LAUNCH_CONNECT_LOCK = new ReentrantLock();
    public final static Lock NODE_ESTABLISH_CONNECT_LOCK = new ReentrantLock();
}
