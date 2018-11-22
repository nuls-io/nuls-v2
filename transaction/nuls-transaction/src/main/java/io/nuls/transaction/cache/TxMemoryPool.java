package io.nuls.transaction.cache;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.cache.LimitHashMap;
import io.nuls.transaction.constant.TransactionConstant;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 交易已完成交易管理模块的校验，待打包
 * @author: Charlie
 * @date: 2018/11/13
 */
public class TxMemoryPool {

    private final static TxMemoryPool INSTANCE = new TxMemoryPool();

    private Queue<Transaction> txQueue;

    //private LimitHashMap<NulsDigestData, Transaction> orphanContainer;

    private TxMemoryPool() {
        this.txQueue = new LinkedBlockingDeque<>();
        //this.orphanContainer = new LimitHashMap(TransactionConstant.ORPHAN_CONTAINER_MAX_SIZE);
    }

    public static TxMemoryPool getInstance() {
        return INSTANCE;
    }


}
