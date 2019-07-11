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
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.rpc.call.TransactionCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.utils.TxUtil;

import java.util.*;

/**
 * @author: Charlie
 * @date: 2019/6/11
 */
public class NetTxProcessTask implements Runnable {
    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);
    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private Chain chain;

    public NetTxProcessTask(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            e.printStackTrace();
            chain.getLogger().error(e);
        }
    }


    private void process() {
        while (true) {
            try {
                if (chain.getUnverifiedQueue().isEmpty()) {
                    Thread.sleep(3000L);
                    continue;
                }
                if (chain.getProtocolUpgrade().get()) {
                    chain.getLogger().info("Protocol upgrade pause process new tx..");
                    Thread.sleep(3000L);
                    continue;
                }
                List<TransactionNetPO> txNetList = new ArrayList<>(TxConstant.NET_TX_PROCESS_NUMBER_ONCE);
                chain.getUnverifiedQueue().drainTo(txNetList, TxConstant.NET_TX_PROCESS_NUMBER_ONCE);
                StatisticsTask.txNetListTotal.addAndGet(txNetList.size());
                //分组 调验证器
                Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
                Iterator<TransactionNetPO> it = txNetList.iterator();
                int packableTxMapSize = chain.getPackableTxMap().size();
                while (it.hasNext()) {
                    TransactionNetPO txNetPO = it.next();
                    Transaction tx = txNetPO.getTx();
                    /*if (txService.isTxExists(chain, tx.getHash())) {
                        it.remove();
                        continue;
                    }*/
                    //待打包队列map超过预定值,则不再接受处理交易,直接转发交易完整交易
                    if (TxUtil.discardTx(packableTxMapSize)) {
                        NetworkCall.broadcastTx(chain, tx, txNetPO.getExcludeNode());
                        it.remove();
                        continue;
                    }
                    TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
                }
                verifiction(chain, moduleVerifyMap, txNetList);
                verifyCoinData(chain, txNetList);
                if (txNetList.isEmpty()) {
                    continue;
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTxList(chain.getChainId(), txNetList);
                for (TransactionNetPO txNet : txNetList) {
                    Transaction tx = txNet.getTx();
                    if (chain.getPackaging().get()) {
                        //当节点是出块节点时, 才将交易放入待打包队列
                        packablePool.add(chain, tx);
                    }
                    //保存到rocksdb
                    //unconfirmedTxStorageService.putTx(chain.getChainId(), tx, txNet.getOriginalSendNanoTime());
                    NetworkCall.forwardTxHash(chain, tx.getHash(), txNet.getExcludeNode());
                    //chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("NEW TX count:{} - hash:{}", ++count, hash.toHex());
                }
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
    }


    private void verifiction(Chain chain, Map<String, List<String>> moduleVerifyMap, List<TransactionNetPO> txNetList) {
        Iterator<Map.Entry<String, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> moduleList = entry.getValue();
            String moduleCode = entry.getKey();
            List<String> txHashList = null;
            try {
                txHashList = TransactionCall.txModuleValidator(chain, moduleCode, moduleList);
            } catch (NulsException e) {
                chain.getLogger().error("Net new tx verify failed -txModuleValidator Exception:{}, module-code:{}, count:{} , return count:{}",
                        BaseConstant.TX_VALIDATOR, moduleCode, moduleList.size(), txHashList.size());
                //出错则删掉整个模块的交易
                Iterator<TransactionNetPO> its = txNetList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next().getTx();
                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                    if (txRegister.getModuleCode().equals(moduleCode)) {
                        its.remove();
                    }
                }
                continue;
            }
            if (null == txHashList || txHashList.isEmpty()) {
                continue;
            }
            chain.getLogger().error("[Net new tx verify failed] module:{}, module-code:{}, count:{} , return count:{}",
                    BaseConstant.TX_VALIDATOR, moduleCode, moduleList.size(), txHashList.size());
            /**冲突检测有不通过的, 执行清除和未确认回滚 从txNetList删除*/
            for (int i = 0; i < txHashList.size(); i++) {
                String hash = txHashList.get(i);
                Iterator<TransactionNetPO> its = txNetList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next().getTx();
                    if (hash.equals(tx.getHash().toHex())) {
                        its.remove();
                    }
                }
            }
        }
    }


    private void verifyCoinData(Chain chain, List<TransactionNetPO> txNetList) throws NulsException {
        if (txNetList.isEmpty()) {
            return;
        }
        try {
            Map verifyCoinDataResult = LedgerCall.commitBatchUnconfirmedTxs(chain, txNetList);
            List<String> failHashs = (List<String>) verifyCoinDataResult.get("fail");
            List<String> orphanHashs = (List<String>) verifyCoinDataResult.get("orphan");
            if (failHashs.isEmpty() && orphanHashs.isEmpty()) {
                return;
            }
            StatisticsTask.addOrphanCount.addAndGet(orphanHashs.size());
//            chain.getLogger().warn("Net new tx verify coinData, -txNetList：{} - failHashSize:{}, - orphanHashSize:{}",txNetList.size(), failHashs.size(), orphanHashs.size());
            Iterator<TransactionNetPO> it = txNetList.iterator();
            removeAndGo:
            while (it.hasNext()) {
                TransactionNetPO transactionNetPO = it.next();
                Transaction tx = transactionNetPO.getTx();
                //去除账本验证失败的交易
                for (String hash : failHashs) {
                    String hashStr = tx.getHash().toHex();
                    if (hash.equals(hashStr)) {
//                        chain.getLogger().error("Net new tx coinData verify fail, - type:{}, - txhash:{}",
//                                tx.getType(), hashStr);
                        it.remove();
                        continue removeAndGo;
                    }
                }
                //去除孤儿交易, 同时把孤儿交易放入孤儿池
                for (String hash : orphanHashs) {
                    String hashStr = tx.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        //孤儿交易
                        List<TransactionNetPO> chainOrphan = chain.getOrphanList();
                        synchronized (chainOrphan) {
                            chainOrphan.add(transactionNetPO);
                        }
//                        chain.getLogger().debug("Net new tx coinData orphan, - type:{}, - txhash:{}",
//                                tx.getType(), hashStr);
                        it.remove();
                        continue removeAndGo;
                    }
                }
            }
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

}
