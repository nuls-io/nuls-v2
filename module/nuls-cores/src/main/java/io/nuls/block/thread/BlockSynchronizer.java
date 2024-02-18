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

package io.nuls.block.thread;

import com.google.common.collect.Lists;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.po.BlockHeaderPo;
import io.nuls.block.constant.LocalBlockStateEnum;
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.*;
import io.nuls.block.rpc.call.ConsensusCall;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.rpc.call.ProtocolCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.block.service.BlockService;
import io.nuls.block.storage.BlockStorageService;
import io.nuls.block.storage.RollbackStorageService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.block.utils.ChainGenerator;
import io.nuls.common.CommonContext;
import io.nuls.common.ConfigBean;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.thread.ThreadUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.BlockBootstrap.blockConfig;
import static io.nuls.block.constant.Constant.MODULE_WORKING;
import static io.nuls.block.constant.LocalBlockStateEnum.*;

/**
 * Block synchronization main thread,Manage block synchronization across multiple chains
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 afternoon5:49
 */
public class BlockSynchronizer implements Runnable {

    private static Map<Integer, BlockSynchronizer> synMap = new HashMap<>();

    private int chainId;

    private boolean running;

    private static boolean firstStart = true;
    /**
     * Save block synchronization status for multiple chains
     */
    private BlockService blockService;

    BlockSynchronizer(int chainId) {
        this.chainId = chainId;
        this.running = false;
        this.blockService = SpringLiteContext.getBean(BlockService.class);
    }

    public static void syn(int chainId) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        BlockSynchronizer blockSynchronizer = synMap.computeIfAbsent(chainId, BlockSynchronizer::new);
        if (!blockSynchronizer.isRunning()) {
            logger.info("blockSynchronizer run......");
            ThreadUtils.createAndRunThread("block-synchronizer", blockSynchronizer);
        } else {
            logger.info("blockSynchronizer already running......");
        }
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        setRunning(true);
        ChainContext context = ContextManager.getContext(chainId);
        context.setStatus(StatusEnum.SYNCHRONIZING);
        NulsLogger logger = context.getLogger();
        try {
            CommonContext.START_BOOT.await();
            BlockStorageService blockStorageService = SpringLiteContext.getBean(BlockStorageService.class);
            long latestHeight = blockStorageService.queryLatestHeight(chainId);
            BlockHeaderPo blockHeaderPo = blockStorageService.query(chainId, latestHeight);
            //If there was an error saving the block during the last synchronization,It is possible that the local latest block header data is inaccurate,Verification required
            if (!blockHeaderPo.isComplete()) {
                logger.info("clean incomplete block between block-syn, incomplete block generated by last failed block-syn");
                if (!ProtocolCall.rollbackNotice(chainId, BlockUtil.fromBlockHeaderPo(blockHeaderPo))) {
                    logger.error("ProtocolCall rollback error when clean incomplete block ");
                    System.exit(1);
                }
                if (!TransactionCall.rollback(chainId, blockHeaderPo)) {
                    logger.error("TransactionCall rollback error when clean incomplete block ");
                    System.exit(1);
                }
                if (!blockStorageService.remove(chainId, latestHeight)) {
                    logger.error("blockStorageService remove error when clean incomplete block ");
                    System.exit(1);
                }
                latestHeight = latestHeight - 1;
                if (!blockStorageService.setLatestHeight(chainId, latestHeight)) {
                    logger.error("blockStorageService setLatestHeight error when clean incomplete block ");
                    System.exit(1);
                }
                //latestHeightSuccessfully maintained,The above steps ensure thatlatestHeightThe block data at this height is complete locally,But the content of block data may not necessarily be correct,So we need to continue verifyinglatestBlock
                Block block = blockService.getBlock(chainId, latestHeight);
                //Local block maintenance successful
                context.setLatestBlock(block);
                BlockChainManager.setMasterChain(chainId, ChainGenerator.generateMasterChain(chainId, block, blockService));
            }
            //Automatically roll back blocks after system startup,Rollback quantitytestAutoRollbackAmountWrite in the configuration file
            if (firstStart) {
                firstStart = false;
                int testAutoRollbackAmount = blockConfig.getTestAutoRollbackAmount();
                if (testAutoRollbackAmount > 0) {
                    if (latestHeight < testAutoRollbackAmount) {
                        testAutoRollbackAmount = (int) (latestHeight);
                    }
                    for (int i = 0; i < testAutoRollbackAmount; i++) {
                        boolean b = blockService.rollbackBlock(chainId, latestHeight--, true);
                        if (!b || latestHeight == 0) {
                            break;
                        }
                    }
                }
                rollbackToHeight(latestHeight, chainId);
            }
            while (true) {
                if (synchronize()) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            setRunning(false);
        }
    }

