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
 * Analysis of the Causes of Orphan Chain Formation：Due to network issues,I haven't received it yetBlock(100)In the case of,I have received itBlock(101),hereBlock(101)Unable to connect to the main chain,Forming an orphan chain
 * Orphan chain timing processor
 * The general process of handling orphan chains：
 * 1.Clean up invalid data
 * 2.sign
 * 3.copy、clean up
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 afternoon3:54
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
                //sign、Change Chain Attribute Stage
                for (Chain orphanChain : orphanChains) {
                    commonLog.debug("OrphanChainsMonitor-mark-begin");
                    mark(orphanChain, masterChain, forkChains, orphanChains);
                    commonLog.debug("OrphanChainsMonitor-mark-end");
                }
                //Print the type of orphan chain after marking
                for (Chain orphanChain : orphanChains) {
                    commonLog.debug(orphanChain.toString());
                }
                //copy、Clear Phase
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
     * The relationship between orphan chains and other chains may be
     *  Connected
     *  repeat
     *  Fork
     *  independence
     * Four types of relationships
     *
     * @param orphanChain
     * @param masterChain
     * @param forkChains
     * @param orphanChains
     */
    private void mark(Chain orphanChain, Chain masterChain, SortedSet<Chain> forkChains, SortedSet<Chain> orphanChains) {
        try {
            //1.Determine if it is connected to the main chain
            if (orphanChain.getParent() == null && tryAppend(masterChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.MASTER_APPEND);
                return;
            }
            //2.Determine if it is duplicated with the main chain
            if (orphanChain.getParent() == null && tryDuplicate(masterChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.MASTER_DUPLICATE);
                return;
            }
            //3.Determine whether to fork from the main chain
            if (orphanChain.getParent() == null && tryFork(masterChain, orphanChain)) {
                orphanChain.setType(ChainTypeEnum.MASTER_FORK);
                return;
            }
            for (Chain forkChain : forkChains) {
                //4.Determine if the fork chain is connected
                if (orphanChain.getParent() == null && tryAppend(forkChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.FORK_APPEND);
                    return;
                }
                //5.Determine if the forked chain is duplicated
                if (orphanChain.getParent() == null && tryDuplicate(forkChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.FORK_DUPLICATE);
                    return;
                }
                //6.Determine whether to fork from the fork chain
                if (orphanChain.getParent() == null && tryFork(forkChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.FORK_FORK);
                    return;
                }
            }
            for (Chain anotherOrphanChain : orphanChains) {
                //Excluding oneself
                if (anotherOrphanChain.equals(orphanChain)) {
                    continue;
                }
                //7.Determine if it is connected to the orphan chain
                if (anotherOrphanChain.getParent() == null && tryAppend(orphanChain, anotherOrphanChain)) {
                    anotherOrphanChain.setType(ChainTypeEnum.ORPHAN_APPEND);
                    return;
                }
                if (orphanChain.getParent() == null && tryAppend(anotherOrphanChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.ORPHAN_APPEND);
                    return;
                }
                //8.Determine if it is duplicated with the orphan chain
                if (anotherOrphanChain.getParent() == null && tryDuplicate(orphanChain, anotherOrphanChain)) {
                    anotherOrphanChain.setType(ChainTypeEnum.ORPHAN_DUPLICATE);
                    return;
                }
                if (orphanChain.getParent() == null && tryDuplicate(anotherOrphanChain, orphanChain)) {
                    orphanChain.setType(ChainTypeEnum.ORPHAN_DUPLICATE);
                    return;
                }
                //9.Determine whether to fork from the orphan chain
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
        //If marked as data error,orphanChainWill not copy to a new collection of orphan chains,Nor will it enter the set of forked chains,AllorphanChainRemove parent chain reference from direct child chain of,Mark asChainTypeEnum.ORPHAN,It's about breaking the association between the chain and the data error
        if (orphanChain.getType().equals(ChainTypeEnum.DATA_ERROR)) {
            orphanChain.getSons().forEach(e -> {e.setType(ChainTypeEnum.ORPHAN);e.setParent(null);});
            return;
        }
        //If marked as duplicate main chain,orphanChainWill not copy to a new collection of orphan chains,Nor will it enter the set of forked chains,AllorphanChainThe direct sub chain of is marked asChainTypeEnum.MASTER_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.MASTER_DUPLICATE)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.MASTER_FORK));
            return;
        }
        //If marked as duplicate forked chains,orphanChainWill not copy to a new collection of orphan chains,Nor will it enter the set of forked chains,AllorphanChainThe direct sub chain of is marked asChainTypeEnum.FORK_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.FORK_DUPLICATE)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
            return;
        }
        //If marked as duplicate orphan chains,orphanChainWill not copy to a new collection of orphan chains,Nor will it enter the set of forked chains,AllorphanChainThe direct sub chain of is marked asChainTypeEnum.ORPHAN_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN_DUPLICATE)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.ORPHAN_FORK));
            return;
        }
        //If marked as connected to the main chain,orphanChainWill not copy to a new collection of orphan chains,Nor will it enter the set of forked chains,But all of itorphanChainThe direct sub chain of is marked asChainTypeEnum.MASTER_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.MASTER_APPEND)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.MASTER_FORK));
            return;
        }
        //If marked as forking from the main chain,orphanChainWill not copy to a new collection of orphan chains,But it will enter the set of forked chains,AllorphanChainThe direct sub chain of is marked asChainTypeEnum.FORK_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.MASTER_FORK)) {
            BlockChainManager.addForkChain(chainId, orphanChain);
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
            return;
        }
        //If marked as connected to a forked chain,orphanChainWill not copy to a new collection of orphan chains,Nor will it enter the set of forked chains,But all of itorphanChainThe direct sub chain of is marked asChainTypeEnum.FORK_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.FORK_APPEND)) {
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
            return;
        }
        //If marked as forking from a forked chain,orphanChainWill not copy to a new collection of orphan chains,But it will enter the set of forked chains,AllorphanChainThe direct sub chain of is marked asChainTypeEnum.FORK_FORK
        if (orphanChain.getType().equals(ChainTypeEnum.FORK_FORK)) {
            BlockChainManager.addForkChain(chainId, orphanChain);
            orphanChain.getSons().forEach(e -> e.setType(ChainTypeEnum.FORK_FORK));
            return;
        }
        //If marked as connected to an orphan chain,Will not copy to a new collection of orphan chains,AllorphanChainThe direct child chains will be copied to the new collection of orphan chains,Type unchanged
        if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN_APPEND)) {
            return;
        }
        //If marked as forking with orphan chain,Will be copied to a new collection of orphan chains,AllorphanChainThe direct child chains will be copied to the new collection of orphan chains,Type unchanged
        if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN_FORK)) {
            maintainedOrphanChains.add(orphanChain);
            return;
        }
        //If marked as an orphan chain(Unchanged),Or fork from the orphan chain,Copy to a new collection of orphan chains
        if (orphanChain.getType().equals(ChainTypeEnum.ORPHAN)) {
            maintainedOrphanChains.add(orphanChain);
        }
    }

    /**
     * Try tosubChainlink tomainChainAt the end of,formationmainChain-subChainStructure of
     * Two chains successfully connected,Need to remove from orphan chain collectionsubChain
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
     * Try tosubChainFork tomainChainupper
     * Forking does not require removing forked chains from the chain set
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
     * judgesubChainIs it related tomainChainrepeat
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
