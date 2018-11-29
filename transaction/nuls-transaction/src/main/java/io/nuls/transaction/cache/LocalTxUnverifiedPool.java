package io.nuls.transaction.cache;

import io.nuls.base.data.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 新收到的本地交易，等待进行验证处理
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

    public boolean addInFirst(Transaction tx) {
        try {
            if (tx == null) {
                return false;
            }
            ((LinkedBlockingDeque)localTxQueue).addFirst(tx);
            return true;
        } finally {
        }
    }

    public boolean add(Transaction tx) {
        try {
            if (tx == null) {
                return false;
            }
            localTxQueue.offer(tx);
            return true;
        } finally {
        }
    }

    /**
     * Get a TxContainer, the first TxContainer received, removed from the memory pool after acquisition
     * <p>
     * 获取一笔交易，最先存入的交易，获取之后从内存池中移除
     *
     * @return TxContainer
     */
    public Transaction get() {
        return localTxQueue.poll();
    }

    public List<Transaction> getAll() {
        List<Transaction> txs = new ArrayList<>();
        Iterator<Transaction> it = localTxQueue.iterator();
        while (it.hasNext()) {
            txs.add(it.next());
        }
        return txs;
    }

    public void clear() {
        try {
            localTxQueue.clear();
        } finally {

        }
    }

    public int size() {
        return localTxQueue.size();
    }

    public int getPoolSize() {
        return localTxQueue.size();
    }

}