    /**
     * Rollback block to specified height
     */
    private void rollbackToHeight(long latestHeight, int chainId) {
        NulsCoresConfig blockConfig = SpringLiteContext.getBean(NulsCoresConfig.class);
        long height = blockConfig.getRollbackHeight();
        if (height > 0) {
            RollbackStorageService rollbackService = SpringLiteContext.getBean(RollbackStorageService.class);
            RollbackInfoPo po = rollbackService.get(chainId);
            if (po == null || po.getHeight() != height) {
                if (latestHeight > height + 1000) {
                    ContextManager.getContext(chainId).getLogger().warn("If the rollback height is greater than 1000,p;ease replace the data package");
                    System.exit(1);
                }
                while (latestHeight >= height) {
                    if (!blockService.rollbackBlock(chainId, latestHeight--, true)) {
                        latestHeight++;
                    }
                    if (latestHeight == 0) {
                        break;
                    }
                }
                po = new RollbackInfoPo(height);
                rollbackService.save(po, chainId);
            }
        }
    }

    /**
     * Waiting for network stability
     * every other5Request once per secondgetAvailableNodes,continuity5The number of sub nodes is greater thanminNodeAmountI think the network is stable
     */
    private List<Node> waitUntilNetworkStable() throws InterruptedException {
        ChainContext context = ContextManager.getContext(chainId);
        ConfigBean parameters = context.getParameters();
        int waitNetworkInterval = parameters.getWaitNetworkInterval();
        int minNodeAmount = parameters.getMinNodeAmount();
        NulsLogger logger = context.getLogger();
        List<Node> availableNodes;
        int nodeAmount;
        int count = 0;
        while (true) {
            availableNodes = NetworkCall.getAvailableNodes(chainId);
            nodeAmount = availableNodes.size();
            if (nodeAmount >= minNodeAmount) {
                count++;
            } else {
                count = 0;
            }
            logger.info("minNodeAmount = " + minNodeAmount + ", current nodes amount=" + nodeAmount + ", wait until network stable......");
            if (count >= 5) {
                return availableNodes;
            }
            Thread.sleep(waitNetworkInterval);
        }
    }

