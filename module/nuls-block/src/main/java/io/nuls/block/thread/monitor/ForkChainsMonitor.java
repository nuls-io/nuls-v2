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

import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.rpc.call.ConsensusCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.core.log.logback.NulsLogger;

import java.util.SortedSet;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.constant.Constant.MODULE_WAITING;
import static io.nuls.block.constant.Constant.MODULE_WORKING;

/**
 * 分叉链的形成原因分析:由于网络延迟,同时有两个矿工发布同一高度的区块,或者被恶意节点攻击
 * 分叉链定时处理器,如果发现某分叉链比主链更长,需要切换该分叉链为主链
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class ForkChainsMonitor extends BaseMonitor {

    private static final ForkChainsMonitor INSTANCE = new ForkChainsMonitor();

    public static ForkChainsMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger commonLog) {
        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.writeLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                SortedSet<Chain> forkChains = BlockChainManager.getForkChains(chainId);
                if (!lock.validate(stamp)) {
                    continue;
                }
                if (forkChains.isEmpty()) {
                    break;
                }
                commonLog.info("####################################fork chains######################################");
                for (Chain forkChain : forkChains) {
                    commonLog.info("#" + forkChain);
                }
                //遍历当前分叉链,与主链进行比对,找出最大高度差,与默认参数chainSwtichThreshold对比,确定要切换的分叉链
                Chain masterChain = BlockChainManager.getMasterChain(chainId);
                ChainParameters parameters = context.getParameters();
                int chainSwtichThreshold = parameters.getChainSwtichThreshold();
                Chain switchChain = new Chain();
                int maxHeightDifference = 0;
                for (Chain forkChain : forkChains) {
                    int temp = (int) (forkChain.getEndHeight() - masterChain.getEndHeight());
                    if (temp > maxHeightDifference) {
                        maxHeightDifference = temp;
                        switchChain = forkChain;
                    }
                }
                commonLog.debug("chainId-" + chainId + ", maxHeightDifference:" + maxHeightDifference + ", chainSwtichThreshold:" + chainSwtichThreshold);
                //高度差不够
                if (maxHeightDifference < chainSwtichThreshold) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                //进行切换,切换前变更模块运行状态
                context.setStatus(StatusEnum.SWITCHING);
                ConsensusCall.notice(chainId, MODULE_WAITING);
                TransactionCall.notice(chainId, MODULE_WAITING);
                if (BlockChainManager.switchChain(chainId, masterChain, switchChain)) {
                    commonLog.info("chainId-" + chainId + ", switchChain success");
                } else {
                    commonLog.info("chainId-" + chainId + ", switchChain fail, auto rollback success");
                }
                ConsensusCall.notice(chainId, MODULE_WORKING);
                TransactionCall.notice(chainId, MODULE_WORKING);
                break;
            }
        } finally {
            context.setStatus(StatusEnum.RUNNING);
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

}
