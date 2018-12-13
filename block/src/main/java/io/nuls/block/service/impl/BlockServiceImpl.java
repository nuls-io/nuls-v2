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

package io.nuls.block.service.impl;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.block.config.GenesisBlock;
import io.nuls.block.constant.CommandConstant;
import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.exception.DbRuntimeException;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashMessage;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.model.Chain;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.block.service.BlockService;
import io.nuls.block.service.BlockStorageService;
import io.nuls.block.service.ChainStorageService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.block.utils.ChainGenerator;
import io.nuls.block.utils.module.ConsensusUtil;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.block.utils.module.TransactionUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 区块服务实现类
 * @author captain
 * @date 18-11-20 上午11:09
 * @version 1.0
 */
@Service
public class BlockServiceImpl implements BlockService {

    @Autowired
    private BlockStorageService blockStorageService;
    @Autowired
    private ChainStorageService chainStorageService;

    @Override
    public Block getGenesisBlock(int chainId) {
        return getBlock(chainId, 0);
    }

    @Override
    public Block getLatestBlock(int chainId) {
        return ContextManager.getContext(chainId).getLatestBlock();
    }

    @Override
    public BlockHeader getLatestBlockHeader(int chainId) {
        return ContextManager.getContext(chainId).getLatestBlock().getHeader();
    }

