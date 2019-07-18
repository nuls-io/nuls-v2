/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.thread.monitor;

import io.nuls.base.data.*;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.model.ChainContext;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.block.service.BlockService;
import io.nuls.block.thread.TxGroupTask;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.logback.NulsLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.stream.Collectors;

import static io.nuls.block.constant.CommandConstant.GET_TXGROUP_MESSAGE;

/**
 * 区块广播过程中,获取本地没有的交易
 *
 * @author captain
 * @version 1.0
 * @date 19-3-28 下午3:54
 */
public class TxGroupRequestor extends BaseMonitor {

    private BlockService blockService;

    private TxGroupRequestor() {
        blockService = SpringLiteContext.getBean(BlockService.class);
    }

    private static Map<Integer, Map<String, DelayQueue<TxGroupTask>>> map = new HashMap<>();

    private static final TxGroupRequestor INSTANCE = new TxGroupRequestor();

    public static TxGroupRequestor getInstance() {
        return INSTANCE;
    }

    public static void init(int chainId) {
        Map<String, DelayQueue<TxGroupTask>> cMap = new ConcurrentHashMap<>(4);
        map.put(chainId, cMap);
    }

    public static void addTask(int chainId, String hash, TxGroupTask task) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        DelayQueue<TxGroupTask> txGroupTasks = map.get(chainId).get(hash);
        if (txGroupTasks == null) {
            txGroupTasks = new DelayQueue<>();
            map.get(chainId).put(hash, txGroupTasks);
        }
        boolean add = txGroupTasks.add(task);
        commonLog.debug("TxGroupRequestor add TxGroupTask, hash-" + hash + ", task-" + task + ", result-" + add + ", chianId-" + chainId);
    }

    public static void removeTask(int chainId, String hash) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        DelayQueue<TxGroupTask> remove = map.get(chainId).remove(hash);
        commonLog.debug("TxGroupRequestor remove TxGroupTask, hash-" + hash + ", size-" + (remove == null ? 0 : remove.size()) + ", chianId-" + chainId);
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger commonLog) {
        Map<String, DelayQueue<TxGroupTask>> delayQueueMap = map.get(chainId);
        List<String> del = new ArrayList<>();
        for (Map.Entry<String, DelayQueue<TxGroupTask>> entry : delayQueueMap.entrySet()) {
            String blockHash = entry.getKey();
            TxGroupTask task = entry.getValue().poll();
            if (task != null) {
                HashListMessage hashListMessage = task.getRequest();
                List<NulsHash> hashList = hashListMessage.getTxHashList();
                int original = hashList.size();
                commonLog.debug("TxGroupRequestor send getTxgroupMessage, original hashList size-" + original + ", blockHash-" + blockHash);
                List<Transaction> existTransactions = TransactionCall.getTransactions(chainId, hashList, false);
                List<NulsHash> existHashes = existTransactions.stream().map(Transaction::getHash).collect(Collectors.toList());
//                hashList = TransactionCall.filterUnconfirmedHash(chainId, hashList);
                hashList.removeAll(existHashes);
                int filtered = hashList.size();
                commonLog.debug("TxGroupRequestor send getTxgroupMessage, filtered hashList size-" + filtered + ", blockHash-" + blockHash);
                //
                if (filtered == 0) {
                    CachedSmallBlock cachedSmallBlock = SmallBlockCacher.getCachedSmallBlock(chainId, NulsHash.fromHex(blockHash));
                    SmallBlock smallBlock = cachedSmallBlock.getSmallBlock();
                    if (null == smallBlock) {
                        return;
                    }

                    BlockHeader header = smallBlock.getHeader();
                    Map<NulsHash, Transaction> txMap = cachedSmallBlock.getTxMap();
                    for (Transaction tx : existTransactions) {
                        txMap.put(tx.getHash(), tx);
                    }

                    Block block = BlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
                    blockService.saveBlock(chainId, block, 1, true, false, true);
                    del.add(blockHash);
                    continue;
                }
                hashListMessage.setTxHashList(hashList);
                if (original != filtered) {
                    entry.getValue().forEach(e -> e.setRequest(hashListMessage));
                    Map<NulsHash, Transaction> map = SmallBlockCacher.getCachedSmallBlock(chainId, NulsHash.fromHex(blockHash)).getTxMap();
                    existTransactions.forEach(e -> map.put(e.getHash(), e));
                }
                boolean b = NetworkCall.sendToNode(chainId, hashListMessage, task.getNodeId(), GET_TXGROUP_MESSAGE);
                commonLog.debug("TxGroupRequestor send getTxgroupMessage to " + task.getNodeId() + ", result-" + b + ", chianId-" + chainId + ", blockHash-" + blockHash);
            }
        }
        del.forEach(delayQueueMap::remove);
    }

}
