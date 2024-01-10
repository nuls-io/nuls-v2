/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.transaction.utils.TxDuplicateRemoval;
import io.nuls.transaction.utils.TxUtil;

import java.util.*;

/**
 * Process new transactions broadcast by other nodes in the network
 *
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
            chain.getLogger().error("NetTxProcessTask Exception");
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
                    Thread.sleep(10000L);
                    continue;
                }
                List<TransactionNetPO> txNetList = new ArrayList<>(TxConstant.NET_TX_PROCESS_NUMBER_ONCE);
                chain.getUnverifiedQueue().drainTo(txNetList, TxConstant.NET_TX_PROCESS_NUMBER_ONCE);
                //grouping Verifier
                Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
                Iterator<TransactionNetPO> it = txNetList.iterator();
                int packableTxMapDataSize = 0;
                for(Transaction tx : chain.getPackableTxMap().values()){
                    packableTxMapDataSize += tx.size();
                }
                while (it.hasNext()) {
                    TransactionNetPO txNetPO = it.next();
                    Transaction tx = txNetPO.getTx();
                    //To be packaged queuemapExceeding the predetermined value,We will no longer accept transaction processing,Direct forwarding of complete transactions
                    if (TxUtil.discardTx(chain, packableTxMapDataSize, tx)) {
                        //To be packaged queuemapExceeding the predetermined value, Do not handle forwarding failures
                        String hash = tx.getHash().toHex();
                        NetworkCall.broadcastTx(chain, tx, TxDuplicateRemoval.getExcludeNode(hash));
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
                //Save torocksdb
                unconfirmedTxStorageService.putTxList(chain.getChainId(), txNetList);
                for (TransactionNetPO txNet : txNetList) {
                    Transaction tx = txNet.getTx();
                    if (chain.getPackaging().get()) {
                        //When a node is a block out node, Only then will the transaction be placed in the queue to be packaged
                        packablePool.add(chain, tx);
                    }
                    //Network transactions do not handle forwarding failures
                    String hash = tx.getHash().toHex();
                    NetworkCall.forwardTxHash(chain, tx.getHash(), TxDuplicateRemoval.getExcludeNode(hash));
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
                //If there is an error, delete the entire transaction of the module
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
            /**Conflict detection has failed, Execute clear and unconfirmed rollback fromtxNetListdelete*/
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

            Iterator<TransactionNetPO> it = txNetList.iterator();
            removeAndGo:
            while (it.hasNext()) {
                TransactionNetPO transactionNetPO = it.next();
                Transaction tx = transactionNetPO.getTx();
                //Remove transactions with failed ledger verification
                for (String hash : failHashs) {
                    String hashStr = tx.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        it.remove();
                        continue removeAndGo;
                    }
                }
                //Remove orphan transactions, Simultaneously placing orphan transactions into the orphan pool
                for (String hash : orphanHashs) {
                    String hashStr = tx.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        //Orphan Trading
                        List<TransactionNetPO> chainOrphan = chain.getOrphanList();
                        //The total size of orphan transaction set data
                        if (chain.getOrphanListDataSize().get() >= TxConstant.ORPHAN_LIST_MAX_DATA_SIZE) {
                            it.remove();
                            break;
                        } else {
                            synchronized (chainOrphan) {
                                chainOrphan.add(transactionNetPO);
                                chain.getOrphanListDataSize().addAndGet(transactionNetPO.getTx().size());
                            }
                            it.remove();
                            continue removeAndGo;
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

}
