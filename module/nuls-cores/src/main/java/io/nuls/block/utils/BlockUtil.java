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

package io.nuls.block.utils;

import io.nuls.base.data.*;
import io.nuls.base.data.po.BlockHeaderPo;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashMessage;
import io.nuls.block.message.HeightMessage;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.common.ConfigBean;
import io.nuls.block.rpc.call.ConsensusCall;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.block.service.BlockService;
import io.nuls.block.storage.ChainStorageService;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.ByteUtils;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.nuls.block.constant.CommandConstant.GET_BLOCK_BY_HEIGHT_MESSAGE;
import static io.nuls.block.constant.CommandConstant.GET_BLOCK_MESSAGE;

/**
 * Block tool class
 *
 * @author captain
 * @version 1.0
 * @date 18-11-19 morning11:06
 */
@Component
public class BlockUtil {

    @Autowired
    private static BlockService blockService;
    @Autowired
    private static ChainStorageService chainStorageService;

    public static boolean basicVerify(int chainId, Block block) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        if (block == null) {
            logger.error("basicVerify fail, block is null!");
            return false;
        }

        BlockHeader header = block.getHeader();
        if (header == null) {
            logger.error("basicVerify fail, blockHeader is null!");
            return false;
        }

        if (!headerVerify(chainId, header)) {
            logger.error("basicVerify fail, blockHeader error! height-" + header.getHeight() + ", hash-" + header.getHash());
            return false;
        }

        if (block.getTxs() == null || block.getTxs().isEmpty()) {
            logger.error("basicVerify fail, transaction is null! height-" + header.getHeight() + ", hash-" + header.getHash());
            return false;
        }

        if (block.getTxs().size() != header.getTxCount()) {
            logger.error("basicVerify fail, transaction count not equals! height-" + header.getHeight() + ", hash-" + header.getHash());
            return false;
        }

        ConfigBean parameters = ContextManager.getContext(chainId).getParameters();
        if (block.size() > parameters.getBlockMaxSize()) {
            logger.error("basicVerify fail, beyond blockMaxSize! height-" + header.getHeight() + ", hash-" + header.getHash());
            return false;
        }

