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

package io.nuls.block.manager;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.model.Chain;
import io.nuls.block.model.CheckResult;
import io.nuls.block.rpc.call.ConsensusCall;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.block.service.BlockService;
import io.nuls.block.storage.ChainStorageService;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.*;

import static io.nuls.block.constant.Constant.MODULE_WAITING;
import static io.nuls.block.constant.Constant.MODULE_WORKING;

/**
 * Chain Manager,Maintain the main chain、Set of forked chains、Orphan Chain Collection
 *
 * @author captain
 * @version 1.0
 * @date 18-11-16 afternoon2:29
 */
@Component
public class BlockChainManager {

    @Autowired
    private static BlockService blockService;
    @Autowired
    private static ChainStorageService chainStorageService;

    /**
     * The collection of all main chains running on this machine,according tochainIddistinguish
     */
    private static Map<Integer, Chain> masterChains = new HashMap<>();

    /**
     * The set of all forked chains running on this machine,according tochainIddistinguish
     */
    private static Map<Integer, SortedSet<Chain>> forkChains = new HashMap<>();

    /**
     * The collection of all orphan chains running on this machine,according tochainIddistinguish
     */
    private static Map<Integer, SortedSet<Chain>> orphanChains = new HashMap<>();

    /**
     * forkChainthanmasterChainLonger,Switch main chain
     * Switching in three steps
     * 1.Calculate the fork point between the longest fork chain and the main chain,And obtain the set of forked chains to switch to the main chainB
     * 2.Roll back the main chain to the fork height.
     * 3.Add a set of forked chains in sequenceBBlock to main chain in
     *
     * @param chainId     chainId/chain id
     * @param masterChain
     * @param forkChain
     * @return
     */
    public static CheckResult switchChain(int chainId, Chain masterChain, Chain forkChain) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        try {
            logger.info("*switch chain start");
            logger.info("*masterChain-" + masterChain);
            logger.info("*forkChain-" + forkChain);
            //1.Obtain the fork point between the main chain and the longest fork chain,And record the longest fork chain path starting from the fork point
            Deque<Chain> switchChainPath = new ArrayDeque<>();
            while (forkChain.getParent() != null) {
                switchChainPath.push(forkChain);
                forkChain = forkChain.getParent();
            }
            Chain topForkChain = switchChainPath.peek();
            long forkHeight = topForkChain.getStartHeight();
            long masterChainEndHeight = masterChain.getEndHeight();
            if (masterChainEndHeight < forkHeight) {
                logger.error("*masterChainEndHeight < forkHeight, data error");
                //reset network
                NetworkCall.resetNetwork(chainId);
                //Restart the block synchronization thread
                ConsensusCall.notice(chainId, MODULE_WAITING);
                TransactionCall.notice(chainId, MODULE_WAITING);
                BlockSynchronizer.syn(chainId);
                return new CheckResult(false, true);
            }
            logger.info("*calculate fork point complete, forkHeight=" + forkHeight);

            //2.Roll back the main chain
            //2.1 Roll back the main chain to the specified height,Collect the rolled back blocks and place them in the forked chain database
            ArrayDeque<NulsHash> hashList = new ArrayDeque<>();
            Stack<Block> blockStack = new Stack<>();
            long rollbackHeight = masterChainEndHeight;
            logger.info("*rollback master chain begin, rollbackHeight=" + rollbackHeight);
            do {
                Block block = blockService.getBlock(chainId, rollbackHeight--);
                NulsHash hash = block.getHeader().getHash();
                if (blockService.rollbackBlock(chainId, BlockUtil.toBlockHeaderPo(block), false)) {
                    blockStack.push(block);
                    hashList.offerFirst(hash);
                    logger.info("*rollback master chain doing, success hash=" + hash);
                } else {
                    logger.info("*rollback master chain doing, fail hash=" + hash);
                    saveBlockToMasterChain(chainId, blockStack);
                    return new CheckResult(false, false);
                }
            } while (rollbackHeight >= forkHeight);
            logger.info("*rollback master chain end");
            //2.2 The new fork chain generated by the main chain rollback
            Chain masterForkChain = new Chain();
            masterForkChain.setParent(masterChain);
            masterForkChain.setStartHeight(forkHeight);
            masterForkChain.setEndHeight(masterChainEndHeight);
            masterForkChain.setChainId(chainId);
            masterForkChain.setPreviousHash(topForkChain.getPreviousHash());
            masterForkChain.setHashList(hashList);
            masterForkChain.setType(ChainTypeEnum.FORK);
            masterForkChain.setStartHashCode(hashList.getFirst().hashCode());
            logger.info("*generate new masterForkChain chain-" + masterForkChain);
            //2.3 Below on the main chaintopForkChainThe chain does not need to be changed
            //2.4 Above on the main chaintopForkChainLink the chain back to the new fork chainmasterForkChain
            SortedSet<Chain> higherChains = masterChain.getSons().tailSet(topForkChain);
            if (higherChains.size() > 1) {
                logger.info("*higher than topForkChain-" + higherChains);
                higherChains.remove(topForkChain);
                masterForkChain.setSons(higherChains);
                higherChains.forEach(e -> e.setParent(masterForkChain));
            }
            addForkChain(chainId, masterForkChain);
            if (!chainStorageService.save(chainId, blockStack)) {
                logger.info("*error occur when save masterForkChain");
                append(masterChain, masterForkChain);
                return new CheckResult(false, false);
            }
            //thus,Main chain rollback completed
            logger.info("*masterChain rollback complete");

            //3.Add all fork chain blocks on the longest fork chain path in sequence
            List<Chain> delete = new ArrayList<>();
            while (!switchChainPath.isEmpty()) {
                Chain chain = switchChainPath.pop();
                delete.add(chain);
                Chain subChain = switchChainPath.isEmpty() ? null : switchChainPath.peek();
                boolean b = switchChain0(chainId, masterChain, chain, subChain);
                if (!b) {
                    //Chain switching failed,Restore main chain
                    //Firstly, roll back the blocks added to the main chain during the failed switching process
                    while (masterChain.getEndHeight() >= forkHeight) {
                        blockService.rollbackBlock(chainId, masterChain.getEndHeight(), false);
                    }
                    logger.info("*switchChain0 fail masterChain-" + masterChain + ",chain-" + chain + ",subChain-" +
                            subChain + ",masterForkChain-" + masterForkChain);
                    deleteForkChain(chainId, topForkChain, true);
                    append(masterChain, masterForkChain);
                    return new CheckResult(false, false);
                }
            }
            //6.Closing work
            delete.forEach(e -> deleteForkChain(chainId, e, false));
            logger.info("*switch chain complete");
        } catch (Exception e) {
            logger.error("block chain switch fail, auto rollback fail, process exit.");
            System.exit(1);
        }
        return new CheckResult(true, false);
    }

    private static void saveBlockToMasterChain(int chainId, Stack<Block> blockStack) {
        //Main chain rollback failed midway,Add the previously rolled back blocks back to the main chain
        while (!blockStack.empty()) {
            if (!blockService.saveBlock(chainId, blockStack.pop(), false)) {
                ContextManager.getContext(chainId).getLogger().error("block chain switch fail, auto rollback fail, process exit.");
                System.exit(1);
            }
        }
    }

    /**
     * From fork chainforkChainUploading blocks to add to the main chainmasterChainupper
     * How many blocks to takeforkChainRelated tosubChainThe starting height difference of is calculated
     * subChainyesforkChainOne of the sub chains of,Located on the path of the longest fork chain
     *
     * @param masterChain
     * @param forkChain
     * @param subChain
     * @return
     */
    private static boolean switchChain0(int chainId, Chain masterChain, Chain forkChain, Chain subChain) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        logger.info("*switchChain0 masterChain=" + masterChain + ",forkChain=" + forkChain + ",subChain=" + subChain);
        //1.Calculate fromforkChainHow many blocks are added to the main chain
        int target;
        if (subChain != null) {
            target = (int) (subChain.getStartHeight() - forkChain.getStartHeight());
        } else {
            target = (int) (forkChain.getEndHeight() - forkChain.getStartHeight()) + 1;
        }
        logger.info("*switchChain0 target=" + target);
        //2.Add blocks to the main chain
        Deque<NulsHash> hashList = ((ArrayDeque<NulsHash>) forkChain.getHashList()).clone();
        int count = 0;
        while (target > count) {
            NulsHash hash = hashList.pop();
            Block block = chainStorageService.query(chainId, hash);
            boolean saveBlock = blockService.saveBlock(chainId, block, false);
            if (saveBlock) {
                count++;
            } else {
                logger.info("*switchChain0 saveBlock fail, hash=" + hash);
                return false;
            }
        }
        logger.info("*switchChain0 add block to master chain success");
        //3.After the previous step is completed,IfforkChainThere are also blocks in the middle,Form a new forked chain,Connect to the main chain
        if (!hashList.isEmpty()) {
            Chain newForkChain = new Chain();
            newForkChain.setChainId(chainId);
            newForkChain.setStartHeight(target + forkChain.getStartHeight());
            newForkChain.setParent(masterChain);
            newForkChain.setEndHeight(forkChain.getEndHeight());
            newForkChain.setPreviousHash(subChain.getPreviousHash());
            newForkChain.setHashList(hashList);
            newForkChain.setStartHashCode(hashList.getFirst().hashCode());
            logger.info("*switchChain0 newForkChain-" + newForkChain);

            //4.undersubChainLink the chain back to the main chainmasterChain
            SortedSet<Chain> lowerSubChains = forkChain.getSons().headSet(subChain);
            if (!lowerSubChains.isEmpty()) {
                lowerSubChains.forEach(e -> e.setParent(masterChain));
                masterChain.getSons().addAll(lowerSubChains);
                lowerSubChains.forEach(e -> logger.info("*switchChain0 lowerSubChains-" + e));
            }

            //5.AbovesubChainRelink the chain to the newly generated fork chainnewForkChain
            SortedSet<Chain> higherSubChains = forkChain.getSons().tailSet(subChain);
            if (higherSubChains.size() > 1) {
                higherSubChains.remove(subChain);
                higherSubChains.forEach(e -> e.setParent(newForkChain));
                newForkChain.setSons(higherSubChains);
                higherSubChains.forEach(e -> logger.info("*switchChain0 higherSubChains-" + e));
            }
            addForkChain(chainId, newForkChain);
        }
        return true;
    }

    /**
     * Set up the main chain
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static void setMasterChain(int chainId, Chain chain) {
        masterChains.put(chainId, chain);
    }

    /**
     * Obtain the main chain
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static Chain getMasterChain(int chainId) {
        return masterChains.get(chainId);
    }

    /**
     * Add fork chain
     *
     * @param chainId chainId/chain id
     */
    public static void addForkChain(int chainId, Chain chain) {
        boolean add = forkChains.get(chainId).add(chain);
        if (!add) {
            ContextManager.getContext(chainId).getLogger().warn("add fail, forkChain-" + chain);
        }
    }

    /**
     * Recursive deletion of forked chains
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static void deleteForkChain(int chainId, Chain forkChain, boolean recursive) {
        forkChains.get(chainId).remove(forkChain);
        chainStorageService.remove(chainId, forkChain.getHashList());
        ContextManager.getContext(chainId).getLogger().info("delete Fork Chain-" + forkChain);
        if (recursive && !forkChain.getSons().isEmpty()) {
            forkChain.getSons().forEach(e -> deleteForkChain(chainId, e, true));
        }
    }

    /**
     * Get the set of forked chains
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static SortedSet<Chain> getForkChains(int chainId) {
        SortedSet<Chain> chains = forkChains.get(chainId);
        return chains == null ? Collections.emptySortedSet() : chains;
    }

    /**
     * Update fork chain set
     *
     * @param chainId chainId/chain id
     */
    public static void setForkChains(int chainId, SortedSet<Chain> chains) {
        forkChains.put(chainId, chains);
    }

    /**
     * Add orphan chain
     *
     * @param chainId chainId/chain id
     */
    public static void addOrphanChain(int chainId, Chain chain) {
        boolean add = orphanChains.get(chainId).add(chain);
        if (!add) {
            ContextManager.getContext(chainId).getLogger().warn("add fail, orphanChain-" + chain);
        }
    }

    /**
     * Get Orphan Chain Collection
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static SortedSet<Chain> getOrphanChains(int chainId) {
        SortedSet<Chain> chains = orphanChains.get(chainId);
        return chains == null ? Collections.emptySortedSet() : chains;
    }

    /**
     * Update Orphan Chain Collection
     *
     * @param chainId chainId/chain id
     * @return
     */
    public static void setOrphanChains(int chainId, SortedSet<Chain> chains) {
        orphanChains.put(chainId, chains);
    }

    /**
     * Two chains connected,formationmainChain-subChainstructure
     * IfmainChainIt's the main chain,Except for updating in memorychainBeyond the attributes of,We still need tosubChainSubmit the block to the main chain
     * IfmainChainNot the main chain,Just update the memorychainProperties of,No need to manipulate block data
     *
     * @param mainChain
     * @param subChain  The one that can be connected to other chains must be an orphan chain,Because forked chains are built from a forked block,The fork chain has already been set during initializationparentattribute
     * @return
     */
    public static boolean append(Chain mainChain, Chain subChain) {
        int chainId = mainChain.getChainId();
        if (mainChain.isMaster()) {
            ConsensusCall.notice(chainId, MODULE_WAITING);
            TransactionCall.notice(chainId, MODULE_WAITING);
            List<Block> blockList = chainStorageService.query(subChain.getChainId(), subChain.getHashList());
            List<Block> savedBlockList = new ArrayList<>();
            for (Block block : blockList) {
                if (!blockService.saveBlock(chainId, block, false)) {
                    for (int i = savedBlockList.size() - 1; i >= 0; i--) {
                        if (!blockService.rollbackBlock(chainId, savedBlockList.get(i).getHeader().getHeight(), false)) {
                            ContextManager.getContext(chainId).getLogger().error("block chain data error, can't restore, system exit");
                            System.exit(1);
                        }
                    }
                    throw new NulsRuntimeException(BlockErrorCode.CHAIN_SWITCH_ERROR);
                } else {
                    savedBlockList.add(block);
                }
            }
            ConsensusCall.notice(chainId, MODULE_WORKING);
            TransactionCall.notice(chainId, MODULE_WORKING);
        }
        if (!mainChain.isMaster()) {
            mainChain.getHashList().addAll(subChain.getHashList());
        }
        mainChain.setEndHeight(subChain.getEndHeight());
        mainChain.getSons().addAll(subChain.getSons());
        subChain.getSons().forEach(e -> e.setParent(mainChain));
        subChain.setParent(mainChain);
        return true;
    }

    /**
     * frommainChainFork outsubChain
     * Just update the memorychainProperties of,No need to manipulate block data
     *
     * @param mainChain
     * @param forkChain
     * @return
     */
    public static boolean fork(Chain mainChain, Chain forkChain) {
        forkChain.setParent(mainChain);
        return mainChain.getSons().add(forkChain);
    }

    /**
     * Recursive deletion of orphan chains
     *
     * @param chainId     chainId/chain id
     * @param orphanChain Orphan chain to be deleted
     */
    public static void deleteOrphanChain(int chainId, Chain orphanChain) {
        orphanChains.get(chainId).remove(orphanChain);
        chainStorageService.remove(chainId, orphanChain.getHashList());
        ContextManager.getContext(chainId).getLogger().info("delete Orphan Chain-" + orphanChain);
        if (!orphanChain.getSons().isEmpty()) {
            orphanChain.getSons().forEach(e -> deleteOrphanChain(chainId, e));
        }
    }

    /**
     * initialization
     *
     * @param chainId chainId/chain id
     */
    public static void init(int chainId) {
        forkChains.put(chainId, new TreeSet<>(Chain.COMPARATOR));
        orphanChains.put(chainId, new TreeSet<>(Chain.COMPARATOR));
    }
}
