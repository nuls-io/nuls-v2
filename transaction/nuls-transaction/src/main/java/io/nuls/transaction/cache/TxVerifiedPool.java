package io.nuls.transaction.cache;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.cache.LimitHashMap;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.TxWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 交易已完成交易管理模块的校验(打包的时候从这里取), 包括孤儿交易
 * @author: Charlie
 * @date: 2018/11/13
 */
public class TxVerifiedPool {

    private final static TxVerifiedPool INSTANCE = new TxVerifiedPool();

    private Queue<TxWrapper> txQueue;

    private LimitHashMap<NulsDigestData, TxWrapper> orphanContainer;

    private TxVerifiedPool() {
        this.txQueue = new LinkedBlockingDeque<>();
        this.orphanContainer = new LimitHashMap(TxConstant.ORPHAN_CONTAINER_MAX_SIZE);
    }

    public static TxVerifiedPool getInstance() {
        return INSTANCE;
    }

    public boolean addInFirst(TxWrapper txWrapper, boolean isOrphan) {
        try {
            if (txWrapper == null) {
                return false;
            }
            //check Repeatability
            if (isOrphan) {
                NulsDigestData hash = txWrapper.getTx().getHash();
                orphanContainer.put(hash, txWrapper);
            } else {
                ((LinkedBlockingDeque) txQueue).addFirst(txWrapper);
            }
            return true;
        } finally {
        }
    }

    public boolean add(TxWrapper txWrapper, boolean isOrphan) {
        try {
            if (txWrapper == null) {
                return false;
            }
            //check Repeatability
            if (isOrphan) {
                NulsDigestData hash = txWrapper.getTx().getHash();
                orphanContainer.put(hash, txWrapper);
            } else {
                txQueue.offer(txWrapper);
            }
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
        return txQueue.poll();
    }

    public List<TxWrapper> getAll() {
        List<TxWrapper> txs = new ArrayList<>();
        Iterator<TxWrapper> it = txQueue.iterator();
        while (it.hasNext()) {
            txs.add(it.next());
        }
        return txs;
    }

    public List<TxWrapper> getAllOrphan() {
        return new ArrayList<>(orphanContainer.values());
    }

    public void remove(NulsDigestData hash) {
        orphanContainer.remove(hash);
    }

    public boolean exist(NulsDigestData hash) {
        return orphanContainer.containsKey(hash);
    }

    public void clear() {
        try {
            txQueue.clear();
            orphanContainer.clear();
        } finally {
        }
    }

    public int size() {
        return txQueue.size();
    }

    public int getPoolSize() {
        return txQueue.size();
    }

    public int getOrphanPoolSize() {
        return orphanContainer.size();
    }

    public void removeOrphan(NulsDigestData hash) {
        this.orphanContainer.remove(hash);
    }

}