    private boolean synchronize() throws Exception {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        //1.Call the network module interface to obtain the currentchainIdAvailable nodes of the network
        List<Node> availableNodes = waitUntilNetworkStable();
        //2.Determine whether the number of available nodes meets the minimum configuration
        ChainContext context = ContextManager.getContext(chainId);
        ConfigBean parameters = context.getParameters();
        int minNodeAmount = parameters.getMinNodeAmount();
        if (minNodeAmount == 0 && availableNodes.isEmpty()) {
            logger.info("Skip block syn, because minNodeAmount is set to 0, minNodeAmount should't set to 0 otherwise you want run local node without connect with network");
            context.setStatus(StatusEnum.RUNNING);
            ConsensusCall.notice(chainId, MODULE_WORKING);
            TransactionCall.notice(chainId, MODULE_WORKING);
            return true;
        }
        //3.Calculate the consistent block height of available nodes in a statistical network、blockhash
        BlockDownloaderParams downloaderParams = statistics(availableNodes, context);
        context.setDownloaderParams(downloaderParams);

        if (downloaderParams.getNodes() == null || downloaderParams.getNodes().isEmpty()) {
            //There are no available consistent nodes on the network,The height of the nodes is inconsistent,Or the proportion of consistent nodes is insufficient
            logger.warn("There are no consistent nodes available on the network, availableNodes-" + availableNodes);
            return false;
        }
        int size = downloaderParams.getNodes().size();
        //All nodes on the network have a height of0,This indicates that the chain is running for the first time
        if (downloaderParams.getNetLatestHeight() == 0 && size == availableNodes.size()) {
            logger.info("This blockchain just started running, no one has been generated a block");
            context.setStatus(StatusEnum.RUNNING);
            ConsensusCall.notice(chainId, MODULE_WORKING);
            TransactionCall.notice(chainId, MODULE_WORKING);
            return true;
        }
        //Check local block status
        LocalBlockStateEnum stateEnum = checkLocalBlock(downloaderParams);
        if (stateEnum.equals(CONSISTENT)) {
            logger.info("The local node's block is the latest height and does not need to be synchronized");
            context.setStatus(StatusEnum.RUNNING);
            ConsensusCall.notice(chainId, MODULE_WORKING);
            TransactionCall.notice(chainId, MODULE_WORKING);
            return true;
        }
        if (stateEnum.equals(UNCERTAINTY)) {
            logger.warn("The number of rolled back blocks exceeded the configured value");
            NetworkCall.resetNetwork(chainId);
            return false;
        }
        if (stateEnum.equals(CONFLICT)) {
            logger.error("The local genesis block is different from networks");
            System.exit(1);
        }
        long netLatestHeight = downloaderParams.getNetLatestHeight();
        context.setNetworkHeight(netLatestHeight);
        long startHeight = downloaderParams.getLocalLatestHeight() + 1;
        long total = netLatestHeight - startHeight + 1;
        long start = System.currentTimeMillis();
        //5.Enable block downloaderBlockDownloader
        BlockDownloader downloader = new BlockDownloader(chainId);
        Future<Boolean> downloadFutrue = ThreadUtils.asynExecuteCallable(downloader);
        //6.Start block consumption threadBlockConsumer
        BlockConsumer consumer = new BlockConsumer(chainId);
        Future<Boolean> consumerFuture = ThreadUtils.asynExecuteCallable(consumer);
        Boolean downResult = downloadFutrue.get();
        Boolean storageResult = consumerFuture.get();
        boolean success = downResult != null && downResult && storageResult != null && storageResult;
        long end = System.currentTimeMillis();
        if (success) {
            logger.info("Block syn complete, total download:" + total + ", total time:" + (end - start) + ", average time:" + (end - start) / total);
            if (checkIsNewest(context)) {
                //To test fork chain switching or orphan chain,Release the following statement,The probability will increase
//                if (true) {
                logger.info("Block syn complete successfully, current height-" + downloaderParams.getNetLatestHeight());
                context.setNeedSyn(false);
                context.setStatus(StatusEnum.RUNNING);
                ConsensusCall.notice(chainId, MODULE_WORKING);
                TransactionCall.notice(chainId, MODULE_WORKING);
                return true;
            } else {
                logger.info("Block syn complete but another syn is needed");
            }
        } else {
            logger.error("Block syn fail, downResult:" + downResult + ", storageResult:" + storageResult);
        }
        context.setNeedSyn(true);
        context.getBlockMap().clear();
        context.getCachedBlockSize().set(0);
        context.setDownloaderParams(null);
        return false;
    }

