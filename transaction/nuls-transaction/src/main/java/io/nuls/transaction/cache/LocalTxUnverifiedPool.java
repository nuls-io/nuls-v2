package io.nuls.transaction.cache;

import io.nuls.base.data.Transaction;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author: Charlie
 * @date: 2018/11/26
 */
public class LocalTxUnverifiedPool {
    private static final LocalTxUnverifiedPool INSTANCE = new LocalTxUnverifiedPool();

    private Queue<Transaction> localTxQueue;

    public LocalTxUnverifiedPool(){
        this.localTxQueue = new LinkedBlockingDeque<>();
    }

    public static LocalTxUnverifiedPool getInstance(){
        return INSTANCE;
    }

}
