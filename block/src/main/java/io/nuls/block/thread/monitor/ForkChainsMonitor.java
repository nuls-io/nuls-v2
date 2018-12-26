/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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

import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.tools.log.Log;

import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.constant.RunningStatusEnum.MAINTAIN_CHAINS;

/**
 * 分叉链的形成原因分析:由于网络延迟,同时有两个矿工发布同一高度的区块,或者被恶意节点攻击
 * 分叉链定时处理器,如果发现某分叉链比主链更长,需要切换该分叉链为主链
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class ForkChainsMonitor implements Runnable {

    private static final ForkChainsMonitor INSTANCE = new ForkChainsMonitor();

    private ForkChainsMonitor() {

    }

    public static ForkChainsMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        for (Integer chainId : ContextManager.chainIds) {
            ChainContext context = ContextManager.getContext(chainId);
            try {
                //判断该链的运行状态,只有正常运行时才会有分叉链的处理
                RunningStatusEnum status = context.getStatus();
                if (!status.equals(RunningStatusEnum.RUNNING)) {
                    Log.info("skip process, status is {}, chainId-{}", status, chainId);
                    continue;
                }
                ReentrantReadWriteLock.ReadLock readLock = context.getReadLock();
                if (readLock.tryLock(1, TimeUnit.SECONDS)) {
                    Chain masterChain = ChainManager.getMasterChain(chainId);
                    SortedSet<Chain> forkChains = ChainManager.getForkChains(chainId);
                    if (forkChains.size() < 1) {
                        readLock.unlock();
                        continue;
                    }
                    //遍历当前分叉链,与主链进行比对,找出最大高度差,与默认参数chainSwtichThreshold对比,确定要切换的分叉链
                    int chainSwtichThreshold = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.CHAIN_SWTICH_THRESHOLD));
                    Chain switchChain = new Chain();
                    int maxHeightDifference = 0;
                    for (Chain forkChain : forkChains) {
                        int temp = (int) (forkChain.getEndHeight() - masterChain.getEndHeight());
                        if (temp > maxHeightDifference) {
                            maxHeightDifference = temp;
                            switchChain = forkChain;
                        }
                    }
                    Log.debug("chainId-{}, maxHeightDifference:{}, chainSwtichThreshold:{}", chainId, maxHeightDifference, chainSwtichThreshold);
                    //高度差不够
                    if (maxHeightDifference < chainSwtichThreshold) {
                        readLock.unlock();
                        continue;
                    }
                    readLock.unlock();
                    ReentrantReadWriteLock.WriteLock writeLock = context.getWriteLock();
                    if (writeLock.tryLock(1, TimeUnit.SECONDS)) {
                        //进行切换,切换前变更模块运行状态
                        context.setStatus(RunningStatusEnum.SWITCHING);
                        if (ChainManager.switchChain(chainId, masterChain, switchChain)) {
                            Log.info("chainId-{}, switchChain success", chainId);
                        } else {
                            Log.info("chainId-{}, switchChain fail, auto rollback success", chainId);
                        }
                        context.setStatus(RunningStatusEnum.RUNNING);
                        writeLock.unlock();
                    }
                }
            } catch (Exception e) {
                Log.info("chainId-{}, switchChain fail, , auto rollback fail", chainId);
                Log.error(e);
                context.setStatus(RunningStatusEnum.EXCEPTION);
            }
        }
    }

}