    /**
     * Check if the local block is synchronized to the latest height,If it's not the latest altitude,Change synchronization status toBlockSynStatusEnum.WAITING,Waiting for next synchronization
     *
     * @param context
     * @return
     */
    private boolean checkIsNewest(ChainContext context) {
        BlockDownloaderParams newestParams = statistics(NetworkCall.getAvailableNodes(chainId), context);
        return newestParams.getNetLatestHeight() <= context.getLatestBlock().getHeader().getHeight();
    }

    /**
     * Calculate the consistent block height of available nodes in a statistical network、blockhash,Construct download parameters
     *
     * @param
     * @param context
     * @return
     * @date 18-11-8 afternoon4:55
     */
    BlockDownloaderParams statistics(List<Node> availableNodes, ChainContext context) {
        BlockDownloaderParams params = new BlockDownloaderParams();
        List<Node> filterAvailableNodes = filterNodes(availableNodes, context);
        params.setAvailableNodesCount(filterAvailableNodes.size());
        if (filterAvailableNodes.isEmpty()) {
            return params;
        }
        //Each node's(LatestHASH+Latest height)yeskey
        String key = "";
        int count = 0;
        //AkeyHold this for the primary key recordkeyNode list for
        Map<String, List<Node>> nodeMap = new HashMap<>(filterAvailableNodes.size());
        //AkeyCount the number of times for the primary key
        Map<String, Integer> countMap = new HashMap<>(filterAvailableNodes.size());
        for (Node node : filterAvailableNodes) {
            String tempKey = node.getHash().toHex() + node.getHeight();
            if (countMap.containsKey(tempKey)) {
                //tempKeyExisting,Count the number of times added1
                countMap.put(tempKey, countMap.get(tempKey) + 1);
            } else {
                //tempKeyNot present,Number of initialization statistics
                countMap.put(tempKey, 1);
            }

            if (nodeMap.containsKey(tempKey)) {
                //tempKeyExisting,Add to the list of holding nodes
                List<Node> nodes = nodeMap.get(tempKey);
                nodes.add(node);
            } else {
                //tempKeyNot present,Add a list of holding nodes
                nodeMap.put(tempKey, Lists.newArrayList(node));
            }
        }
        //Finally, the most frequent occurrence was identifiedkey,Get the latest credible altitude and latesthash,And a list of trusted nodes
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            Integer value = entry.getValue();
            if (value > count) {
                count = value;
                key = entry.getKey();
            }
        }
        ConfigBean parameters = context.getParameters();
        double div = DoubleUtils.div(count, filterAvailableNodes.size(), 2);
        byte percent = calculateConsistencyNodePercent(parameters.getConsistencyNodePercent(), filterAvailableNodes.size());
        if (div * 100 < percent) {
            return params;
        }
        List<Node> nodeList = nodeMap.get(key);
        params.setNodes(nodeList);
        Map<String, Node> statusMap = new ConcurrentHashMap<>();
        nodeList.forEach(e -> statusMap.put(e.getId(), e));
        params.setNodeMap(statusMap);
        Node node = nodeList.get(0);
        params.setNetLatestHash(node.getHash());
        params.setNetLatestHeight(node.getHeight());

        // a read-only method
        // upgrade from optimistic read to read lock
        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.readLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                params.setLocalLatestHeight(context.getLatestHeight());
                params.setLocalLatestHash(context.getLatestBlock().getHeader().getHash());
                if (!lock.validate(stamp)) {
                    continue;
                }
                return params;
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    /**
     * Filter invalid connection nodes
     *
     * @param availableNodes
     * @param context
     * @return
     */
    private List<Node> filterNodes(List<Node> availableNodes, ChainContext context) {
        //The height of the connecting node is less than the height of this node1000
        availableNodes.removeIf(availableNode -> availableNode.getHeight() < context.getLatestHeight() - context.getParameters().getHeightRange());
        //The connecting node is on the same chain as the current node and has a lower height than the current node
        Chain masterChain = BlockChainManager.getMasterChain(chainId);
        availableNodes.removeIf(availableNode -> masterChain.getHashList().contains(availableNode.getHash()) && availableNode.getHeight() < context.getLatestHeight());
        return availableNodes;
    }

