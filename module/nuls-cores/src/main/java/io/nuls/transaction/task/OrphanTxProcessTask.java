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

import io.nuls.base.RPCUtil;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.Orphans;
import io.nuls.transaction.model.bo.VerifyLedgerResult;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.utils.OrphanSort;
import io.nuls.transaction.utils.TxDuplicateRemoval;
import io.nuls.transaction.utils.TxUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2019/4/26
 */
public class OrphanTxProcessTask implements Runnable {


    private Chain chain;

    private PackablePool packablePool = SpringLiteContext.getBean(PackablePool.class);

    private TxService txService = SpringLiteContext.getBean(TxService.class);
    private UnconfirmedTxStorageService unconfirmedTxStorageService = SpringLiteContext.getBean(UnconfirmedTxStorageService.class);

    private OrphanSort orphanSort = SpringLiteContext.getBean(OrphanSort.class);

    public OrphanTxProcessTask(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        try {
            doOrphanTxTask(chain);
        } catch (Exception e) {
            chain.getLogger().error("OrphanTxProcessTask Exception");
            chain.getLogger().error(e);
        }
    }


    private void doOrphanTxTask(Chain chain) throws NulsException {
        if(chain.getProtocolUpgrade().get()){
            chain.getLogger().info("Protocol upgrade pause process orphan tx..");
            return;
        }
        List<TransactionNetPO> chainOrphan = chain.getOrphanList();
        if (chainOrphan.size() == 0) {
            return;
        }
        //Trading OrphanslistRetrieve all transactions and clear them；If there is anything that cannot be verified Add it back,Avoid blocking the addition of new orphan transactions
        List<TransactionNetPO> orphanTxList = new LinkedList<>();
        synchronized (chainOrphan) {
            orphanTxList.addAll(chainOrphan);
            chainOrphan.clear();
        }
        try {
            //Orphan sorting
            orphanSort.rank(orphanTxList);
            boolean flag = true;
            while (flag) {
                flag = process(orphanTxList);
            }
        } catch (RuntimeException e) {
            chain.getLogger().error("[OrphanTxProcessTask] RuntimeException:{}", e.getMessage());
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        } finally {
            if (orphanTxList.size() > 0) {
                synchronized (chainOrphan) {
                    chainOrphan.addAll(orphanTxList);
                    int size = chainOrphan.size();
                    chain.getLogger().debug("[OrphanTxProcessTask] OrphanTxList size:{}", size);
                }
            }
            chain.getLogger().debug("Processing completed, current total number of orphan transactionschainOrphan:{}", chainOrphan.size());
        }
    }

    private boolean process(List<TransactionNetPO> orphanTxList) {
        boolean flag = false;
        Iterator<TransactionNetPO> it = orphanTxList.iterator();
        while (it.hasNext()) {
            //Protocol upgrade,Terminate this processing
            if(chain.getProtocolUpgrade().get()){
                return false;
            }
            TransactionNetPO txNet = it.next();
            boolean rs = processOrphanTx(chain, txNet);
            if (rs) {
                chain.getOrphanListDataSize().addAndGet(Math.negateExact(txNet.getTx().size()));
                it.remove();
                //An orphan transaction has been processed
                flag = true;
            }
        }
        return flag;
    }

    /**
     * Handling orphan transactions
     *
     * @param chain
     * @param txNet
     * @return true     Indicates that it needs to be cleared from the orphan trading pool,1:Verified transactions,2：Transactions that have exceeded the time limit in the orphan pool,3：Verification of ledger failed(Abnormalities, etc)
     * false    Indicates that it still needs to be retained in the orphan trading pool(Failed verification)
     */
    private boolean processOrphanTx(Chain chain, TransactionNetPO txNet) {
        try {
            Transaction tx = txNet.getTx();
            int chainId = chain.getChainId();
            if (txService.isTxExists(chain, tx.getHash())) {
                return true;
            }
            //To be packaged queuemapExceeding the predetermined value,We will no longer accept transaction processing,Direct forwarding of complete transactions
            int packableTxMapDataSize = 0;
            for(Transaction transaction : chain.getPackableTxMap().values()){
                packableTxMapDataSize += transaction.size();
            }
            if(TxUtil.discardTx(chain, packableTxMapDataSize, tx)){
                //To be packaged queuemapExceeding the predetermined value, Do not handle forwarding failures
                String hash = tx.getHash().toHex();
                NetworkCall.broadcastTx(chain, tx, TxDuplicateRemoval.getExcludeNode(hash));
                return true;
            }
            VerifyLedgerResult verifyLedgerResult = LedgerCall.commitUnconfirmedTx(chain, RPCUtil.encode(tx.serialize()));
            if (verifyLedgerResult.businessSuccess()) {
                if (chain.getPackaging().get()) {
                    //When a node is a block out node, Only then will the transaction be placed in the queue to be packaged
                    packablePool.add(chain, tx);
                }
                unconfirmedTxStorageService.putTx(chainId, tx);
                //Forwarding transactionshash,Network transactions do not handle forwarding failures
                String hash = tx.getHash().toHex();
                NetworkCall.forwardTxHash(chain, tx.getHash(), TxDuplicateRemoval.getExcludeNode(hash));
                return true;
            }
            if (!verifyLedgerResult.getSuccess()) {
                //If the ledger verification returns an exception when processing orphan transactions, the transaction will be cleared directly
                chain.getLogger().error("[OrphanTxProcessTask] tx coinData verify fail - code:{}, type:{}, - txhash:{}",
                        verifyLedgerResult.getErrorCode() == null ? "" : verifyLedgerResult.getErrorCode().getCode(), tx.getType(), tx.getHash().toHex());
                return true;
            }
            long currentTimeSeconds = NulsDateUtils.getCurrentTimeSeconds();
            //If it is still an orphan transaction after the specified time, delete it
            boolean rs = tx.getTime() < (currentTimeSeconds - (chain.getConfig().getOrphanTtl()));
            return rs;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }


    private boolean orphanTxTask(Chain chain) throws NulsException {
        Map<String, Orphans> map = chain.getOrphanMap();

        Iterator<Map.Entry<String, Orphans>> it = map.entrySet().iterator();
        boolean rs = false;
        while (it.hasNext()) {
            Map.Entry<String, Orphans> entry = it.next();
            Orphans orphans = entry.getValue();

            boolean isRemove = false;
            //Process an orphan transaction string
            Orphans currentOrphan = orphans;
            while (null != currentOrphan) {
                if (processOrphanTx(chain, currentOrphan.getTx())) {
                    /**
                     * as long asmapThe orphan transaction in has been approved,Then frommapDelete this element from the middle,
                     * If the subsequent validation in the same string does not pass, it will be abandoned. If it can be explained in the same string that orphan will not be tried again, it will be discarded if it cannot be verified for other reasons,
                     * orphanmapThere is only one orphan string in the first oneOrphans
                     */
                    if (!isRemove) {
                        isRemove = true;
                    }
                    if (null != currentOrphan.getNext()) {
                        currentOrphan = currentOrphan.getNext();
                        continue;
                    }
                }
                currentOrphan = null;
            }
            if (isRemove) {
                it.remove();
                rs = true;
            }
        }
        int size = map.size();
        if (size > 0) {
            chain.getLogger().debug("** Number of orphan transaction strings：{} ", map.size());
        }
        return rs;
    }

}
