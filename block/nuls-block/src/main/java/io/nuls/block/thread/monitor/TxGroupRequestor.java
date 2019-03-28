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

import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.rpc.call.ConsensusUtil;
import io.nuls.block.rpc.call.NetworkUtil;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.thread.TxGroupTask;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.tools.thread.ThreadUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import static io.nuls.block.constant.CommandConstant.GET_TXGROUP_MESSAGE;
import static io.nuls.block.constant.Constant.CONSENSUS_WAITING;

/**
 * 区块广播过程中，获取本地没有的交易
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:53
 */
public class TxGroupRequestor implements Runnable {

    private static Map<Integer, Map<String, DelayQueue<TxGroupTask>>> map = new HashMap<>();

    private static final TxGroupRequestor INSTANCE = new TxGroupRequestor();

    private TxGroupRequestor() {

    }

    public static TxGroupRequestor getInstance() {
        return INSTANCE;
    }

    private void init(int chainId) {
        Map<String, DelayQueue<TxGroupTask>> cMap = new ConcurrentHashMap<>(4);
        map.put(chainId, cMap);
    }

    public static void addTask(int chainId, String hash, TxGroupTask task) {
        map.get(chainId).get(hash).add(task);
    }

    public static void removeTask(int chainId, String hash) {
        map.get(chainId).remove(hash).clear();
    }

    @Override
    public void run() {
        for (int chainId : ContextManager.chainIds) {
            ChainContext context = ContextManager.getContext(chainId);
            NulsLogger commonLog = context.getCommonLog();
            try {
                RunningStatusEnum status = context.getStatus();
                if (!status.equals(RunningStatusEnum.RUNNING)) {
                    commonLog.debug("skip process, status is " + status + ", chainId-" + chainId);
                    return;
                }
                ChainParameters parameters = context.getParameters();
                Map<String, DelayQueue<TxGroupTask>> delayQueueMap = map.get(chainId);
                delayQueueMap.values().forEach(e -> {
                    TxGroupTask task = e.poll();
                    if (task != null) {
                        NetworkUtil.sendToNode(chainId, task.getRequest(), task.getNodeId(), GET_TXGROUP_MESSAGE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                commonLog.error("chainId-" + chainId + ",NetworkReset error!");
            }
        }
    }

}
