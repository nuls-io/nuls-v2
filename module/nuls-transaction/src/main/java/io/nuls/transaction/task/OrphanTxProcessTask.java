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
import io.nuls.transaction.utils.TransactionComparator;
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

    private TransactionComparator txComparator = SpringLiteContext.getBean(TransactionComparator.class);

    public OrphanTxProcessTask(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        try {
            doOrphanTxTask(chain);
//            boolean run = true;
//            while (run){
//                run = orphanTxTask(chain);
//            }
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
//            LOG.debug("执行处理孤儿交易Task，孤儿数为：0");
            return;
        }
//        LOG.debug("开始处理孤儿交易，-当前孤儿交易总数:{}", chainOrphan.size());
        //把孤儿交易list的交易全部取出来，然后清空；如果有验不过的 再加回去,避免阻塞新的孤儿交易的加入
        List<TransactionNetPO> orphanTxList = new LinkedList<>();
        synchronized (chainOrphan) {
            orphanTxList.addAll(chainOrphan);
            chainOrphan.clear();
        }
        try {
            //时间排序TransactionTimeComparator
            orphanTxList.sort(txComparator);
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
            //todo 测试
//            chain.getLogger().debug("[OrphanTxProcessTask] OrphanTxList size:{}", orphanTxList.size());
            chain.getLogger().debug("处理完成，当前孤儿交易总数chainOrphan:{}", chainOrphan.size());
        }
    }

    private boolean process(List<TransactionNetPO> orphanTxList) {
        boolean flag = false;
        Iterator<TransactionNetPO> it = orphanTxList.iterator();
        while (it.hasNext()) {
            //协议升级,终止此次处理
            if(chain.getProtocolUpgrade().get()){
                return false;
            }
            TransactionNetPO txNet = it.next();
            boolean rs = processOrphanTx(chain, txNet);
            if (rs) {
                StatisticsTask.orphanTxTotal.incrementAndGet();
                it.remove();
                //有孤儿交易被处理
                flag = true;
            }
        }
        return flag;
    }


    /**
     * 处理孤儿交易
     *
     * @param chain
     * @param txNet
     * @return true     表示该需要从孤儿交易池中清理掉，1:验证通过的交易，2：在孤儿池中超时的交易，3：验证账本失败(异常等)
     * false    表示仍然需要保留在孤儿交易池中(没有验证通过)
     */
    private boolean processOrphanTx(Chain chain, TransactionNetPO txNet) {
        try {
            Transaction tx = txNet.getTx();
            int chainId = chain.getChainId();
            if (txService.isTxExists(chain, tx.getHash())) {
                return true;
            }
            //待打包队列map超过预定值,则不再接受处理交易,直接转发交易完整交易
            int packableTxMapSize = chain.getPackableTxMap().size();
            if(TxUtil.discardTx(packableTxMapSize)){
                //待打包队列map超过预定值, 不处理转发失败的情况
                NetworkCall.broadcastTx(chain, tx, txNet.getExcludeNode());
                return true;
            }
            VerifyLedgerResult verifyLedgerResult = LedgerCall.commitUnconfirmedTx(chain, RPCUtil.encode(tx.serialize()));
            if (verifyLedgerResult.businessSuccess()) {
                if (chain.getPackaging().get()) {
                    //当节点是出块节点时, 才将交易放入待打包队列
                    packablePool.add(chain, tx);
                //chain.getLogger().debug("[OrphanTxProcessTask] 加入待打包队列....hash:{}", tx.getHash().toHex());
                }
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chainId, tx, txNet.getOriginalSendNanoTime());
                //转发交易hash,网络交易不处理转发失败的情况
                NetworkCall.forwardTxHash(chain, tx.getHash(), txNet.getExcludeNode());
                return true;
            }
           /* if(!verifyLedgerResult.isOrphan()) {
                chain.getLogger().error("[OrphanTxProcessTask] tx coinData verify fail - orphan: {}, - code:{}, type:{}, - txhash:{}", verifyLedgerResult.getOrphan(),
                        verifyLedgerResult.getErrorCode() == null ? "" : verifyLedgerResult.getErrorCode().getCode(), tx.getType(), tx.getHash().toHex());
            }*/
            if (!verifyLedgerResult.getSuccess()) {
                //如果处理孤儿交易时，账本验证返回异常，则直接清理该交易
                chain.getLogger().error("[OrphanTxProcessTask] tx coinData verify fail - code:{}, type:{}, - txhash:{}",
                        verifyLedgerResult.getErrorCode() == null ? "" : verifyLedgerResult.getErrorCode().getCode(), tx.getType(), tx.getHash().toHex());
                return true;
            }
            long currentTimeSeconds = NulsDateUtils.getCurrentTimeSeconds();
            //超过指定时间仍旧是孤儿交易，则删除
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
            //处理一个孤儿交易串
            Orphans currentOrphan = orphans;
            while (null != currentOrphan) {
                if (processOrphanTx(chain, currentOrphan.getTx())) {
                    /**
                     * 只要map中的孤儿交易通过了,则从map中删除该元素,
                     * 同一个串中后续没有验证通过的则放弃，能在一个串中说明不会再试孤儿，其他原因验不过的则丢弃,
                     * 孤儿map中只存有一个孤儿串的第一个Orphans
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
            chain.getLogger().debug("** 孤儿交易串数量：{} ", map.size());
        }
        return rs;
    }

}
