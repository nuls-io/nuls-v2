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

import io.nuls.base.data.NulsHash;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.model.ChainContext;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.block.thread.TxGroupTask;
import io.nuls.core.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import static io.nuls.block.constant.CommandConstant.GET_TXGROUP_MESSAGE;

/**
 * 区块广播过程中,获取本地没有的交易
 *
 * @author captain
 * @version 1.0
 * @date 19-3-28 下午3:54
 */
public class TxGroupRequestor extends BaseMonitor {

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
        delayQueueMap.values().forEach(e -> {
            TxGroupTask task = e.poll();
            if (task != null) {
                HashListMessage hashListMessage = task.getRequest();
                List<NulsHash> hashList = hashListMessage.getTxHashList();
                commonLog.debug("TxGroupRequestor send getTxgroupMessage, original hashList size-" + hashList.size());
                hashList = TransactionCall.filterUnconfirmedHash(chainId, hashList);
                commonLog.debug("TxGroupRequestor send getTxgroupMessage, filtered hashList size-" + hashList.size());
                hashListMessage.setTxHashList(hashList);
                boolean b = NetworkCall.sendToNode(chainId, hashListMessage, task.getNodeId(), GET_TXGROUP_MESSAGE);
                commonLog.debug("TxGroupRequestor send getTxgroupMessage to " + task.getNodeId() + ", result-" + b + ", chianId-" + chainId);
            }
        });
    }

}
