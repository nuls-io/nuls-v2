/*
 * MIT License
 *
 * Copyright (c) 2018-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.transaction.task;

import io.nuls.base.data.Transaction;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionUnconfirmedPO;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 未确认交易清理机制
 */
public class ClearUnconfirmedTxProcessTask implements Runnable {

    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);
    private TxConfig txConfig = SpringLiteContext.getBean(TxConfig.class);
    private Chain chain;

    public ClearUnconfirmedTxProcessTask(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        try {
            doTask(chain);
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
    }

    private void doTask(Chain chain) {
        List<byte[]> txKeyList = unconfirmedTxStorageService.getAllTxkeyList(chain.getChainId());
        if (txKeyList == null || txKeyList.size() == 0) {
            return;
        }
        int count = processUnconfirmedTxs(txKeyList);
        chain.getLogger().info("%%%%% Clean %%%%% [UnconfirmedTxProcessTask] expire count: {}", count);
    }

    private boolean processTx(Chain chain, Transaction tx) {
        try {
            txService.clearInvalidTx(chain, tx, false);
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
        return false;
    }

    /**
     * 过滤指定时间内过期的交易
     *
     * @param txKeyList
     * @return expireTxList
     */
    private int processUnconfirmedTxs(List<byte[]> txKeyList) {
        int unconfirmedTxsCount = 0;
        List<byte[]> queryList = new ArrayList<>();
        //一次最多处理1W笔
        for (int i = 0; i < txKeyList.size(); i++) {
            queryList.add(txKeyList.get(i));
            if (queryList.size() == 10000) {
                unconfirmedTxsCount += processExpireTxs(queryList);
                queryList.clear();
            }
        }
        if(!queryList.isEmpty()){
            unconfirmedTxsCount += processExpireTxs(queryList);
        }
        return unconfirmedTxsCount;
    }

    public int processExpireTxs(List<byte[]> queryList){
        //获取未确认的交易
        List<TransactionUnconfirmedPO> list = unconfirmedTxStorageService.getTransactionUnconfirmedPOList(chain.getChainId(), queryList);
        //计算出超时的未确认交易
        List<Transaction> expireTxList = getExpireTxList(list);
        int count = 0;
        Transaction tx;
        for (int i = 0; i < expireTxList.size(); i++) {
            tx = expireTxList.get(i);
            //如果该未确认交易不在待打包池中，则认为是过期脏数据，需要清理
            if (!packablePool.exist(chain, tx)) {
                processTx(chain, tx);
                StatisticsTask.clearUnconfirmedTx++;
                count++;
            }
        }
        return count;
    }

    /**
     * 过滤指定时间内过期的交易
     *
     * @param txPOList
     * @return expireTxList
     */
    private List<Transaction> getExpireTxList(List<TransactionUnconfirmedPO> txPOList) {
        List<Transaction> expireTxList = new ArrayList<>();
        long currentTimeSeconds = NulsDateUtils.getCurrentTimeSeconds();
        //过滤指定时间内过期的交易
        List<TransactionUnconfirmedPO> expireTxPOList = txPOList.stream().filter(txPo -> currentTimeSeconds - txConfig.getUnconfirmedTxExpire() > txPo.getCreateTime()).collect(Collectors.toList());
        expireTxPOList.forEach(txPo -> expireTxList.add(txPo.getTx()));
        return expireTxList;
    }

}
