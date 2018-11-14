package io.nuls.network.loker;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Lockers {
    public final static Lock NETWORK_GROUP_NODE_CONNECT_LOCK = new ReentrantLock();
}
