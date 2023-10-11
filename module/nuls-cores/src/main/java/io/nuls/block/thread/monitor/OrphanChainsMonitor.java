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
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.block.rpc.call.ConsensusCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.Deque;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.constant.Constant.MODULE_WORKING;
import static io.nuls.block.constant.StatusEnum.MAINTAIN_ORPHAN_CHAINS;
import static io.nuls.block.constant.StatusEnum.RUNNING;

/**
 * 孤儿链的形成原因分析：因为网络问题,在没有收到Block(100)的情况下,已经收到了Block(101),此时Block(101)不能连接到主链上,形成孤儿链
 * 孤儿链定时处理器
 * 孤儿链处理大致流程：
 * 1.清理无效数据
 * 2.标记
 * 3.复制、清除
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:54
 */
public class OrphanChainsMonitor extends BaseMonitor {

    private static final OrphanChainsMonitor INSTANCE = new OrphanChainsMonitor();

    public static OrphanChainsMonitor getInstance() {
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
                SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
                if (!lock.validate(stamp)) {
                    continue;
                }
                if (orphanChains.isEmpty()) {
                    break;
                }
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    continue;
                }
                // exclusive access
                context.printChains();
                context.setStatus(MAINTAIN_ORPHAN_CHAINS);
                Chain masterChain = BlockChainManager.getMasterChain(chainId);
                SortedSet<Chain> forkChains = BlockChainManager.getForkChains(chainId);
                //标记、变更链属性阶段
                for (Chain orphanChain : orphanChains) {
                    commonLog.debug("OrphanChainsMonitor-mark-begin");
                    mark(orphanChain, masterChain, forkChains, orphanChains);
                    commonLog.debug("OrphanChainsMonitor-mark-end");
                }
                //打印标记后孤儿链的类型
                for (Chain orphanChain : orphanChains) {
                    commonLog.debug(orphanChain.toString());
                }
                //复制、清除阶段
                SortedSet<Chain> maintainedOrphanChains = new TreeSet<>(Chain.COMPARATOR);
                for (Chain orphanChain : orphanChains) {
                    commonLog.debug("OrphanChainsMonitor-copy-begin");
                    copy(chainId, maintainedOrphanChains, orphanChain);
                    commonLog.debug("OrphanChainsMonitor-copy-end");
                }
                BlockChainManager.setOrphanChains(chainId, maintainedOrphanChains);
                forkChains.forEach(e -> e.setType(ChainTypeEnum.FORK));
                maintainedOrphanChains.forEach(e -> e.setType(ChainTypeEnum.ORPHAN));
                context.printChains();
                break;
            }
        } finally {
            context.setStatus(RUNNING);
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    /**
     * 孤儿链与其他链的关系可能是
     *  相连
     *  重复
     *  分叉
     *  无关
     * 四种关系
     *
     * @param orphanChain
     * @param masterChain
     * @param forkChains
     * @param orphanChains
     */
    private void mark(Chain orphanChain, Chain masterChain, SortedSet<Chain> forkChains, SortedSet<Chain> orphanChains) {
        try {
            //1.判断与主链是否相连
            if (orphanChain.getParent() == null && tryAppend(masterChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.MASTER_APPEND);
                return;
            }
            //2.判断是否与主链重复
            if (orphanChain.getParent() == null && tryDuplicate(masterChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.MASTER_DUPLICATE);
                return;
            }
            //3.判断是否从主链分叉
            if (orphanChain.getParent() == null && tryFork(masterChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.MASTER_FORK);
                return;
            }
            for (Chain forkChain : forkChains) {
                //4.判断与分叉链是否相连
                if (orphanChain.getParent() == null && tryAppend(forkChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.FORK_APPEND);
                    return;
                }
                //5.判断与分叉链是否重复
                if (orphanChain.getParent() == null && tryDuplicate(forkChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.FORK_DUPLICATE);
                    return;
                }
                //6.判断是否从分叉链分叉
                if (orphanChain.getParent() == null && tryFork(forkChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.FORK_FORK);
                    return;
                }
            }
            for (Chain anotherOrphanChain : orphanChains) {
                //排除自身
                if (anotherOrphanChain.equals(orphanChain)) {
                    continue;
                }
                //7.判断与孤儿链是否相连
                if (anotherOrphanChain.getParent() == null && tryAppend(orphanChain, anotherOrphanChain)) {
                    anotherOrphanChain.setType(ChainTypeEnum.ORPHAN_APPEND);
                    return;
                }
                if (orphanChain.getParent() == null && tryAppend(anotherOrphanChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.ORPHAN_APPEND);
                    return;
                }
                //8.判断与孤儿链是否重复
                if (anotherOrphanChain.getParent() == null && tryDuplicate(orphanChain, anotherOrphanChain)) {
                    anotherOrphanChain.setType(ChainTypeEnum.ORPHAN_DUPLICATE);
                    return;
                }
                if (orphanChain.getParent() == null && tryDuplicate(anotherOrphanChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.ORPHAN_DUPLICATE);
                    return;
                }
                //9.判断是否从孤儿链分叉
                if (anotherOrphanChain.getParent() == null && tryFork(orphanChain, anotherOrphanChain)) {
                    anotherOrphanChain.setType(ChainTypeEnum.ORPHAN_FORK);
                    return;
                }
                if (orphanChain.getParent() == null && tryFork(anotherOrphanChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.ORPHAN_FORK);
                    return;
                }
            }
        } catch (NulsRuntimeException e) {
            ContextManager.getContext(masterChain.getChainId()).getLogger().error("orphanChain data error-" + orphanChain);
            orphanChain.setType(ChainTypeEnum.DATA_ERROR);
            ConsensusCall.notice(masterChain.getChainId(), MODULE_WORKING);
            TransactionCall.notice(masterChain.getChainId(), MODULE_WORKING);
        }
    }

    private void copy(Integer chainId, SortedSet<Chain> maintainedOrphanChains, Chain orphanChain) {
        //如果标记为数据错误,orphanChain不会复制到新的孤儿链集合,也不会进入分叉链集合,所有orphanChain的直接子链移除父链引用,标记为ChainTypeEnum.ORPHAN,就是断开与数据错误的链的关联关系
        if (orphanChain.getType().equals(ChainTypeEnum.DATA_ERROR)) {
            orphanChain.getSons().forEach(e -> {e.setType(ChainTypeEnum.ORPHAN);e.setParent(null);});
            return;
        }
        //如果标记为主链重复,orphanChain不会复制到新的孤儿链集合,也不会进入分叉链集合,所有orphanChain的直接子链标记为ChainTypeEnum.MASTER_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.MASTER_DUPLICATE)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.MASTER_FORK));
            return;
        }
        //如果标记为分叉链重复,orphanChain不会复制到新的孤儿链集合,也不会进入分叉链集合,所有orphanChain的直接子链标记为ChainTypeEnum.FORK_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.FORK_DUPLICATE)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
            return;
        }
        //如果标记为孤儿链重复,orphanChain不会复制到新的孤儿链集合,也不会进入分叉链集合,所有orphanChain的直接子链标记为ChainTypeEnum.ORPHAN_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN_DUPLICATE)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.ORPHAN_FORK));
            return;
        }
        //如果标记为与主链相连,orphanChain不会复制到新的孤儿链集合,也不会进入分叉链集合,但是所有orphanChain的直接子链标记为ChainTypeEnum.MASTER_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.MASTER_APPEND)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.MASTER_FORK));
            return;
        }
        //如果标记为从主链分叉,orphanChain不会复制到新的孤儿链集合,但是会进入分叉链集合,所有orphanChain的直接子链标记为ChainTypeEnum.FORK_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.MASTER_FORK)) {
            BlockChainManager.addForkChain(chainId, orphanChain);
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
            return;
        }
        //如果标记为与分叉链相连,orphanChain不会复制到新的孤儿链集合,也不会进入分叉链集合,但是所有orphanChain的直接子链标记为ChainTypeEnum.FORK_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.FORK_APPEND)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
            return;
        }
        //如果标记为从分叉链分叉,orphanChain不会复制到新的孤儿链集合,但是会进入分叉链集合,所有orphanChain的直接子链标记为ChainTypeEnum.FORK_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.FORK_FORK)) {
            BlockChainManager.addForkChain(chainId, orphanChain);
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
            return;
        }
        //如果标记为与孤儿链相连,不会复制到新的孤儿链集合,所有orphanChain的直接子链会复制到新的孤儿链集合,类型不变
        if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN_APPEND)) {
            return;
        }
        //如果标记为与孤儿链分叉,会复制到新的孤儿链集合,所有orphanChain的直接子链会复制到新的孤儿链集合,类型不变
        if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN_FORK)) {
            maintainedOrphanChains.add(orphanChain);
            return;
        }
        //如果标记为孤儿链(未变化),或者从孤儿链分叉,复制到新的孤儿链集合
        if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN)) {
            maintainedOrphanChains.add(orphanChain);
        }
    }

    /**
     * 尝试把subChain链接到mainChain的末尾,形成mainChain-subChain的结构
     * 两个链相连成功,需要从孤儿链集合中删除subChain
     *
     * @param mainChain
     * @param subChain
     * @return
     */
    private boolean tryAppend(Chain mainChain, Chain subChain) {
        if (mainChain.getEndHeight() + 1 == subChain.getStartHeight() && mainChain.getEndHash().equals(subChain.getPreviousHash())) {
            return BlockChainManager.append(mainChain, subChain);
        }
        return false;
    }

    /**
     * 尝试把subChain分叉到mainChain上
     * 分叉不需要从链集合中删除分叉的链
     *
     * @param mainChain
     * @param subChain
     * @return
     */
    private boolean tryFork(Chain mainChain, Chain subChain) {
        if (mainChain.getHashList().contains(subChain.getPreviousHash())) {
            return BlockChainManager.fork(mainChain, subChain);
        }
        return false;
    }

    /**
     * 判断subChain是否与mainChain重复
     *
     * @param mainChain
     * @param subChain
     * @return
     */
    private boolean tryDuplicate(Chain mainChain, Chain subChain) {
        Deque<NulsHash> mainChainHashList = mainChain.getHashList();
        return mainChainHashList.contains(subChain.getEndHash()) && mainChainHashList.contains(subChain.getStartHash());
    }

}
