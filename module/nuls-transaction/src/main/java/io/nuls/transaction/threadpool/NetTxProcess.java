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

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.Orphans;
import io.nuls.transaction.model.bo.VerifyResult;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Charlie
 * @date: 2019/5/5
 */
@Component
public class NetTxProcess {

    @Autowired
    private PackablePool packablePool;
    @Autowired
    private TxService txService;
    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    private ExecutorService verifyExecutor = ThreadUtils.createThreadPool(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, new NulsThreadFactory(TxConstant.VERIFY_TX_THREAD));

    public static AtomicInteger netTxToPackablePoolCount = new AtomicInteger(0);

    /**
     * 处理新交易
     *
     * @throws RuntimeException
     */
    public void process(Chain chain) throws RuntimeException {
        if (chain.getProtocolUpgrade().get()) {
            chain.getLogger().info("Protocol upgrade pause process new tx..");
        }
        if (chain.getTxNetProcessList().isEmpty()) {
            return;
        }
        Map<String, TransactionNetPO> txNetMap = null;
        List<Transaction> txList = null;
        List<Future<String>> futures = null;
        try {
            txNetMap = new HashMap<>(TxConstant.NET_TX_PROCESS_NUMBER_ONCE);
            txList = new LinkedList<>();
            futures = new ArrayList<>();
            for (TransactionNetPO txNet : chain.getTxNetProcessList()) {
                Transaction tx = txNet.getTx();
                String hashStr = tx.getHash().toHex();
                //多线程处理单个交易
                Future<String> res = verifyExecutor.submit(new Callable<String>() {
                    @Override
                    public String call() {
                        /**if(txService.isTxExists(chain, tx.getHash())){
                         return false;
                         }*/
                        VerifyResult verifyResult = txService.verify(chain, tx);
                        if (!verifyResult.getResult()) {
                            chain.getLogger().error("Net new tx verify fail..errorCode:{}...hash:{}",
                                    verifyResult.getErrorCode().getCode(), hashStr);
                            return hashStr;
                        }
                        return null;
                    }
                });
                futures.add(res);
                txList.add(tx);
                txNetMap.put(hashStr, txNet);
            }
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
        } finally {
            chain.getTxNetProcessList().clear();
        }

        List<String> txFailList = new LinkedList<>();
        //多线程处理结果
        try {
            for (Future<String> future : futures) {
                if (null != future.get()) {
                    txFailList.add(future.get());
                }
            }
        } catch (InterruptedException e) {
            chain.getLogger().error(e);
            return;
        } catch (ExecutionException e) {
            chain.getLogger().error(e);
            return;
        }
        //有验证不通过的，则过滤掉
        if (!txFailList.isEmpty()) {
            Iterator<Transaction> it = txList.iterator();
            while (it.hasNext()) {
                Transaction tx = it.next();
                for (String hash : txFailList) {
                    if (hash.equals(tx.getHash().toHex())) {
                        it.remove();
                    }
                }
            }
        }

        if (txList.isEmpty()) {
            return;
        }
        try {
            verifyCoinData(chain, txList, txNetMap);
            for (Transaction tx : txList) {
                if (chain.getPackaging().get()) {
                    //当节点是出块节点时, 才将交易放入待打包队列
                    packablePool.add(chain, tx);
                    netTxToPackablePoolCount.incrementAndGet();
                }
                NulsHash hash = tx.getHash();
                //转发交易hash
                TransactionNetPO txNetPo = txNetMap.get(hash.toHex());
                //保存到rocksdb
                unconfirmedTxStorageService.putTx(chain.getChainId(), tx, txNetPo.getOriginalSendNanoTime());


                NetworkCall.forwardTxHash(chain, hash, txNetPo.getExcludeNode());
                //chain.getLoggerMap().get(TxConstant.LOG_NEW_TX_PROCESS).debug("NEW TX count:{} - hash:{}", ++count, hash.toHex());
            }
        } catch (NulsException e) {
            chain.getLogger().error("Net new tx process exception, -code:{}", e.getErrorCode().getCode());
        }
    }


