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
import io.nuls.common.ConfigBean;
import io.nuls.block.model.CheckResult;
import io.nuls.block.rpc.call.ConsensusCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.core.log.logback.NulsLogger;

import java.util.SortedSet;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.constant.Constant.MODULE_WAITING;
import static io.nuls.block.constant.Constant.MODULE_WORKING;

/**
 * Analysis of the formation reasons of forked chains:Due to network latency,Two miners simultaneously release blocks of the same height,Or attacked by malicious nodes
 * Forked chain timing processor,If a forked chain is found to be longer than the main chain,Need to switch the fork chain to the main chain
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 afternoon3:54
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
        StatusEnum status = StatusEnum.RUNNING;
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
                context.printChains();
                //Traverse the current forked chain,Compare with the main chain,Find the maximum height difference,Same as default parameterschainSwtichThresholdcontrast,Determine the fork chain to switch between
                Chain masterChain = BlockChainManager.getMasterChain(chainId);
                ConfigBean parameters = context.getParameters();
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
                //Insufficient height difference
                if (maxHeightDifference < chainSwtichThreshold) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                //Switch,Change module operation status before switching
                context.setStatus(StatusEnum.SWITCHING);
                ConsensusCall.notice(chainId, MODULE_WAITING);
                TransactionCall.notice(chainId, MODULE_WAITING);
                CheckResult checkResult = BlockChainManager.switchChain(chainId, masterChain, switchChain);
                if (checkResult.isResult()) {
                    commonLog.info("chainId-" + chainId + ", switchChain success");
                } else if (checkResult.isTimeout()) {
                    status = StatusEnum.WAITING;
                    break;
                } else {
                    commonLog.info("chainId-" + chainId + ", switchChain fail, auto rollback success");
                }
                context.printChains();
                ConsensusCall.notice(chainId, MODULE_WORKING);
                TransactionCall.notice(chainId, MODULE_WORKING);
                break;
            }
        } finally {
            context.setStatus(status);
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

}
