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

package io.nuls.transaction.threadpool;

import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionNetPO;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2019/5/5
 */
public class NetTxProcessJob implements Runnable {

    private Chain chain;

    private TransactionNetPO txNet;

    private NetTxProcess netTxProcess;

    public NetTxProcessJob(Chain chain, TransactionNetPO txNet) {
        this.chain = chain;
        this.txNet = txNet;
        this.netTxProcess = SpringLiteContext.getBean(NetTxProcess.class);
    }

    @Override
    public void run() {
        try {
            //获取到交易就放入临时待处理集合
            List<TransactionNetPO> txNetList = chain.getTxNetProcessList();
            txNetList.add(txNet);
            if(txNetList.size() >= TxConstant.NET_TX_PROCESS_NUMBER_ONCE){
                netTxProcess.process(chain);
            }
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error(e);
        }
    }
}
