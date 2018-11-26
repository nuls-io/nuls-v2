package io.nuls.transaction.utils;

import io.nuls.base.data.Transaction;
import io.nuls.transaction.model.bo.CrossChainTx;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 管理接收的其他链创建的跨链交易
 * @author: Charlie
 * @date: 2018/11/26
 */
public class CrossTxVerifyingManager {
    private static final CrossTxVerifyingManager INSTANCE = new CrossTxVerifyingManager();

    private Queue<CrossChainTx> localTxQueue;

    public CrossTxVerifyingManager(){
        this.localTxQueue = new LinkedBlockingDeque<>();
    }

    public static CrossTxVerifyingManager getInstance(){
        return INSTANCE;
    }
}
