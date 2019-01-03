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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.block.constant.CommandConstant;
import io.nuls.block.exception.ChainRuntimeException;
import io.nuls.block.exception.DbRuntimeException;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashMessage;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.GenesisBlock;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.block.service.BlockService;
import io.nuls.block.service.BlockStorageService;
import io.nuls.block.service.ChainStorageService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.block.utils.ChainGenerator;
import io.nuls.block.utils.module.ConsensusUtil;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.block.utils.module.TransactionUtil;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.nuls.block.constant.Constant.*;

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
        return saveBlock(chainId, block, false, 0);
    }

    @Override
    public boolean saveBlock(int chainId, Block block, int download) {
        return saveBlock(chainId, block, false, download);
    }

    private boolean saveBlock(int chainId, Block block, boolean localInit, int download) {
        BlockHeader header = block.getHeader();
        long height = header.getHeight();
        NulsDigestData hash = header.getHash();
        ChainContext context = ContextManager.getContext(chainId);
        ReentrantReadWriteLock.WriteLock writeLock = context.getWriteLock();
        boolean lock;
        try {
            lock = writeLock.tryLock(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new ChainRuntimeException(e.getMessage());
        }
        if (lock) {
            //1.验证区块
            if (!verifyBlock(chainId, block, localInit, download)) {
                Log.error("verify block fail!chainId-{},height-{}", chainId, height);
                return false;
            }
            //2.设置最新高度,如果失败则恢复上一个高度
            if (!blockStorageService.setLatestHeight(chainId, height)) {
                Log.error("set latest height fail!chainId-{},height-{}", chainId, height);
                if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                    throw new DbRuntimeException("setLatestHeight error!");
                }
                return false;
            }
            //3.保存区块头,保存交易
            BlockHeaderPo blockHeaderPo = BlockUtil.toBlockHeaderPo(block);
            if (!blockStorageService.save(chainId, blockHeaderPo) || !TransactionUtil.save(chainId, block.getTxHashList())) {
                Log.error("save blockheader fail!chainId-{},height-{}", chainId, height);
                if (!blockStorageService.remove(chainId, height)) {
                    throw new DbRuntimeException("remove blockheader error!");
                }
                if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                    throw new DbRuntimeException("setLatestHeight error!");
                }
                return false;
            }
            //4.保存区块头,完全保存,更新标记
            blockHeaderPo.setComplete(true);
            if (!ConsensusUtil.newBlock(chainId, header, localInit) || !blockStorageService.save(chainId, blockHeaderPo)) {
                Log.error("update blockheader fail!chainId-{},height-{}", chainId, height);
                if (!TransactionUtil.rollback(chainId, block.getTxHashList())) {
                    throw new DbRuntimeException("remove transactions error!");
                }
                if (!blockStorageService.remove(chainId, height)) {
                    throw new DbRuntimeException("remove blockheader error!");
                }
                if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                    throw new DbRuntimeException("setLatestHeight error!");
                }
                return false;
            }
            //5.如果不是第一次启动,则更新主链属性
            if (!localInit) {
                context.setLatestBlock(block);
                Chain masterChain = ChainManager.getMasterChain(chainId);
                masterChain.setEndHeight(masterChain.getEndHeight() + 1);
                int heightRange = context.getParameters().getHeightRange();
                LinkedList<NulsDigestData> hashList = masterChain.getHashList();
                if (hashList.size() > heightRange) {
                    hashList.removeFirst();
                }
                hashList.addLast(hash);
            }
            Log.info("save block success, height-{}, hash-{}, preHash-{}", height, hash, header.getPreHash());
            writeLock.unlock();
            return true;
        } else {
            return saveBlock(chainId, block, localInit, download);
        }
    }

    @Override
    public boolean rollbackBlock(int chainId, BlockHeaderPo blockHeaderPo) {
        return rollbackBlock(chainId, blockHeaderPo, false);
    }

    @Override
    public boolean rollbackBlock(int chainId, long height) {
        BlockHeaderPo blockHeaderPo = getBlockHeader(chainId, height);
        return rollbackBlock(chainId, blockHeaderPo, false);
    }

    private boolean rollbackBlock(int chainId, BlockHeaderPo blockHeaderPo, boolean localInit) {
        long height = blockHeaderPo.getHeight();
        ChainContext context = ContextManager.getContext(chainId);
        ReentrantReadWriteLock.WriteLock writeLock = context.getWriteLock();
        boolean lock;
        try {
            lock = writeLock.tryLock(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new ChainRuntimeException(e.getMessage());
        }
        if (lock) {
            if (!TransactionUtil.rollback(chainId, blockHeaderPo.getTxHashList())) {
                Log.error("rollback transactions fail!chainId-{},height-{}", chainId, height);
                return false;
            }
            if (!blockStorageService.remove(chainId, height)) {
                Log.error("rollback blockheader fail!chainId-{},height-{}", chainId, height);
                if (!TransactionUtil.save(chainId, blockHeaderPo.getTxHashList())) {
                    throw new DbRuntimeException("rollback blockheader error!");
                }
                if (!blockStorageService.save(chainId, blockHeaderPo)) {
                    throw new DbRuntimeException("rollback blockheader error!");
                }
                return false;
            }
            if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                Log.error("rollback setLatestHeight fail!chainId-{},height-{}", chainId, height);
                if (!TransactionUtil.save(chainId, blockHeaderPo.getTxHashList())) {
                    throw new DbRuntimeException("rollback blockheader error!");
                }
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
            writeLock.unlock();
            return true;
        } else {
            return rollbackBlock(chainId, blockHeaderPo, localInit);
        }
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
        boolean broadcast = NetworkUtil.broadcast(chainId, message);
        if (!broadcast) {
            rollbackBlock(chainId, BlockUtil.toBlockHeaderPo(block));
        }
        return broadcast;
    }

    @Override
    public boolean startChain(int chainId) {
        return false;
    }

    @Override
    public boolean stopChain(int chainId, boolean cleanData) {
        return false;
    }

    private boolean verifyBlock(int chainId, Block block, boolean localInit, int download) {
        //1.验证一些基本信息如区块大小限制、字段非空验证
        boolean basicVerify = BlockUtil.basicVerify(chainId, block);
        if (!basicVerify) {
            return false;
        }

        if (localInit) {
            return TransactionUtil.verify(chainId, block.getTxs());
        }

        //分叉验证
        return BlockUtil.forkVerify(chainId, block)
                //共识验证
                && ConsensusUtil.verify(chainId, block, download)
                //交易验证
                && TransactionUtil.verify(chainId, block.getTxs());
    }

    private boolean initLocalBlocks(int chainId) {
        Block block = null;
        Block genesisBlock;
        try {
            genesisBlock = getGenesisBlock(chainId);
            //1.判断有没有创世块,如果没有就初始化创世块并保存
            if (null == genesisBlock) {
                genesisBlock = GenesisBlock.getInstance();
                saveBlock(chainId, genesisBlock, true, 0);
            }

            //2.获取缓存的最新区块高度（缓存的最新高度与实际的最新高度最多相差1,理论上不会有相差多个高度的情况,所以异常场景也只考虑了高度相差1）
            long latestHeight = blockStorageService.queryLatestHeight(chainId);

            //3.查询有没有这个高度的区块头
            BlockHeaderPo blockHeader = blockStorageService.query(chainId, latestHeight);
            //如果没有对应高度的header,说明缓存的本地高度错误,更新高度
            if (blockHeader == null) {
                latestHeight = latestHeight -1;
                blockStorageService.setLatestHeight(chainId, latestHeight);
            } else {
                if (!blockHeader.isComplete()) {
                    blockStorageService.remove(chainId, latestHeight);
                    latestHeight = latestHeight -1;
                    blockStorageService.setLatestHeight(chainId, latestHeight);
                }
            }
            //4.latestHeight已经维护成功,上面的步骤保证了latestHeight这个高度的区块数据在本地是完整的,但是区块数据的内容并不一定是正确的,所以要继续验证latestBlock
            block = getBlock(chainId, latestHeight);
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
            if (!RocksDBService.existTable(BLOCK_HEADER + chainId)) {
                RocksDBService.createTable(BLOCK_HEADER + chainId);
            }
            if (!RocksDBService.existTable(BLOCK_HEADER_INDEX + chainId)) {
                RocksDBService.createTable(BLOCK_HEADER_INDEX + chainId);
            }
            if (RocksDBService.existTable(CACHED_BLOCK + chainId)) {
                RocksDBService.destroyTable(CACHED_BLOCK + chainId);
            }
            RocksDBService.createTable(CACHED_BLOCK + chainId);
            initLocalBlocks(chainId);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Override
    public NulsDigestData getBlockHash(int chainId, long height) {
        try {
            byte[] key = SerializeUtils.uint64ToByteArray(height);
            byte[] value = RocksDBService.get(BLOCK_HEADER_INDEX + chainId, key);
            if (value == null) {
                return null;
            }
            NulsDigestData hash = new NulsDigestData();
            hash.parse(new NulsByteBuffer(value));
            return hash;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
}