        return true;
    }

    public static boolean headerVerify(int chainId, BlockHeader header) {
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        if (header.getHash() == null) {
            logger.error("headerVerify fail, block hash can not be null! height-" + header.getHeight() + ", hash-" + header.getHash());
            return false;
        }

        if (header.getHeight() < 0) {
            logger.error("headerVerify fail, block height can not be less than 0! height-" + header.getHeight() + ", hash-" + header.getHash());
            return false;
        }

        if (null == header.getPackingAddress(chainId)) {
            logger.error("headerVerify fail, block packingAddress can not be null! height-" + header.getHeight() + ", hash-" + header.getHash());
            return false;
        }
        ConfigBean parameters = ContextManager.getContext(chainId).getParameters();
        if (header.getExtend() != null && header.getExtend().length > parameters.getExtendMaxSize()) {
            logger.error("headerVerify fail, block extend too long! height-" + header.getHeight() + ", hash-" + header.getHash());
            return false;
        }

        return true;
    }

    /**
     * Forked block、Orphan block verification
     * blockBChainAThere are four types of relationships：
     * 1.ByesARepeated blocks on top
     * 2.ByesAUpward forked block
     * 3.BCan be directly connected toA
     * 4.BRelated toAThere is no related relationship
     * The above four relationships apply to the main chain、Forked chain、Orphan Chain
     *
     * @param chainId chainId/chain id
     * @param block
     * @return
     */
    public static boolean forkVerify(int chainId, Block block) {
        //1.Judging from the main chain
        ErrorCode mainCode = mainChainProcess(chainId, block).getErrorCode();
        if (mainCode.equals(BlockErrorCode.SUCCESS)) {
            return true;
        }
        if (mainCode.equals(BlockErrorCode.IRRELEVANT_BLOCK)) {
            //2.Not associated with the main chain,Enter the fork chain judgment process
            ErrorCode forkCode = forkChainProcess(chainId, block).getErrorCode();
            if (forkCode.equals(BlockErrorCode.IRRELEVANT_BLOCK)) {
                //3.Not associated with forked chains,Enter the orphan chain judgment process
                orphanChainProcess(chainId, block);
            }
        }
        return false;
    }

    /**
     * Comparison between blocks and main chains
     *
     * @param chainId chainId/chain id
     * @param block
     * @return
     */
    private static Result mainChainProcess(int chainId, Block block) {
        BlockHeader header = block.getHeader();
        long blockHeight = header.getHeight();
        NulsHash blockHash = header.getHash();
        NulsHash blockPreviousHash = header.getPreHash();

        Chain masterChain = BlockChainManager.getMasterChain(chainId);
        long masterChainEndHeight = masterChain.getEndHeight();
        NulsHash masterChainEndHash = masterChain.getEndHash();

        //1.The height difference between the received block and the latest main chain is greater than1000(Configurable),discard
        ChainContext context = ContextManager.getContext(chainId);
        ConfigBean parameters = context.getParameters();
        NulsLogger logger = context.getLogger();
        if (Math.abs(blockHeight - masterChainEndHeight) > parameters.getHeightRange()) {
            logger.error("received out of range block, height:" + blockHeight + ", hash:" + blockHash);
            return Result.getFailed(BlockErrorCode.OUT_OF_RANGE);
        }

        //2.The received block can be connected to the main chain,Verification passed
        if (blockHeight == masterChainEndHeight + 1 && blockPreviousHash.equals(masterChainEndHash)) {
//            logger.debug("received continuous block of masterChain, height:" + blockHeight + ", hash:" + blockHash);
            return Result.getSuccess(BlockErrorCode.SUCCESS);
        }

        if (blockHeight <= masterChainEndHeight) {
            //3.The received block is a duplicate block on the main chain,discard
            BlockHeaderPo masterHeader = blockService.getBlockHeaderPo(chainId, blockHeight);
            if (blockHash.equals(masterHeader.getHash())) {
//                logger.debug("received duplicate block of masterChain, height:" + blockHeight + ", hash:" + blockHash);
                return Result.getFailed(BlockErrorCode.DUPLICATE_MAIN_BLOCK);
            }
            //4.The received block is a fork block on the main chain,Save Block,And add a fork chain link to the main chain
            if (blockPreviousHash.equals(masterHeader.getPreHash())) {
                if (handleSpecificForkBlock(chainId, blockService, header, masterChainEndHeight, masterChainEndHash, masterHeader)) {
                    return Result.getSuccess(BlockErrorCode.SUCCESS);
                }
                chainStorageService.save(chainId, block);
                Chain forkChain = ChainGenerator.generate(chainId, block, masterChain, ChainTypeEnum.FORK);
                BlockChainManager.addForkChain(chainId, forkChain);
                logger.error("received fork block of masterChain, height:" + blockHeight + ", hash:" + blockHash);
                ConsensusCall.evidence(chainId, blockService, header);
                return Result.getFailed(BlockErrorCode.FORK_BLOCK);
            }
        }
        //Not associated with the main chain
        return Result.getFailed(BlockErrorCode.IRRELEVANT_BLOCK);
    }

    /**
     * Processing blocks of the same height from the same packaging address to reduce the probability of network partitioning
     * @param chainId           chainID
     * @param blockService
     * @param header            Block to be saved
     * @param masterChainEndHeight          The latest height of the main chain
     * @param masterChainEndHash            The latest block in the main chainhash
     * @param masterHeader
     * @return
     */
    private static boolean handleSpecificForkBlock(int chainId, BlockService blockService, BlockHeader header, long masterChainEndHeight, NulsHash masterChainEndHash, BlockHeaderPo masterHeader) {
        if (header.getHeight() == masterChainEndHeight) {
            if (Arrays.equals(masterHeader.getPackingAddress(chainId), header.getPackingAddress(chainId))) {
                List<String> list = new ArrayList<>();
                list.add(masterChainEndHash.toHex());
                list.add(header.getHash().toHex());
                list.sort(String.CASE_INSENSITIVE_ORDER);
                if (list.get(0).equals(header.getHash().toHex())) {
                    return blockService.rollbackBlock(chainId, masterHeader, false);
                }
            }
        }
        return false;
    }

    /**
     * Block and fork chain comparison
     *
     * @param chainId chainId/chain id
     * @param block
     * @return
     */
    private static Result forkChainProcess(int chainId, Block block) {
        BlockHeader header = block.getHeader();
        long blockHeight = header.getHeight();
        NulsHash blockHash = header.getHash();
        NulsHash blockPreviousHash = header.getPreHash();
        SortedSet<Chain> forkChains = BlockChainManager.getForkChains(chainId);
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        try {
            for (Chain forkChain : forkChains) {
                long forkChainStartHeight = forkChain.getStartHeight();
                long forkChainEndHeight = forkChain.getEndHeight();
                NulsHash forkChainEndHash = forkChain.getEndHash();
                //1.Direct connection,Chain tail
                if (blockHeight == forkChainEndHeight + 1 && blockPreviousHash.equals(forkChainEndHash)) {
                    chainStorageService.save(chainId, block);
                    forkChain.addLast(block);
                    logger.error("received continuous block of forkChain, height:" + blockHeight + ", hash:" + blockHash);
                    ConsensusCall.evidence(chainId, blockService, header);
                    return Result.getFailed(BlockErrorCode.FORK_BLOCK);
                }
                //2.repeat,discard
                if (forkChainStartHeight <= blockHeight && blockHeight <= forkChainEndHeight && forkChain.getHashList().contains(blockHash)) {
                    logger.error("received duplicate block of forkChain, height:" + blockHeight + ", hash:" + blockHash);
                    return Result.getFailed(BlockErrorCode.FORK_BLOCK);
                }
                //3.Fork
                if (forkChainStartHeight <= blockHeight && blockHeight <= forkChainEndHeight && forkChain.getHashList().contains(blockPreviousHash)) {
                    chainStorageService.save(chainId, block);
                    Chain newForkChain = ChainGenerator.generate(chainId, block, forkChain, ChainTypeEnum.FORK);
                    BlockChainManager.addForkChain(chainId, newForkChain);
                    logger.error("received fork block of forkChain, height:" + blockHeight + ", hash:" + blockHash);
                    ConsensusCall.evidence(chainId, blockService, header);
                    return Result.getFailed(BlockErrorCode.FORK_BLOCK);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        //4.Not associated with forked chains,Enter the orphan chain judgment process
        return Result.getFailed(BlockErrorCode.IRRELEVANT_BLOCK);
    }

    /**
     * Comparison between blocks and orphan chains
     *
     * @param chainId chainId/chain id
     * @param block
     * @return
     */
    private static void orphanChainProcess(int chainId, Block block) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsHash blockHash = block.getHeader().getHash();
        NulsLogger logger = context.getLogger();
        if (block.getNodeId() != null) {
            //Add the association relationship between orphan blocks and nodes, and download from these nodes when maintaining orphan blocks
            Map<NulsHash, List<String>> map = context.getOrphanBlockRelatedNodes();
            List<String> list = map.computeIfAbsent(blockHash, k -> new ArrayList<>());
            list.add(block.getNodeId());
            logger.debug("add OrphanBlockRelatedNodes, blockHash-{}, nodeId-{}", blockHash, block.getNodeId());
        }
        long blockHeight = block.getHeader().getHeight();
        NulsHash blockPreviousHash = block.getHeader().getPreHash();
        SortedSet<Chain> orphanChains = BlockChainManager.getOrphanChains(chainId);
        try {
            for (Chain orphanChain : orphanChains) {
                long orphanChainStartHeight = orphanChain.getStartHeight();
                long orphanChainEndHeight = orphanChain.getEndHeight();
                NulsHash orphanChainEndHash = orphanChain.getEndHash();
                NulsHash orphanChainPreviousHash = orphanChain.getPreviousHash();
                //1.Direct connection,Splitting head、There are two situations at the end of the chain
                if (blockHeight == orphanChainEndHeight + 1 && blockPreviousHash.equals(orphanChainEndHash)) {
                    chainStorageService.save(chainId, block);
                    orphanChain.addLast(block);
                    logger.debug("received continuous tail block of orphanChain, height:" + blockHeight + ", hash:" + blockHash);
                    return;
                }
                if (blockHeight == orphanChainStartHeight - 1 && blockHash.equals(orphanChainPreviousHash)) {
                    chainStorageService.save(chainId, block);
                    orphanChain.addFirst(block);
                    logger.info("received continuous head block of orphanChain, height:" + blockHeight + ", hash:" + blockHash);
                    return;
                }
                //2.repeat,discard
                if (orphanChainStartHeight <= blockHeight && blockHeight <= orphanChainEndHeight && orphanChain.getHashList().contains(blockHash)) {
                    logger.debug("received duplicate block of orphanChain, height:" + blockHeight + ", hash:" + blockHash);
                    return;
                }
                //3.Fork
                if (orphanChainStartHeight <= blockHeight && blockHeight <= orphanChainEndHeight && orphanChain.getHashList().contains(blockPreviousHash)) {
                    chainStorageService.save(chainId, block);
                    Chain forkOrphanChain = ChainGenerator.generate(chainId, block, orphanChain, ChainTypeEnum.ORPHAN);
                    BlockChainManager.addOrphanChain(chainId, forkOrphanChain);
                    logger.info("received fork block of orphanChain, height:" + blockHeight + ", hash:" + blockHash);
                    return;
                }
            }
            //4.With the main chain、Forked chain、Orphan chains have nothing to do with it,Forming a new orphan chain
            chainStorageService.save(chainId, block);
            Chain newOrphanChain = ChainGenerator.generate(chainId, block, null, ChainTypeEnum.ORPHAN);
            BlockChainManager.addOrphanChain(chainId, newOrphanChain);
            logger.info("received orphan block, height:" + blockHeight + ", hash:" + blockHash);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static SmallBlock getSmallBlock(int chainId, Block block) {
        ChainContext context = ContextManager.getContext(chainId);
        List<Integer> transactionType = context.getSystemTransactionType();
        if (transactionType.isEmpty()) {
            transactionType.addAll(TransactionCall.getSystemTypes(chainId));
        }
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setHeader(block.getHeader());
        smallBlock.setTxHashList((ArrayList<NulsHash>) block.getTxHashList());
        block.getTxs().stream().filter(e -> transactionType.contains(e.getType())).forEach(smallBlock::addSystemTx);
        return smallBlock;
    }

    /**
     * according tosmallblockandtxmapAssemble a complete block
     *
     * @param header
     * @param txMap
     * @param txHashList
     * @return
     */
    public static Block assemblyBlock(BlockHeader header, Map<NulsHash, Transaction> txMap, List<NulsHash> txHashList) {
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for (NulsHash txHash : txHashList) {
            Transaction tx = txMap.get(txHash);
            if (null == tx) {
                throw new NulsRuntimeException(BlockErrorCode.DATA_ERROR);
            }
            tx.setBlockHeight(header.getHeight());
            txs.add(tx);
        }
        block.setTxs(txs);
        return block;
    }

    public static BlockHeader fromBlockHeaderPo(BlockHeaderPo po) {
        BlockHeader header = new BlockHeader();
        header.setHash(po.getHash());
        header.setHeight(po.getHeight());
        header.setExtend(po.getExtend());
        header.setPreHash(po.getPreHash());
        header.setTime(po.getTime());
        header.setMerkleHash(po.getMerkleHash());
        header.setTxCount(po.getTxCount());
        header.setBlockSignature(po.getBlockSignature());
        return header;
    }


    public static BlockHeaderPo toBlockHeaderPo(Block block) {
        BlockHeaderPo po = new BlockHeaderPo();
        BlockHeader blockHeader = block.getHeader();
        po.setHash(blockHeader.getHash());
        po.setPreHash(blockHeader.getPreHash());
        po.setMerkleHash(blockHeader.getMerkleHash());
        po.setTime(blockHeader.getTime());
        po.setHeight(blockHeader.getHeight());
        po.setTxCount(blockHeader.getTxCount());
        po.setBlockSignature(blockHeader.getBlockSignature());
        po.setExtend(blockHeader.getExtend());
        po.setTxHashList(block.getTxHashList());
        po.setComplete(false);
        po.setBlockSize(block.size());
        return po;
    }

    /**
     * Download blocks from nodes based on block height
     *
     * @param chainId chainId/chain id
     * @param nodeId
     * @param height
     * @return
     */
    public static Block downloadBlockByHeight(int chainId, String nodeId, long height) {
        if (height < 0 || nodeId == null) {
            return null;
        }
        HeightMessage message = new HeightMessage(height);
        ChainContext context = ContextManager.getContext(chainId);
        int singleDownloadTimeout = context.getParameters().getSingleDownloadTimeout();
        NulsLogger logger = context.getLogger();
        NulsHash hash = NulsHash.calcHash(ByteUtils.longToBytes(height));
        Future<Block> future = SingleBlockCacher.addRequest(chainId, hash);
        logger.debug("get block from " + nodeId + " begin, height-" + height);
        boolean result = NetworkCall.sendToNode(chainId, message, nodeId, GET_BLOCK_BY_HEIGHT_MESSAGE);
        if (!result) {
            SingleBlockCacher.removeRequest(chainId, hash);
            return null;
        }
        try {
            Block block = future.get(singleDownloadTimeout, TimeUnit.MILLISECONDS);
            logger.debug("get block from " + nodeId + " success!, height-" + height);
            return block;
        } catch (Exception e) {
            logger.error("get block from " + nodeId + " fail!, height-" + height, e);
            return null;
        } finally {
            SingleBlockCacher.removeRequest(chainId, hash);
        }
    }

    /**
     * Based on blockshashDownload blocks from nodes
     *
     * @param chainId chainId/chain id
     * @param hash
     * @param nodeId
     * @param height
     * @return
     */
    public static Block downloadBlockByHash(int chainId, NulsHash hash, String nodeId, long height) {
        if (hash == null || nodeId == null) {
            return null;
        }
        HashMessage message = new HashMessage();
        message.setRequestHash(hash);
        ChainContext context = ContextManager.getContext(chainId);
        int singleDownloadTimeout = context.getParameters().getSingleDownloadTimeout();
        NulsLogger logger = context.getLogger();
        Future<Block> future = SingleBlockCacher.addRequest(chainId, hash);
        logger.debug("get block-" + hash + " from " + nodeId + " begin, height-" + height);
        boolean result = NetworkCall.sendToNode(chainId, message, nodeId, GET_BLOCK_MESSAGE);
        if (!result) {
            SingleBlockCacher.removeRequest(chainId, hash);
            return null;
        }
        try {
            Block block = future.get(singleDownloadTimeout, TimeUnit.MILLISECONDS);
            logger.debug("get block-" + hash + " from " + nodeId + " success!, height-" + height);
            return block;
        } catch (Exception e) {
            logger.error("get block-" + hash + " from " + nodeId + " fail!, height-" + height, e);
            return null;
        } finally {
            SingleBlockCacher.removeRequest(chainId, hash);
        }
    }

}
