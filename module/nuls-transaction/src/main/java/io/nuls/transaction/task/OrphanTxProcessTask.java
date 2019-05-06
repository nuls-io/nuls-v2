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

import io.nuls.base.data.Transaction;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.rpc.util.TimeUtils;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyLedgerResult;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.utils.TransactionComparator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2019/4/26
 */
public class OrphanTxProcessTask implements Runnable {


    private Chain chain;

    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);

    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);

    private TransactionComparator txComparator = SpringLiteContext.getBean(TransactionComparator.class);

    public OrphanTxProcessTask(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        try {
            doOrphanTxTask(chain);
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error(e);
        }
    }


    private void doOrphanTxTask(Chain chain) throws NulsException {
        List<TransactionNetPO> chainOrphan = chain.getOrphanList();
        if(chainOrphan.size() == 0){
            return;
        }
        //把孤儿交易list的交易全部取出来，然后清空；如果有验不过的 再加回去,避免阻塞新的孤儿交易的加入
        List<TransactionNetPO> orphanTxList = new LinkedList<>();
        synchronized (chainOrphan){
            orphanTxList.addAll(chainOrphan);
            chainOrphan.clear();
        }
        try {
            //时间排序TransactionTimeComparator
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("[OrphanTxProcessTask] OrphanTxList size:{}", orphanTxList.size());
            orphanTxList.sort(txComparator);
            Iterator<TransactionNetPO> it = orphanTxList.iterator();
            while (it.hasNext()) {
                TransactionNetPO txNet = it.next();
                boolean rs = processTx(chain, txNet);
                if (rs) {
                    it.remove();
                    chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("[OrphanTxProcessTask] Orphan tx remove - type:{} - txhash:{}, -orphanTxList size:{}",
                            txNet.getTx().getType(), txNet.getTx().getHash().getDigestHex(), orphanTxList.size());
                }
            }
        } catch (RuntimeException e) {
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        } finally {
            if(orphanTxList.size() > 0){
                chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("[OrphanTxProcessTask] Orphan tx add back size:{}", orphanTxList.size());
                synchronized (chainOrphan){
                    chainOrphan.addAll(orphanTxList);
                }
            }
        }
    }

    /**
     * 处理孤儿交易
     * @param chain
     * @param txNet
     * @return true     表示该需要从孤儿交易池中清理掉，1:验证通过的交易，2：在孤儿池中超时的交易，3：验证账本失败(异常等)
     *         false    表示仍然需要保留在孤儿交易池中
     */
    private boolean processTx(Chain chain, TransactionNetPO txNet){
        try {
            Transaction tx = txNet.getTx();
            int chainId = chain.getChainId();
            TransactionConfirmedPO existTx = txService.getTransaction(chain, tx.getHash());
            if(null != existTx){
                return true;
            }
            VerifyLedgerResult verifyLedgerResult = LedgerCall.commitUnconfirmedTx(chain, RPCUtil.encode(tx.serialize()));
            if(verifyLedgerResult.businessSuccess()){
                if(chain.getPackaging().get()) {
                    //当节点是出块节点时, 才将交易放入待打包队列
                    packablePool.add(chain, tx);
                    chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("[OrphanTxProcessTask] 加入待打包队列....hash:{}", tx.getHash().getDigestHex());
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chainId, tx);
                //转发交易hash
                NetworkCall.forwardTxHash(chain.getChainId(),tx.getHash(), txNet.getExcludeNode());
                return true;
            }
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("[OrphanTxProcessTask] tx coinData verify fail - orphan: {}, - code:{}, type:{}, - txhash:{}", verifyLedgerResult.getOrphan(),
                    verifyLedgerResult.getErrorCode() == null ? "" : verifyLedgerResult.getErrorCode().getCode(),tx.getType(), tx.getHash().getDigestHex());

            if(!verifyLedgerResult.getSuccess()){
                //如果处理孤儿交易时，账本验证返回异常，则直接清理该交易
                return true;
            }
            long currentTimeMillis = TimeUtils.getCurrentTimeMillis();
            //超过指定时间仍旧是孤儿交易，则删除
            return tx.getTime() < (currentTimeMillis - chain.getConfig().getOrphanTtl());
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).error(e);
            return false;
        }
    }

}
