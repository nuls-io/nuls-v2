package io.nuls.transaction.cache;

import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 交易已完成交易管理模块的校验(打包的时候从这里取)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
@Component
public class PackablePool {

    public void addInFirst(Chain chain, Transaction tx) {
        chain.getTxQueue().addFirst(tx);
    }

    public boolean add(Chain chain, Transaction tx) {
        return chain.getTxQueue().offer(tx);
    }

    /**
     * Get a TxContainer, the first TxContainer received, removed from the memory pool after acquisition
     * <p>
     * 获取一笔交易，最先存入的交易，获取之后从内存池中移除
     *
     * @return TxContainer
     */
    public Transaction poll(Chain chain) {
        return chain.getTxQueue().poll();
    }

    public List<Transaction> getAll(Chain chain) {
        List<Transaction> txs = new ArrayList<>(TxConstant.INIT_CAPACITY_4);
        Iterator<Transaction> it = chain.getTxQueue().iterator();
        while (it.hasNext()) {
            txs.add(it.next());
        }
        return txs;
    }

    public boolean exist(Chain chain, Transaction tx) {
        return chain.getTxQueue().contains(tx);
    }

    public int getPoolSize(Chain chain) {
        return chain.getTxQueue().size();
    }



    public void clear(Chain chain) {
        chain.getTxQueue().clear();
    }

}
