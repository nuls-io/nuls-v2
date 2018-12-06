package io.nuls.transaction.cache;

import io.nuls.transaction.model.bo.TxWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 新收到的本地交易，等待进行验证处理
 * @author: Charlie
 * @date: 2018/11/26
 */
public class LocalTxUnverifiedPool {

    private static final LocalTxUnverifiedPool INSTANCE = new LocalTxUnverifiedPool();

    private BlockingDeque<TxWrapper> localTxQueue;

    public LocalTxUnverifiedPool(){
        this.localTxQueue = new LinkedBlockingDeque<>();
    }

    public static LocalTxUnverifiedPool getInstance(){
        return INSTANCE;
    }

    public boolean addInFirst(TxWrapper txWrapper) {
        try {
            if (txWrapper == null) {
                return false;
            }
            localTxQueue.addFirst(txWrapper);
            return true;
        } finally {
        }
    }

    public boolean add(TxWrapper txWrapper) {
        try {
            if (txWrapper == null) {
                return false;
            }
            localTxQueue.offer(txWrapper);
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
    public TxWrapper get() {
        return localTxQueue.poll();
    }

    public List<TxWrapper> getAll() {
        List<TxWrapper> txWrappers = new ArrayList<>();
        Iterator<TxWrapper> it = localTxQueue.iterator();
        while (it.hasNext()) {
            txWrappers.add(it.next());
        }
        return txWrappers;
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
