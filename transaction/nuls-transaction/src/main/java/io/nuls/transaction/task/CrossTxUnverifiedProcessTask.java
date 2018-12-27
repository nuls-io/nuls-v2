/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.task;

import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxUnprocessedStorageService;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.service.CrossChainTxService;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018-12-27
 */
public class CrossTxUnverifiedProcessTask implements Runnable {


    private CrossChainTxService crossChainTxService = SpringLiteContext.getBean(CrossChainTxService.class);
    private CrossChainTxStorageService crossChainTxStorageService = SpringLiteContext.getBean(CrossChainTxStorageService.class);
    private CrossChainTxUnprocessedStorageService crossChainTxUnprocessedStorageService = SpringLiteContext.getBean(CrossChainTxUnprocessedStorageService.class);
    private TransactionManager transactionManager = SpringLiteContext.getBean(TransactionManager.class);
    private Chain chain;

    public CrossTxUnverifiedProcessTask(Chain chain) {
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

    /**
     * 1.基础验证
     * 2.发送跨链验证
     *
     * @param chain
     */
    private void doTask(Chain chain) {
        int chainId = chain.getChainId();
        List<CrossChainTx> unprocessedList = crossChainTxUnprocessedStorageService.getTxList(chainId);
        for(CrossChainTx ctx : unprocessedList){
            //交易验证
            transactionManager.verify(chain, ctx.getTx());
            //todo 发送跨链验证消息


            ctx.setState(TxConstant.CTX_VERIFY_REQUEST_1);
            //添加到处理中
            crossChainTxStorageService.putTx(chainId, ctx);
        }
        //从未处理DB表中清除
        crossChainTxUnprocessedStorageService.removeTxList(chainId, unprocessedList);
    }
}