    /**
     * When calculating connections to different numbers of nodes,The lowest proportion of consistent nodes
     *
     * @param consistencyNodePercent Original Scale
     * @param size                   Number of connected nodes
     * @return Final proportion
     */
    private byte calculateConsistencyNodePercent(byte consistencyNodePercent, int size) {
        byte percent = consistencyNodePercent;
        percent -= ((size / 4) - 1) * 5;
        return percent < 50 ? 50 : percent;
    }

    /**
     * Before block synchronization,Compared to network blocks,Check if the local block needs to be rolled back
     *
     * @param params
     * @return
     */
    private LocalBlockStateEnum checkLocalBlock(BlockDownloaderParams params) {
        long localHeight = params.getLocalLatestHeight();
        long netHeight = params.getNetLatestHeight();
        //Obtain a common height
        long commonHeight = Math.min(localHeight, netHeight);
        CheckResult result = checkHashEquality(params);
        if (result.isResult() || result.isTimeout()) {
            //commonHeightBlockedhashunanimous,normal,Lagging behind remote nodes,Download blocks
            if (commonHeight < netHeight) {
                return INCONSISTENT;
            } else {
                return CONSISTENT;
            }
        } else {
            return checkRollback(0, params);
        }
    }

    private LocalBlockStateEnum checkRollback(int rollbackCount, BlockDownloaderParams params) {
        //Maximum rollback per sessionmaxRollbackBlocks,Waiting for next synchronization,This can avoid being attacked by malicious nodes,Rolling back a large number of normal blocks.
        ConfigBean parameters = ContextManager.getContext(chainId).getParameters();
        if (params.getLocalLatestHeight() == 0) {
            return CONFLICT;
        }
        if (rollbackCount >= parameters.getMaxRollback()) {
            return UNCERTAINTY;
        }
        blockService.rollbackBlock(chainId, params.getLocalLatestHeight(), true);
        BlockHeader latestBlockHeader = blockService.getLatestBlockHeader(chainId);
        params.setLocalLatestHeight(latestBlockHeader.getHeight());
        params.setLocalLatestHash(latestBlockHeader.getHash());
        CheckResult result = checkHashEquality(params);
        if (result.isResult() || result.isTimeout()) {
            return INCONSISTENT;
        }
        return checkRollback(rollbackCount + 1, params);
    }

    /**
     * Based on the incoming blocklocalBlockjudgelocalBlock.hashBlocks at the same height as those on the networkhashIs it consistent
     *
     * @author captain
     * @date 18-11-9 afternoon6:13
     * @version 1.0
     */
    private CheckResult checkHashEquality(BlockDownloaderParams params) {
        NulsHash localHash = params.getLocalLatestHash();
        long localHeight = params.getLocalLatestHeight();
        long netHeight = params.getNetLatestHeight();
        NulsHash netHash = params.getNetLatestHash();
        //Obtain a common height
        long commonHeight = Math.min(localHeight, netHeight);
        //If both parties share a common height<Network height,To proceedhashjudge,Need to download blocks from the network,becauseparamsThere are only the latest blocks in ithash,No old oneshash
        if (commonHeight < netHeight) {
            for (Node node : params.getNodes()) {
                Block remoteBlock = BlockUtil.downloadBlockByHash(chainId, localHash, node.getId(), commonHeight);
                if (remoteBlock != null) {
                    netHash = remoteBlock.getHeader().getHash();
                    return new CheckResult(localHash.equals(netHash), false);
                }
            }
            //If downloading blocks from the network fails,returnfalse
            return new CheckResult(false, true);
        }
        if (commonHeight < localHeight) {
            localHash = blockService.getBlockHash(chainId, commonHeight);
        }
        return new CheckResult(localHash.equals(netHash), false);
    }
}
