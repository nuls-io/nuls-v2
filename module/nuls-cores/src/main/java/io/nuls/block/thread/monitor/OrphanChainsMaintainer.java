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

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.common.ConfigBean;
import io.nuls.block.model.Node;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.storage.ChainStorageService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.logback.NulsLogger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

import static io.nuls.block.constant.StatusEnum.RUNNING;
import static io.nuls.block.constant.StatusEnum.UPDATE_ORPHAN_CHAINS;

/**
 * Analysis of the Causes of Orphan Chain Formation：Due to network issues,I haven't received it yetBlock(100)In the case of,I have received itBlock(101),hereBlock(101)Unable to connect to the main chain,Forming an orphan chain
 * Orphan Chain Timed Maintenance Processor
 * The general process of handling orphan chains：
 * 1.Clean up invalid data
 * 2.Maintain existing data
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 afternoon3:54
 */
public class OrphanChainsMaintainer extends BaseMonitor {

    private ChainStorageService chainStorageService;

    private static final OrphanChainsMaintainer INSTANCE = new OrphanChainsMaintainer();

    private OrphanChainsMaintainer() {
        super();
        chainStorageService = SpringLiteContext.getBean(ChainStorageService.class);
    }

    public static OrphanChainsMaintainer getInstance() {
        return INSTANCE;
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger commonLog) {
        ConfigBean parameters = context.getParameters();
        int orphanChainMaxAge = parameters.getOrphanChainMaxAge();

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
                List<Node> availableNodes = NetworkCall.getAvailableNodes(chainId);
                //Maintain existing orphan chains,Attempt to add blocks at the beginning of the chain
                context.setStatus(UPDATE_ORPHAN_CHAINS);
                long l = System.nanoTime();
                for (Chain orphanChain : orphanChains) {
                    maintainOrphanChain(chainId, orphanChain, availableNodes, orphanChainMaxAge);
                    //If the maintenance time of the orphan chain exceeds ten seconds, exit
                    if (System.nanoTime() - l > 10000000000L) {
                        break;
                    }
                }
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
     * Maintaining orphan chains,Requesting the previous block of the orphan chain starting block from other nodes,Limited to orphan chains without a parent chain
     *
     * @param chainId chainId/chain id
     * @param orphanChain
     * @param orphanChainMaxAge
     */
    private void maintainOrphanChain(int chainId, Chain orphanChain, List<Node> availableNodes, int orphanChainMaxAge) {
        ChainContext context = ContextManager.getContext(chainId);
        Map<NulsHash, List<String>> orphanBlockRelatedNodes = context.getOrphanBlockRelatedNodes();
        //Orphan chains with parent chains,It was obtained from a fork in an orphan chain,No need for chain head maintenance
        //The height of the chain head is1Time,No need for chain head maintenance
        if (orphanChain.getParent() != null || orphanChain.getStartHeight() <= 1) {
            return;
        }
        AtomicInteger age = orphanChain.getAge();
        //Orphan chain age exceeds limit,No need for chain head maintenance
        if (age.get() > orphanChainMaxAge) {
            return;
        }
        NulsHash previousHash = orphanChain.getPreviousHash();
        Chain masterChain = BlockChainManager.getMasterChain(chainId);
        if (masterChain.getHashList().contains(previousHash)) {
            return;
        }
        Set<String> nodes = new HashSet<>();
        for (NulsHash nulsHash : orphanChain.getHashList()) {
            List<String> list = orphanBlockRelatedNodes.get(nulsHash);
            if (list != null) {
                nodes.addAll(list);
            }
        }
        //Filter available orphan block maintenance nodes
        Set<String> set = availableNodes.stream().map(Node::getId).collect(Collectors.toSet());
        nodes.retainAll(set);
        Block block;
        //Requesting the previous block of the orphan chain starting block from other nodes
        long l = System.nanoTime();
        for (String availableNode : nodes) {
            block = BlockUtil.downloadBlockByHash(chainId, previousHash, availableNode, orphanChain.getStartHeight() - 1);
            if (block != null) {
                //Successfully downloaded block from node
                orphanChain.addFirst(block);
                chainStorageService.save(chainId, block);
                return;
            } else {
                //Unable to download or timed out, remove this node
                for (NulsHash nulsHash : orphanChain.getHashList()) {
                    List<String> list = orphanBlockRelatedNodes.get(nulsHash);
                    list.remove(availableNode);
                    context.getLogger().warn("get block timeout, kick out this node-" + availableNode);
                }
            }
            //Request block failed,Orphan Chain Age Plus One
            age.incrementAndGet();
            if (age.get() > orphanChainMaxAge) {
                return;
            }
            if (System.nanoTime() - l > 10000000000L) {
                break;
            }
        }
    }

}
