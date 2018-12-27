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

package io.nuls.block.utils;

import io.nuls.base.data.*;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.block.service.BlockService;
import io.nuls.block.service.ChainStorageService;
import io.nuls.block.utils.module.ConsensusUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 区块工具类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-19 上午11:06
 */
@Component
public class BlockUtil {

    @Autowired
    private static BlockService blockService;
    @Autowired
    private static ChainStorageService chainStorageService;

    public static boolean basicVerify(int chainId, Block block) {

        if (block == null) {
            Log.warn("basicVerify fail, block is null! chainId-{}, height-{}, hash-{}", chainId, block.getHeader().getHeight(), block.getHeader().getHash());
            return false;
        }

        if (block.getHeader() == null) {
            Log.warn("basicVerify fail, blockHeader is null! chainId-{}, height-{}, hash-{}", chainId, block.getHeader().getHeight(), block.getHeader().getHash());
            return false;
        }

        if (!headerVerify(chainId, block.getHeader())) {
            Log.warn("basicVerify fail, blockHeader error! chainId-{}, height-{}, hash-{}", chainId, block.getHeader().getHeight(), block.getHeader().getHash());
            return false;
        }

        if (block.getTxs() == null || block.getTxs().isEmpty()) {
            Log.warn("basicVerify fail, transaction is null! chainId-{}, height-{}, hash-{}", chainId, block.getHeader().getHeight(), block.getHeader().getHash());
            return false;
        }

        if (block.getTxs().size() != block.getHeader().getTxCount()) {
            Log.warn("basicVerify fail, transaction count not equals! chainId-{}, height-{}, hash-{}", chainId, block.getHeader().getHeight(), block.getHeader().getHash());
            return false;
        }

        int value = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.BLOCK_MAX_SIZE));
        if (block.size() > value) {
            Log.warn("basicVerify fail, blockMaxSize! chainId-{}, height-{}, hash-{}", chainId, block.getHeader().getHeight(), block.getHeader().getHash());
            return false;
        }

        return true;
    }

    public static boolean headerVerify(int chainId, BlockHeader header) {

        if (header.getHash() == null) {
            Log.warn("headerVerify fail, block hash can not be null! chainId-{}, height-{}, hash-{}", chainId, header.getHeight(), header.getHash());
            return false;
        }

        if (header.getHeight() < 0) {
            Log.warn("headerVerify fail, block height can not be less than 0! chainId-{}, height-{}, hash-{}", chainId, header.getHeight(), header.getHash());
            return false;
        }

        if (null == header.getPackingAddress(chainId)) {
            Log.warn("headerVerify fail, block packingAddress can not be null! chainId-{}, height-{}, hash-{}", chainId, header.getHeight(), header.getHash());
            return false;
        }

        int value = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.EXTEND_MAX_SIZE));
        if (header.getExtend() != null && header.getExtend().length > value) {
            Log.warn("headerVerify fail, block extend too long! chainId-{}, height-{}, hash-{}", chainId, header.getHeight(), header.getHash());
            return false;
        }

        if (header.getBlockSignature().verifySignature(header.getHash()).isFailed()) {
            Log.warn("headerVerify fail, Block Signature error! chainId-{}, height-{}, hash-{}", chainId, header.getHeight(), header.getHash());
            return false;
        }

        return true;
    }

    /**
     * 分叉区块、孤儿区块验证
     * 区块B与链A的关系有四种：
     * 1.B是A上重复的区块
     * 2.B是A上分叉的区块
     * 3.B能直接连到A
     * 4.B与A没有任何关联关系
     * 以上四种关系适用于主链、分叉链、孤儿链
     *
     * @param chainId
     * @param block
     * @return
     */
    public static boolean forkVerify(int chainId, Block block) {
        try {
            //1.与主链判断
            ErrorCode mainCode = mainChainProcess(chainId, block).getErrorCode();
            if (mainCode.equals(BlockErrorCode.SUCCESS)) {
                return true;
            }

            if (mainCode.equals(BlockErrorCode.IRRELEVANT_BLOCK)) {
                //2.与主链没有关联,进入分叉链判断流程
                ErrorCode forkCode = forkChainProcess(chainId, block).getErrorCode();
                if (forkCode.equals(BlockErrorCode.IRRELEVANT_BLOCK)) {
                    //3.与分叉链没有关联,进入孤儿链判断流程
                    orphanChainProcess(chainId, block);
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    /**
     * 区块与主链比对
     *
     * @param chainId
     * @param block
     * @return
     */
    private static Result mainChainProcess(int chainId, Block block) {
        BlockHeader header = block.getHeader();
        long blockHeight = header.getHeight();
        NulsDigestData blockHash = header.getHash();
        NulsDigestData blockPreviousHash = header.getPreHash();

        Chain masterChain = ChainManager.getMasterChain(chainId);
        long masterChainEndHeight = masterChain.getEndHeight();
        NulsDigestData masterChainEndHash = masterChain.getEndHash();

        //1.收到的区块与主链最新高度差大于1000(可配置),丢弃
        int value = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.HEIGHT_RANGE));
        if (Math.abs(blockHeight - masterChainEndHeight) > value) {
            Log.debug("chainId:{}, received out of range blocks, height:{}, hash:{}", chainId, blockHeight, blockHash);
            return Result.getFailed(BlockErrorCode.OUT_OF_RANGE);
        }

        //2.收到的区块可以连到主链,验证通过
        if (blockHeight == masterChainEndHeight + 1 && blockPreviousHash.equals(masterChainEndHash)) {
            Log.debug("chainId:{}, received continuous blocks of masterChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
            return Result.getSuccess(BlockErrorCode.SUCCESS);
        }

        if (blockHeight <= masterChainEndHeight) {
            //3.收到的区块是主链上的重复区块,丢弃
            BlockHeaderPo blockHeader = blockService.getBlockHeader(chainId, blockHeight);
            if (blockHash.equals(blockHeader.getHash())) {
                Log.debug("chainId:{}, received duplicate blocks of masterChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                return Result.getFailed(BlockErrorCode.DUPLICATE_MAIN_BLOCK);
            }
            //4.收到的区块是主链上的分叉区块,保存区块,并新增一条分叉链链接到主链
            if (blockPreviousHash.equals(blockHeader.getPreHash())) {
                chainStorageService.save(chainId, block);
                Chain forkChain = ChainGenerator.generate(chainId, block, masterChain, ChainTypeEnum.FORK);
                ChainManager.addForkChain(chainId, forkChain);
                ConsensusUtil.evidence(chainId, ContextManager.getContext(chainId).getLatestBlock().getHeader(), header);
                Log.debug("chainId:{}, received evidence blocks of masterChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                return Result.getFailed(BlockErrorCode.FORK_BLOCK);
            }
        }
        //与主链没有关联
        return Result.getFailed(BlockErrorCode.IRRELEVANT_BLOCK);
    }

    /**
     * 区块与分叉链比对
     *
     * @param chainId
     * @param block
     * @return
     */
    private static Result forkChainProcess(int chainId, Block block) {
        long blockHeight = block.getHeader().getHeight();
        NulsDigestData blockHash = block.getHeader().getHash();
        NulsDigestData blockPreviousHash = block.getHeader().getPreHash();
        SortedSet<Chain> forkChains = ChainManager.getForkChains(chainId);
        try {
            for (Chain forkChain : forkChains) {
                long forkChainStartHeight = forkChain.getStartHeight();
                long forkChainEndHeight = forkChain.getEndHeight();
                NulsDigestData forkChainEndHash = forkChain.getEndHash();
                NulsDigestData forkChainPreviousHash = forkChain.getPreviousHash();
                //1.直连,链尾
                if (blockHeight == forkChainEndHeight + 1 && blockPreviousHash.equals(forkChainEndHash)) {
                    chainStorageService.save(chainId, block);
                    forkChain.addLast(block);
                    Log.debug("chainId:{}, received continuous blocks of forkChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                    return Result.getFailed(BlockErrorCode.FORK_BLOCK);
                }
                //2.重复,丢弃
                if (forkChainStartHeight <= blockHeight && blockHeight <= forkChainEndHeight && forkChain.getHashList().contains(blockHash)) {
                    Log.debug("chainId:{}, received duplicate blocks of forkChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                    return Result.getFailed(BlockErrorCode.FORK_BLOCK);
                }
                //3.分叉
                if (forkChainStartHeight <= blockHeight && blockHeight <= forkChainEndHeight && forkChain.getHashList().contains(blockPreviousHash)) {
                    chainStorageService.save(chainId, block);
                    Chain newForkChain = ChainGenerator.generate(chainId, block, forkChain, ChainTypeEnum.FORK);
                    ChainManager.addForkChain(chainId, newForkChain);
                    Log.debug("chainId:{}, received evidence blocks of forkChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                    return Result.getFailed(BlockErrorCode.FORK_BLOCK);
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
        //4.与分叉链没有关联,进入孤儿链判断流程
        return Result.getFailed(BlockErrorCode.IRRELEVANT_BLOCK);
    }

    /**
     * 区块与孤儿链比对
     *
     * @param chainId
     * @param block
     * @return
     */
    private static Result orphanChainProcess(int chainId, Block block) {
        long blockHeight = block.getHeader().getHeight();
        NulsDigestData blockHash = block.getHeader().getHash();
        NulsDigestData blockPreviousHash = block.getHeader().getPreHash();
        SortedSet<Chain> orphanChains = ChainManager.getOrphanChains(chainId);
        try {
            for (Chain orphanChain : orphanChains) {
                long orphanChainStartHeight = orphanChain.getStartHeight();
                long orphanChainEndHeight = orphanChain.getEndHeight();
                NulsDigestData orphanChainEndHash = orphanChain.getEndHash();
                NulsDigestData orphanChainPreviousHash = orphanChain.getPreviousHash();
                //1.直连,分链头、链尾两种情况
                if (blockHeight == orphanChainEndHeight + 1 && blockPreviousHash.equals(orphanChainEndHash)) {
                    chainStorageService.save(chainId, block);
                    orphanChain.addLast(block);
                    Log.debug("chainId:{}, received continuous blocks of orphanChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                    return Result.getFailed(BlockErrorCode.ORPHAN_BLOCK);
                }
                if (blockHeight == orphanChainStartHeight - 1 && blockHash.equals(orphanChainPreviousHash)) {
                    chainStorageService.save(chainId, block);
                    orphanChain.addFirst(block);
                    Log.debug("chainId:{}, received continuous blocks of orphanChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                    return Result.getFailed(BlockErrorCode.ORPHAN_BLOCK);
                }
                //2.重复,丢弃
                if (orphanChainStartHeight <= blockHeight && blockHeight <= orphanChainEndHeight && orphanChain.getHashList().contains(blockHash)) {
                    Log.debug("chainId:{}, received duplicate blocks of orphanChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                    return Result.getFailed(BlockErrorCode.ORPHAN_BLOCK);
                }
                //3.分叉
                if (orphanChainStartHeight <= blockHeight && blockHeight <= orphanChainEndHeight && orphanChain.getHashList().contains(blockPreviousHash)) {
                    chainStorageService.save(chainId, block);
                    Chain forkOrphanChain = ChainGenerator.generate(chainId, block, orphanChain, ChainTypeEnum.ORPHAN);
                    forkOrphanChain.setAge(new AtomicInteger(0));
                    ChainManager.addOrphanChain(chainId, forkOrphanChain);
                    Log.debug("chainId:{}, received evidence blocks of orphanChain, height:{}, hash:{}", chainId, blockHeight, blockHash);
                    return Result.getFailed(BlockErrorCode.ORPHAN_BLOCK);
                }
            }
            //4.与主链、分叉链、孤儿链都无关,形成一个新的孤儿链
            chainStorageService.save(chainId, block);
            Chain newOrphanChain = ChainGenerator.generate(chainId, block, null, ChainTypeEnum.ORPHAN);
            newOrphanChain.setAge(new AtomicInteger(0));
            ChainManager.addOrphanChain(chainId, newOrphanChain);
            return Result.getFailed(BlockErrorCode.ORPHAN_BLOCK);
        } catch (Exception e) {
            Log.error(e);
        }
        return Result.getFailed(BlockErrorCode.UNDEFINED_ERROR);
    }

    public static SmallBlock getSmallBlock(int chainId, Block block) {
        ChainContext chainContext = ContextManager.getContext(chainId);
        List<Integer> systemTransactionType = chainContext.getSystemTransactionType();
        SmallBlock smallBlock = new SmallBlock();
        smallBlock.setHeader(block.getHeader());
        smallBlock.setTxHashList(block.getTxHashList());
        block.getTxs().stream().filter(e -> systemTransactionType.contains(e.getType())).forEach(e -> smallBlock.addBaseTx(e));
        return smallBlock;
    }

    /**
     * 根据smallblock和txmap组装一个完整区块
     *
     * @param header
     * @param txMap
     * @param txHashList
     * @return
     */
    public static Block assemblyBlock(BlockHeader header, Map<NulsDigestData, Transaction> txMap, List<NulsDigestData> txHashList) {
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for (NulsDigestData txHash : txHashList) {
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
        return po;
    }

}
