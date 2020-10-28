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
package io.nuls.api.rpc.controller.runner;

import io.nuls.api.ApiContext;
import io.nuls.api.analysis.AnalysisHandler;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.db.TransactionService;
import io.nuls.api.model.po.TransactionInfo;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;

import java.util.concurrent.TimeUnit;

/**
 * @author: PierreLuo
 * @date: 2020-03-31
 */
public class QueueContractRun implements Runnable {
    private int chainId;
    private String txHex;
    private TransactionService txService;

    public QueueContractRun(int chainId, String txHex, TransactionService txService) {
        this.chainId = chainId;
        this.txHex = txHex;
        this.txService = txService;
    }

    @Override
    public void run() {
        try {
            Result result = WalletRpcHandler.broadcastTx(chainId, txHex);

            if (result.isSuccess()) {
                Transaction tx = new Transaction();
                tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                TransactionInfo txInfo = AnalysisHandler.toTransaction(chainId, tx, ApiContext.protocolVersion);
                LoggerUtil.commonLog.info("排队广播指定合约交易[{}]成功", txInfo.getHash());
                txService.saveUnConfirmTx(chainId, txInfo, txHex);
                // 休眠10秒
                TimeUnit.SECONDS.sleep(10);
                return;
            }
            LoggerUtil.commonLog.error("排队广播指定合约交易失败, 详细: {}", result.toString());
        } catch (Exception e) {
            LoggerUtil.commonLog.error("排队广播指定合约交易失败", e);
        }
    }
}