    public void verifyCoinData(Chain chain, List<Transaction> txList, Map<String, TransactionNetPO> txNetMap) throws NulsException {
        try {
            Map verifyCoinDataResult = LedgerCall.commitBatchUnconfirmedTxs(chain, txList);
            List<String> failHashs = (List<String>) verifyCoinDataResult.get("fail");
            List<String> orphanHashs = (List<String>) verifyCoinDataResult.get("orphan");
            Iterator<Transaction> it = txList.iterator();
            removeAndGo:
            while (it.hasNext()) {
                Transaction tx = it.next();
                //去除账本验证失败的交易
                for (String hash : failHashs) {
                    String hashStr = tx.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        chain.getLogger().error("Net new tx coinData verify fail, - type:{}, - txhash:{}",
                                tx.getType(), hashStr);
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
                            chainOrphan.add(txNetMap.get(hash));
                        }
                        chain.getLogger().debug("Net new tx coinData orphan, - type:{}, - txhash:{}",
                                tx.getType(), hashStr);
//                        long s1 = System.nanoTime();
//                        processOrphanTx(chain, txNetMap.get(hash));
//                        chain.getLogger().debug("Net new tx coinData orphan, -pTime:{} - type:{}, - txhash:{}",
//                                System.nanoTime() - s1 , tx.getType(), hashStr);
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

    private void processOrphanTx(Chain chain, TransactionNetPO txNet) {
        Map<String, Orphans> map = chain.getOrphanMap();
        if (map.isEmpty()) {
            Orphans orphans = new Orphans(txNet);
            map.put(txNet.getTx().getHash().toHex(), orphans);
            return;
        }
        /**
         * 遍历集合
         * 1.能不能连上每一个的前面
         * 2.能不能连上最后一个的后面
         */
        Orphans orphansNew = null;
        for (Orphans orphans : map.values()) {
            orphansNew = joinOrphans(orphans, txNet);
            if (null == orphansNew) {
                break;
            }
            if (null != orphansNew && null != orphansNew.getNext()) {
                break;
            }
        }
        if (null != orphansNew) {
            //加入新的孤儿
            map.put(orphansNew.getTx().getTx().getHash().toHex(), orphansNew);
            //如果该孤儿的下一笔,在map中存在，则从map中删除
            Orphans next = orphansNew.getNext();
            if (null != next) {
                //能连上前面
                String nextHash = next.getTx().getTx().getHash().toHex();
                if (map.containsKey(nextHash)) {
                    map.remove(nextHash);
                }
            }
        }

//        for(Orphans orphans : map.values()){;
//            Orphans tempOrphans = orphans;
//            while (null != tempOrphans) {
//                System.out.println(tempOrphans.getTx().getTx().getHash().toHex());
//                try {
//                    CoinData coinData = tempOrphans.getTx().getTx().getCoinDataInstance();
//                    System.out.println(HexUtil.encode(coinData.getFrom().get(0).getNonce()));
//                } catch (NulsException e) {
//                    e.printStackTrace();
//                }
//                tempOrphans = tempOrphans.getNext();
//            }
//        }
        chain.getLogger().debug("新加入孤儿交易 - 孤儿交易串数量：{} ", map.size());
    }


    /**
     * 1.能连在某个孤儿交易串的尾部，则直接放入该orphans对象中，返回null
     * 2.能连在map中某个孤儿交易的前面,或者不能连上任何已存在的孤儿交易，
     * 则返回一个新增orphans对象(后续加入map中)
     *
     * @param orphans
     * @param txNet
     * @return
     */
    private Orphans joinOrphans(Orphans orphans, TransactionNetPO txNet) {
        //新孤儿txNet应该连在orphans的前面
        if (orphans.isNextTx(txNet)) {
            return new Orphans(txNet, orphans);
        } else if (orphans.isPrevTx(txNet)) {
            //新孤儿txNet应该连在orphans的后面
            orphans.setNext(new Orphans(txNet));
            return null;
        } else if (null != orphans.getNext()) {
            return joinOrphans(orphans.getNext(), txNet);
        } else {
            //都连不上
            return new Orphans(txNet);
        }
    }
}