    @Override
    public BlockHeaderPo getBlockHeader(int chainId, long height) {
        try {
            return blockStorageService.query(chainId, height);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public List<BlockHeader> getBlockHeader(int chainId, long startHeight, long endHeight) {
        try {
            int size = (int) (endHeight - startHeight + 1);
            List<BlockHeader> list = new ArrayList<>(size);
            for (long i = startHeight; i <= endHeight; i++) {
                BlockHeaderPo blockHeaderPo = blockStorageService.query(chainId, i);
                BlockHeader blockHeader = BlockUtil.fromBlockHeaderPo(blockHeaderPo);
                if (blockHeader == null) {
                    return null;
                }
                list.add(blockHeader);
            }
            return list;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public BlockHeader getBlockHeader(int chainId, NulsDigestData hash) {
        BlockHeaderPo blockHeaderPo = blockStorageService.query(chainId, hash);
        return BlockUtil.fromBlockHeaderPo(blockHeaderPo);
    }

    @Override
    public Block getBlock(int chainId, NulsDigestData hash) {
        try {
            Block block = new Block();
            BlockHeaderPo blockHeaderPo = blockStorageService.query(chainId, hash);
            if (blockHeaderPo == null) {
                return null;
            }
            block.setHeader(BlockUtil.fromBlockHeaderPo(blockHeaderPo));
            List<Transaction> transactions = TransactionUtil.getTransactions(chainId, blockHeaderPo.getTxHashList());
            block.setTxs(transactions);
            return block;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public Block getBlock(int chainId, long height) {
        try {
            Block block = new Block();
            BlockHeaderPo blockHeaderPo = blockStorageService.query(chainId, height);
            if (blockHeaderPo == null) {
                return null;
            }
            block.setHeader(BlockUtil.fromBlockHeaderPo(blockHeaderPo));
            block.setTxs(TransactionUtil.getTransactions(chainId, blockHeaderPo.getTxHashList()));
            return block;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public List<Block> getBlock(int chainId, long startHeight, long endHeight) {
        try {
            List<Block> list = new ArrayList<>();
            for (long i = startHeight; i <= endHeight; i++) {
                Block block = getBlock(chainId, i);
                if (block == null) {
                    return null;
                }
                list.add(block);
            }
            return list;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public boolean saveBlock(int chainId, Block block) {
        return saveBlock(chainId, block, false);
    }

    private boolean saveBlock(int chainId, Block block, boolean localInit) {
        long height = block.getHeader().getHeight();
        //1.验证区块
        if (!verifyBlock(chainId, block, localInit)) {
            Log.error("verify block fail!chainId-{},height-{}", chainId, height);
            return false;
        }
        //2.设置最新高度，如果失败则恢复上一个高度
        if (!blockStorageService.setLatestHeight(chainId, height)) {
            Log.error("set latest height fail!chainId-{},height-{}", chainId, height);
            if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                throw new DbRuntimeException("setLatestHeight error!");
            }
            return false;
        }
        //3.保存区块头
        if (!blockStorageService.save(chainId, BlockUtil.toBlockHeaderPo(block))) {
            Log.error("save blockheader fail!chainId-{},height-{}", chainId, height);
            if (!blockStorageService.remove(chainId, height)) {
                throw new DbRuntimeException("save blockheader error!");
            }
            return false;
        }
        //4.保存交易
        if (!TransactionUtil.save(chainId, block.getTxs())) {
            Log.info("save transactions fail!chainId-{},height-{}", chainId, height);
            if (!blockStorageService.remove(chainId, height)) {
                throw new DbRuntimeException("save blockheader error!");
            }
            if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                throw new DbRuntimeException("setLatestHeight error!");
            }
            return false;
        }
        //5.如果不是第一次启动，则更新主链属性
        if (!localInit) {
            ContextManager.getContext(chainId).setLatestBlock(block);
            Chain masterChain = ChainManager.getMasterChain(chainId);
            masterChain.setEndHeight(masterChain.getEndHeight() + 1);
            int heightRange = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.HEIGHT_RANGE));
            LinkedList<NulsDigestData> hashList = masterChain.getHashList();
            if (hashList.size() > heightRange) {
                hashList.removeFirst();
            }
            hashList.addLast(block.getHeader().getHash());
        }
        Log.debug("save block success, height-{}, hash-{}, preHash-{}", height, block.getHeader().getHash(), block.getHeader().getPreHash());
        return true;
    }

    @Override
    public boolean rollbackBlock(int chainId, BlockHeaderPo blockHeaderPo) {
        return rollbackBlock(chainId, blockHeaderPo, false);
    }

    private boolean rollbackBlock(int chainId, BlockHeaderPo blockHeaderPo, boolean localInit) {
        long height = blockHeaderPo.getHeight();
        if (!TransactionUtil.rollback(chainId, blockHeaderPo.getTxHashList())) {
            Log.error("rollback transactions fail!chainId-{},height-{}", chainId, height);
            return false;
        }
        if (!blockStorageService.remove(chainId, height)) {
            Log.error("rollback blockheader fail!chainId-{},height-{}", chainId, height);
            if (!blockStorageService.save(chainId, blockHeaderPo)) {
                throw new DbRuntimeException("rollback blockheader error!");
            }
            return false;
        }
        if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
            Log.error("rollback setLatestHeight fail!chainId-{},height-{}", chainId, height);
            if (!blockStorageService.save(chainId, blockHeaderPo)) {
                throw new DbRuntimeException("rollback blockheader error!");
            }
            if (!blockStorageService.setLatestHeight(chainId, height)) {
                throw new DbRuntimeException("rollback setLatestHeight error!");
            }
            return false;
        }
        if (!localInit) {
            ContextManager.getContext(chainId).setLatestBlock(getBlock(chainId, height - 1));
            Chain masterChain = ChainManager.getMasterChain(chainId);
            masterChain.setEndHeight(height - 1);
            masterChain.getHashList().pollLast();
        }
        return true;
    }

    @Override
    public boolean forwardBlock(int chainId, NulsDigestData hash, String excludeNode) {
        HashMessage message = new HashMessage(hash);
        message.setCommand(CommandConstant.FORWARD_SMALL_BLOCK_MESSAGE);
        return NetworkUtil.broadcast(chainId, message, excludeNode);
    }

    @Override
    public boolean broadcastBlock(int chainId, Block block) {
        SmallBlockMessage message = new SmallBlockMessage();
        message.setSmallBlock(BlockUtil.getSmallBlock(chainId, block));
        message.setCommand(CommandConstant.SMALL_BLOCK_MESSAGE);
        return NetworkUtil.broadcast(chainId, message);
    }

    @Override
    public boolean startChain(int chainId) {
        return false;
    }

    @Override
    public boolean stopChain(int chainId, boolean cleanData) {
        return false;
    }

    private boolean verifyBlock(int chainId, Block block, boolean localInit) {
        //1.验证一些基本信息如区块大小限制、字段非空验证
        boolean basicVerify = BlockUtil.basicVerify(chainId, block);
        if (!basicVerify) {
            return false;
        }
        //2.分叉验证逻辑
        if (!localInit) {
            boolean forkVerify = BlockUtil.forkVerify(chainId, block);
            if (!forkVerify) {
                return false;
            }
        }
        //3.共识验证
        boolean consensusVerify = ConsensusUtil.verify(chainId, block);
        if (!consensusVerify) {
            return false;
        }
        return true;
    }

    private boolean initLocalBlocks(int chainId) {
        Block block = null;
        Block genesisBlock = null;
        try {
            genesisBlock = getGenesisBlock(chainId);
            //1.判断有没有创世块，如果没有就初始化创世块并保存
            if (null == genesisBlock) {
                genesisBlock = GenesisBlock.getInstance();
                saveBlock(chainId, genesisBlock, true);
            }

            //2.获取缓存的最新区块高度（缓存的最新高度与实际的最新高度最多相差1，理论上不会有相差多个高度的情况，所以异常场景也只考虑了高度相差1）
            long latestHeight = blockStorageService.queryLatestHeight(chainId);

            //3.查询有没有这个高度的区块头
            BlockHeaderPo blockHeader = blockStorageService.query(chainId, latestHeight);
            //如果没有对应高度的header，说明缓存的本地高度错误，更新高度
            if (blockHeader == null) {
                latestHeight = latestHeight -1;
                blockStorageService.setLatestHeight(chainId, latestHeight);
            } else {
                //如果有对应高度的header，继续检查是否有对应高度的所有transaction
                List<Transaction> transactions = TransactionUtil.getTransactions(chainId, blockHeader.getTxHashList());
                //没有这个高度的交易，只回滚区块头
                if (transactions == null || transactions.size() == 0) {
                    blockStorageService.remove(chainId, latestHeight);
                    latestHeight = latestHeight -1;
                    blockStorageService.setLatestHeight(chainId, latestHeight);
                } else {
                    NulsDigestData merkleHash = NulsDigestData.calcMerkleDigestData(transactions.stream().map(e -> e.getHash()).collect(Collectors.toList()));
                    NulsDigestData blockMerkleHash = blockHeader.getMerkleHash();
                    //merkleHash不一致，回滚区块头，回滚交易
                    if (!merkleHash.equals(blockMerkleHash)) {
                        blockStorageService.remove(chainId, latestHeight);
                        TransactionUtil.rollback(chainId, blockHeader.getTxHashList());
                        latestHeight = latestHeight - 1;
                        blockStorageService.setLatestHeight(chainId, latestHeight);
                    }
                }
            }

            //4.latestHeight已经维护成功，上面的步骤保证了latestHeight这个高度的区块数据在本地是完整的，但是区块数据的内容并不一定是正确的，所以要继续验证latestBlock
            block = getBlock(chainId, latestHeight);
            //系统初始化时，区块的验证跳过分叉链验证，因为此时主链还没有加载完成，无法进行分叉链判断
            while (null != block && !verifyBlock(chainId, block, true)) {
                rollbackBlock(chainId, BlockUtil.toBlockHeaderPo(block), true);
                block = getBlock(chainId, block.getHeader().getPreHash());
            }
            //5.本地区块维护成功
            ContextManager.getContext(chainId).setLatestBlock(block);
            ContextManager.getContext(chainId).setGenesisBlock(genesisBlock);
            ChainManager.setMasterChain(chainId, ChainGenerator.generateMasterChain(chainId, block));
        } catch (Exception e) {
            Log.error(e);
        }
        return null != block;
    }

    @Override
    public void init(int chainId) {
        try {
            blockStorageService.init(chainId);
            chainStorageService.init(chainId);
            initLocalBlocks(chainId);
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
