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
import io.nuls.block.manager.ContextManager;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.message.ForwardSmallBlockMessage;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.message.body.ForwardSmallBlockMessageBody;
import io.nuls.block.message.body.SmallBlockMessageBody;
import io.nuls.block.model.Chain;
import io.nuls.block.model.Node;
import io.nuls.block.service.BlockService;
import io.nuls.block.service.BlockStorageService;
import io.nuls.block.service.ChainStorageService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.block.utils.ConsensusUtil;
import io.nuls.block.utils.NetworkUtil;
import io.nuls.block.utils.TransactionUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
    public Block getGenesisBlock(int chainId) throws Exception {
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
    public BlockHeader getBlockHeader(int chainId, long height) throws UnsupportedEncodingException, NulsException {
        BlockHeader blockHeader = blockStorageService.query(chainId, height);
        if (blockHeader == null) {
            return null;
        }
        return blockHeader;
    }

    @Override
    public List<BlockHeader> getBlockHeader(int chainId, long startHeight, long endHeight) throws UnsupportedEncodingException, NulsException {
        List<BlockHeader> list = new ArrayList<>();
        for (long i = startHeight; i <= endHeight; i++) {
            BlockHeader blockHeader = blockStorageService.query(chainId, i);
            if (blockHeader == null) {
                return list;
            }
            list.add(blockHeader);
        }
        return list;
    }

    @Override
    public BlockHeader getBlockHeader(int chainId, NulsDigestData hash) throws NulsException {
        return blockStorageService.query(chainId, hash);
    }

    @Override
    public Block getBlock(int chainId, NulsDigestData hash) throws NulsException, IOException {
        BlockHeader blockHeader = blockStorageService.query(chainId, hash);
        Block block = new Block();
        block.setHeader(blockHeader);
        block.setTxs(TransactionUtil.getTransactions(chainId, blockHeader.getHeight()));
        return block;
    }

    @Override
    public Block getBlock(int chainId, long height) throws Exception {
        BlockHeader blockHeader = blockStorageService.query(chainId, height);
        if (blockHeader == null) {
            return null;
        }
        Block block = new Block();
        block.setHeader(blockHeader);
        block.setTxs(TransactionUtil.getTransactions(chainId, height));
        return block;
    }

    @Override
    public List<Block> getBlock(int chainId, long startHeight, long endHeight) throws IOException, NulsException {
        List<Block> list = new ArrayList<>();
        for (long i = startHeight; i <= endHeight; i++) {
            BlockHeader blockHeader = blockStorageService.query(chainId, i);
            if (blockHeader == null) {
                return list;
            }
            Block block = new Block();
            block.setHeader(blockHeader);
            block.setTxs(TransactionUtil.getTransactions(chainId, i));
            list.add(block);
        }
        return list;
    }

    @Override
    public boolean saveBlock(int chainId, Block block) throws Exception {

        if (!verifyBlock(chainId, block, block.getHeader().getHeight() == 0)) {
            Log.info("[save block]:verify block fail!chainId-{},height-{}", chainId, block.getHeader().getHeight());
            return false;
        }

        if (!blockStorageService.setLatestHeight(chainId, block.getHeader().getHeight())) {
            Log.info("[save block]:set latest height fail!chainId-{},height-{}", chainId, block.getHeader().getHeight());
            return false;
        }

        if (!blockStorageService.save(chainId, block.getHeader())) {
            Log.info("[save block]:save blockheader fail!chainId-{},height-{}", chainId, block.getHeader().getHeight());
            return false;
        }

        if (!TransactionUtil.save(chainId, block.getTxs())) {
            Log.info("[save block]:save transactions fail!chainId-{},height-{}", chainId, block.getHeader().getHeight());
            return false;
        }
        ContextManager.getContext(chainId).setLatestBlock(block);
        return true;
    }

    @Override
    public boolean rollbackBlock(int chainId, Block block) throws Exception {
        return false;
    }

    @Override
    public Block rollbackBlock(int chainId, long height) {
        BlockHeader header = null;
        Block block = new Block();
        try {
            header = blockStorageService.query(chainId, height);
            List<Transaction> transactions = TransactionUtil.getTransactions(chainId, height);
            TransactionUtil.rollback(chainId, height);
            block.setHeader(header);
            block.setTxs(transactions);
            blockStorageService.remove(chainId, height);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
        return block;
    }

    @Override
    public boolean forwardBlock(int chainId, NulsDigestData hash, Node excludeNode) {
        ForwardSmallBlockMessage message = new ForwardSmallBlockMessage();
        ForwardSmallBlockMessageBody body = new ForwardSmallBlockMessageBody();
        body.setChainID(chainId);
        body.setBlockHash(hash);
        message.setMsgBody(body);
        return NetworkUtil.broadcast(chainId, message, excludeNode.getId());
    }

    @Override
    public boolean broadcastBlock(int chainId, NulsDigestData hash) throws IOException, NulsException {
        SmallBlockMessage message = new SmallBlockMessage();
        SmallBlockMessageBody body = new SmallBlockMessageBody();
        body.setChainID(chainId);
        body.setSmallBlock(BlockUtil.getSmallBlock(getBlock(chainId, hash)));
        message.setMsgBody(body);
        return NetworkUtil.broadcast(chainId, message, "");
    }

    @Override
    public boolean startChain(int chainId) {
        return false;
    }

    @Override
    public boolean stopChain(int chainId, boolean cleanData) {
        return false;
    }

    @Override
    public boolean verifyBlock(int chainId, Block block) throws Exception {
        return verifyBlock(chainId, block, false);
    }

    public boolean verifyBlock(int chainId, Block block, boolean localInit) throws Exception {
        //1.验证一些基本信息如区块大小限制、字段非空验证
        boolean basicVerify = BlockUtil.basicVerify(chainId, block);
        if (!basicVerify) {
            return false;
        }
        //2.分叉验证逻辑
        if (!localInit) {
            Result result = BlockUtil.forkVerify(chainId, block);
            boolean forkVerify = result.isSuccess();
            if (!forkVerify) {
                return false;
            }
        }
        //3.共识验证
        boolean consensusVerify = ConsensusUtil.verify(chainId, block.getHeader());
        if (!consensusVerify) {
            return false;
        }
        //4.批量交易验证
        boolean transactionVerify = TransactionUtil.verify(chainId, block.getTxs());
        if (!transactionVerify) {
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
                saveBlock(chainId, genesisBlock);
            }

            //2.获取缓存的最新区块高度（缓存的最新高度与实际的最新高度最多相差1，理论上不会有相差多个高度的情况，所以异常场景也只考虑了高度相差1）
            long latestHeight = blockStorageService.queryLatestHeight(chainId);

            //3.查询有没有这个高度的区块头
            BlockHeader blockHeader = blockStorageService.query(chainId, latestHeight);
            //如果没有对应高度的header，说明缓存的本地高度错误，更新高度
            if (blockHeader == null) {
                latestHeight = latestHeight -1;
                blockStorageService.setLatestHeight(chainId, latestHeight);
            } else {
                //如果有对应高度的header，继续检查是否有对应高度的所有transaction
                List<Transaction> transactions = TransactionUtil.getTransactions(chainId, latestHeight);
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
                        TransactionUtil.rollback(chainId, transactions);
                        latestHeight = latestHeight - 1;
                        blockStorageService.setLatestHeight(chainId, latestHeight);
                    }
                }
            }

            //4.latestHeight已经维护成功，上面的步骤保证了latestHeight这个高度的区块数据在本地是完整的，但是区块数据的内容并不一定是正确的，所以要继续验证latestBlock
            block = getBlock(chainId, latestHeight);
            //系统初始化时，区块的验证跳过分叉链验证，因为此时主链还没有加载完成，无法进行分叉链判断
            while (null != block && !verifyBlock(chainId, block, true)) {
                rollbackBlock(chainId, block);
                block = getBlock(chainId, block.getHeader().getPreHash());
            }
            //5.本地区块维护成功
            ContextManager.getContext(chainId).setLatestBlock(block);
            ContextManager.getContext(chainId).setGenesisBlock(genesisBlock);
            ChainManager.setMasterChain(chainId, Chain.generateMasterChain(chainId, block));
        } catch (Exception e) {
            e.printStackTrace();
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
