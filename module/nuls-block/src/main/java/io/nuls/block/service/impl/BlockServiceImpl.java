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

package io.nuls.block.service.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.*;
import io.nuls.base.data.po.BlockHeaderPo;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.BlockChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashMessage;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.model.Chain;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.model.GenesisBlock;
import io.nuls.block.rpc.call.*;
import io.nuls.block.service.BlockService;
import io.nuls.block.storage.BlockStorageService;
import io.nuls.block.storage.ChainStorageService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.block.utils.ChainGenerator;
import io.nuls.block.utils.LoggerUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.base.data.BlockHeader.BLOCK_HEADER_COMPARATOR;
import static io.nuls.block.constant.CommandConstant.*;
import static io.nuls.block.constant.Constant.BLOCK_HEADER_INDEX;

/**
 * 区块服务实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Component
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
    public BlockHeaderPo getLatestBlockHeaderPo(int chainId) {
        ChainContext context = ContextManager.getContext(chainId);
        return getBlockHeaderPo(chainId, context.getLatestHeight());
    }

    @Override
    public BlockHeader getBlockHeader(int chainId, long height) {
        return BlockUtil.fromBlockHeaderPo(getBlockHeaderPo(chainId, height));
    }

    @Override
    public BlockHeaderPo getBlockHeaderPo(int chainId, long height) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            return blockStorageService.query(chainId, height);
        } catch (Exception e) {
            commonLog.error("", e);
            return null;
        }
    }

    @Override
    public List<BlockHeader> getBlockHeader(int chainId, long startHeight, long endHeight) {
        if (startHeight < 0 || endHeight < 0 || startHeight > endHeight) {
            return Collections.emptyList();
        }
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            int size = (int) (endHeight - startHeight + 1);
            List<BlockHeader> list = new ArrayList<>(size);
            for (long i = startHeight; i <= endHeight; i++) {
                BlockHeaderPo blockHeaderPo = blockStorageService.query(chainId, i);
                BlockHeader blockHeader = BlockUtil.fromBlockHeaderPo(blockHeaderPo);
                if (blockHeader == null) {
                    return Collections.emptyList();
                }
                list.add(blockHeader);
            }
            return list;
        } catch (Exception e) {
            commonLog.error("", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<BlockHeader> getBlockHeaderByRound(int chainId, long height, int round) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        try {
            int count = 0;
            BlockHeaderPo startHeaderPo = getBlockHeaderPo(chainId, height);
            byte[] extend = startHeaderPo.getExtend();
            BlockExtendsData data = new BlockExtendsData(extend);
            long roundIndex = data.getRoundIndex();
            List<BlockHeader> blockHeaders = new ArrayList<>();
            if (startHeaderPo.isComplete()) {
                blockHeaders.add(BlockUtil.fromBlockHeaderPo(startHeaderPo));
            }
            while (true) {
                height--;
                if ((height < 0)) {
                    break;
                }
                BlockHeader blockHeader = getBlockHeader(chainId, height);
                BlockExtendsData newData = new BlockExtendsData(blockHeader.getExtend());
                long newRoundIndex = newData.getRoundIndex();
                if (newRoundIndex != roundIndex) {
                    count++;
                    roundIndex = newRoundIndex;
                }
                if (count >= round) {
                    break;
                }
                blockHeaders.add(blockHeader);
            }
            blockHeaders.sort(BLOCK_HEADER_COMPARATOR);
            return blockHeaders;
        } catch (Exception e) {
            commonLog.error("", e);
            return Collections.emptyList();
        }
    }

    @Override
    public BlockHeader getBlockHeader(int chainId, NulsHash hash) {
        return BlockUtil.fromBlockHeaderPo(getBlockHeaderPo(chainId, hash));
    }

    @Override
    public BlockHeaderPo getBlockHeaderPo(int chainId, NulsHash hash) {
        return blockStorageService.query(chainId, hash);
    }

    @Override
    public Block getBlock(int chainId, NulsHash hash) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            Block block = new Block();
            BlockHeaderPo blockHeaderPo = blockStorageService.query(chainId, hash);
            if (blockHeaderPo == null) {
                commonLog.warn("hash-" + hash + " block not exists");
                return null;
            }
            block.setHeader(BlockUtil.fromBlockHeaderPo(blockHeaderPo));
            List<Transaction> transactions = TransactionUtil.getConfirmedTransactions(chainId, blockHeaderPo.getTxHashList());
            block.setTxs(transactions);
            return block;
        } catch (Exception e) {
            commonLog.error("",e);
            return null;
        }
    }

    @Override
    public Block getBlock(int chainId, long height) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            Block block = new Block();
            BlockHeaderPo blockHeaderPo = blockStorageService.query(chainId, height);
            if (blockHeaderPo == null) {
                return null;
            }
            block.setHeader(BlockUtil.fromBlockHeaderPo(blockHeaderPo));
            block.setTxs(TransactionUtil.getConfirmedTransactions(chainId, blockHeaderPo.getTxHashList()));
            return block;
        } catch (Exception e) {
            commonLog.error("", e);
            return null;
        }
    }

    @Override
    public List<Block> getBlock(int chainId, long startHeight, long endHeight) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            List<Block> list = new ArrayList<>();
            for (long i = startHeight; i <= endHeight; i++) {
                Block block = getBlock(chainId, i);
                if (block == null) {
                    return Collections.emptyList();
                }
                list.add(block);
            }
            return list;
        } catch (Exception e) {
            commonLog.error("", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean saveBlock(int chainId, Block block, boolean needLock) {
        return saveBlock(chainId, block, false, 0, needLock, false, false);
    }

    @Override
    public boolean saveBlock(int chainId, Block block, int download, boolean needLock, boolean broadcast, boolean forward) {
        return saveBlock(chainId, block, false, download, needLock, broadcast, forward);
    }

    private boolean saveBlock(int chainId, Block block, boolean localInit, int download, boolean needLock, boolean broadcast, boolean forward) {
        long startTime = System.nanoTime();
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        BlockHeader header = block.getHeader();
        long height = header.getHeight();
        NulsHash hash = header.getHash();
        StampedLock lock = context.getLock();
        long l = 0;
        if (needLock) {
            l = lock.writeLock();
        }
        try {
            //1.验证区块
            long startTime1 = System.nanoTime();
            Result result = verifyBlock(chainId, block, localInit, download);
            if (result.isFailed()) {
                commonLog.debug("verifyBlock fail!chainId-" + chainId + ",height-" + height);
                return false;
            }
            long elapsedNanos1 = System.nanoTime() - startTime1;
            commonLog.debug("1. verifyBlock time-" + elapsedNanos1);
            //2.设置最新高度,如果失败则恢复上一个高度
            boolean setHeight = blockStorageService.setLatestHeight(chainId, height);
            if (!setHeight) {
                if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                    throw new NulsRuntimeException(BlockErrorCode.UPDATE_HEIGHT_ERROR);
                }
                commonLog.error("setHeight false, chainId-" + chainId + ",height-" + height);
                return false;
            }

            //3.保存区块头, 保存交易
            long startTime3 = System.nanoTime();
            BlockHeaderPo blockHeaderPo = BlockUtil.toBlockHeaderPo(block);
            boolean headerSave = false;
            boolean txSave = false;
            if (!(headerSave = blockStorageService.save(chainId, blockHeaderPo)) || !(txSave = TransactionUtil.save(chainId, blockHeaderPo, block.getTxs(), localInit, (List) result.getData()))) {
                if (!blockStorageService.remove(chainId, height)) {
                    throw new NulsRuntimeException(BlockErrorCode.HEADER_REMOVE_ERROR);
                }
                if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                    throw new NulsRuntimeException(BlockErrorCode.UPDATE_HEIGHT_ERROR);
                }
                commonLog.error("headerSave-" + headerSave + ", txsSave-" + txSave + ", chainId-" + chainId + ", height-" + height + ", hash-" + hash);
                return false;
            }
            long elapsedNanos3 = System.nanoTime() - startTime3;
            commonLog.debug("2. headerSave and txsSave time-" + elapsedNanos3);

            //4.通知共识模块
            boolean csNotice = ConsensusUtil.saveNotice(chainId, header, localInit);
            if (!csNotice) {
                if (!TransactionUtil.rollback(chainId, blockHeaderPo)) {
                    throw new NulsRuntimeException(BlockErrorCode.TX_ROLLBACK_ERROR);
                }
                if (!blockStorageService.remove(chainId, height)) {
                    throw new NulsRuntimeException(BlockErrorCode.HEADER_REMOVE_ERROR);
                }
                if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                    throw new NulsRuntimeException(BlockErrorCode.UPDATE_HEIGHT_ERROR);
                }
                commonLog.error("csNotice false!chainId-" + chainId + ",height-" + height);
                return false;
            }

            //5.通知协议升级模块,完全保存,更新标记
            blockHeaderPo.setComplete(true);
            if (!ProtocolUtil.saveNotice(chainId, header) || !blockStorageService.save(chainId, blockHeaderPo) || !TransactionUtil.heightNotice(chainId, height)) {
                if (!ConsensusUtil.rollbackNotice(chainId, height)) {
                    throw new NulsRuntimeException(BlockErrorCode.CS_ROLLBACK_ERROR);
                }
                if (!TransactionUtil.rollback(chainId, blockHeaderPo)) {
                    throw new NulsRuntimeException(BlockErrorCode.TX_ROLLBACK_ERROR);
                }
                if (!blockStorageService.remove(chainId, height)) {
                    throw new NulsRuntimeException(BlockErrorCode.HEADER_REMOVE_ERROR);
                }
                if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                    throw new NulsRuntimeException(BlockErrorCode.UPDATE_HEIGHT_ERROR);
                }
                commonLog.error("ProtocolUtil saveNotice fail!chainId-" + chainId + ",height-" + height);
                return false;
            }
            try {
                CrossChainUtil.heightNotice(chainId, height, RPCUtil.encode(block.getHeader().serialize()));
            }catch (Exception e){
                LoggerUtil.COMMON_LOG.error(e);
            }

            //6.如果不是第一次启动,则更新主链属性
            if (!localInit) {
                context.setLatestBlock(block);
                Chain masterChain = BlockChainManager.getMasterChain(chainId);
                masterChain.setEndHeight(masterChain.getEndHeight() + 1);
                int heightRange = context.getParameters().getHeightRange();
                Deque<NulsHash> hashList = masterChain.getHashList();
                if (hashList.size() >= heightRange) {
                    hashList.removeFirst();
                }
                hashList.addLast(hash);
            }
            //同步\链切换\孤儿链对接过程中不进行区块广播
            if (download == 1) {
                if (broadcast) {
                    broadcastBlock(chainId, block);
                }
                if (forward) {
                    forwardBlock(chainId, hash, null);
                }
            }
            Response response = MessageUtil.newSuccessResponse("");
            Map<String, Long> responseData = new HashMap<>(2);
            responseData.put("value", height);
            Map<String, Object> sss = new HashMap<>(2);
            sss.put(LATEST_HEIGHT, responseData);
            response.setResponseData(sss);
            ConnectManager.eventTrigger(LATEST_HEIGHT, response);
            context.setNetworkHeight(height);
            long elapsedNanos = System.nanoTime() - startTime;
            commonLog.info("save block success, time-" + elapsedNanos + ", height-" + height + ", txCount-" + blockHeaderPo.getTxCount() + ", hash-" + hash + ", size-" + block.size());
            return true;
        } finally {
            if (needLock) {
                lock.unlockWrite(l);
            }
        }
    }

    @Override
    public boolean rollbackBlock(int chainId, long height, boolean needLock) {
        BlockHeaderPo blockHeaderPo = getBlockHeaderPo(chainId, height);
        return rollbackBlock(chainId, blockHeaderPo, needLock);
    }

    @Override
    public boolean rollbackBlock(int chainId, BlockHeaderPo blockHeaderPo, boolean needLock) {
        long startTime = System.nanoTime();
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        long height = blockHeaderPo.getHeight();
        if (height == 0) {
            commonLog.warn("can't rollback GenesisBlock!chainId-" + chainId);
            return true;
        }
        StampedLock lock = context.getLock();
        long l = 0;
        if (needLock) {
            l = lock.writeLock();
        }
        try {
            BlockHeader blockHeader = BlockUtil.fromBlockHeaderPo(blockHeaderPo);
            long startTime1 = System.nanoTime();
            blockHeaderPo.setComplete(false);
            if (!TransactionUtil.heightNotice(chainId, height - 1) || !blockStorageService.save(chainId, blockHeaderPo) || !ProtocolUtil.rollbackNotice(chainId, blockHeader)) {
                commonLog.error("ProtocolUtil rollbackNotice fail!chainId-" + chainId + ",height-" + height);
                return false;
            }

            if (!ConsensusUtil.rollbackNotice(chainId, height)) {
                if (!ProtocolUtil.saveNotice(chainId, blockHeader)) {
                    throw new NulsRuntimeException(BlockErrorCode.PU_SAVE_ERROR);
                }
                commonLog.error("ConsensusUtil rollbackNotice fail!chainId-" + chainId + ",height-" + height);
                return false;
            }
            long elapsedNanos1 = System.nanoTime() - startTime1;
            commonLog.debug("1. time-" + elapsedNanos1);

            long startTime2 = System.nanoTime();
            List<NulsHash> csTxHashList = ContractCall.contractOfflineTxHashList(chainId, blockHeader.getHash().toHex());
            List<NulsHash> txHashList = blockHeaderPo.getTxHashList();
            if (!csTxHashList.isEmpty()) {
                int last = txHashList.size() - 1;
                NulsHash hashLast = txHashList.get(last);
                Transaction confirmedTransaction = TransactionUtil.getConfirmedTransaction(chainId, hashLast);
                if (confirmedTransaction.getType() == TxType.CONTRACT_RETURN_GAS) {
                    txHashList.remove(last);
                    txHashList.addAll(csTxHashList);
                    txHashList.add(hashLast);
                } else {
                    txHashList.addAll(csTxHashList);
                }
            }
            if (!TransactionUtil.rollback(chainId, blockHeaderPo)) {
                if (!ConsensusUtil.saveNotice(chainId, blockHeader, false)) {
                    throw new NulsRuntimeException(BlockErrorCode.CS_SAVE_ERROR);
                }
                if (!ProtocolUtil.saveNotice(chainId, blockHeader)) {
                    throw new NulsRuntimeException(BlockErrorCode.PU_SAVE_ERROR);
                }
                commonLog.error("TransactionUtil rollback fail!chainId-" + chainId + ",height-" + height);
                return false;
            }
            long elapsedNanos2 = System.nanoTime() - startTime2;
            commonLog.debug("2. time-" + elapsedNanos2);

            long startTime3 = System.nanoTime();
            if (!blockStorageService.remove(chainId, height)) {
                blockHeaderPo.setComplete(true);
                if (!blockStorageService.save(chainId, blockHeaderPo)) {
                    throw new NulsRuntimeException(BlockErrorCode.HEADER_SAVE_ERROR);
                }
                //todo 待确认
                if (!TransactionUtil.saveNormal(chainId, blockHeaderPo, TransactionUtil.getTransactions(chainId, blockHeaderPo.getTxHashList(), true), null)) {
                    throw new NulsRuntimeException(BlockErrorCode.TX_SAVE_ERROR);
                }
                if (!ConsensusUtil.saveNotice(chainId, blockHeader, false)) {
                    throw new NulsRuntimeException(BlockErrorCode.CS_SAVE_ERROR);
                }
                if (!ProtocolUtil.saveNotice(chainId, blockHeader)) {
                    throw new NulsRuntimeException(BlockErrorCode.PU_SAVE_ERROR);
                }
                commonLog.error("blockStorageService remove fail!chainId-" + chainId + ",height-" + height);
                return false;
            }
            if (!blockStorageService.setLatestHeight(chainId, height - 1)) {
                if (!blockStorageService.setLatestHeight(chainId, height)) {
                    throw new NulsRuntimeException(BlockErrorCode.UPDATE_HEIGHT_ERROR);
                }
                blockHeaderPo.setComplete(true);
                if (!blockStorageService.save(chainId, blockHeaderPo)) {
                    throw new NulsRuntimeException(BlockErrorCode.HEADER_SAVE_ERROR);
                }
                //todo 待确认
                if (!TransactionUtil.saveNormal(chainId, blockHeaderPo, TransactionUtil.getTransactions(chainId, blockHeaderPo.getTxHashList(), true), null)) {
                    throw new NulsRuntimeException(BlockErrorCode.TX_SAVE_ERROR);
                }
                if (!ConsensusUtil.saveNotice(chainId, blockHeader, false)) {
                    throw new NulsRuntimeException(BlockErrorCode.CS_SAVE_ERROR);
                }
                if (!ProtocolUtil.saveNotice(chainId, blockHeader)) {
                    throw new NulsRuntimeException(BlockErrorCode.PU_SAVE_ERROR);
                }
                commonLog.error("rollback setLatestHeight fail!chainId-" + chainId + ",height-" + height);
                return false;
            }
            long elapsedNanos3 = System.nanoTime() - startTime3;
            commonLog.debug("3. time-" + elapsedNanos3);
            context.setLatestBlock(getBlock(chainId, height - 1));
            Chain masterChain = BlockChainManager.getMasterChain(chainId);
            masterChain.setEndHeight(height - 1);
            Deque<NulsHash> hashList = masterChain.getHashList();
            hashList.removeLast();
            int heightRange = context.getParameters().getHeightRange();
            if (height - heightRange >= 0) {
                hashList.addFirst(getBlockHash(chainId, height - heightRange));
            }
            long elapsedNanos = System.nanoTime() - startTime;
            commonLog.info("rollback block success, time-" + elapsedNanos + ", height-" + height + ", txCount-" + blockHeaderPo.getTxCount() + ", hash-" + blockHeaderPo.getHash());
            Response response = MessageUtil.newSuccessResponse("");
            Map<String, Long> responseData = new HashMap<>(2);
            responseData.put("value", height - 1);
            Map<String, Object> sss = new HashMap<>(2);
            sss.put(LATEST_HEIGHT, responseData);
            response.setResponseData(sss);
            ConnectManager.eventTrigger(LATEST_HEIGHT, response);
            return true;
        } catch (NulsException e) {
            return false;
        } finally {
            if (needLock) {
                lock.unlockWrite(l);
            }
        }
    }

    @Override
    public boolean forwardBlock(int chainId, NulsHash hash, String excludeNode) {
        HashMessage message = new HashMessage(hash);
        return NetworkUtil.broadcast(chainId, message, excludeNode, FORWARD_SMALL_BLOCK_MESSAGE);
    }

    @Override
    public boolean broadcastBlock(int chainId, Block block) {
        NulsLogger messageLog = ContextManager.getContext(chainId).getLogger();
        SmallBlockMessage message = new SmallBlockMessage();
        message.setSmallBlock(BlockUtil.getSmallBlock(chainId, block));
        boolean broadcast = NetworkUtil.broadcast(chainId, message, SMALL_BLOCK_MESSAGE);
        messageLog.debug("chainId-" + chainId + ", hash-" + block.getHeader().getHash() + ", broadcast-" + broadcast);
        return broadcast;
    }

    private Result verifyBlock(int chainId, Block block, boolean localInit, int download) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        BlockHeader header = block.getHeader();
        //0.版本验证：通过获取block中extends字段的版本号
        if (header.getHeight() > 0 && !ProtocolUtil.checkBlockVersion(chainId, header)) {
            commonLog.debug("checkBlockVersion failed! height-" + header.getHeight());
            return Result.getFailed(BlockErrorCode.BLOCK_VERIFY_ERROR);
        }

        //1.验证一些基本信息如区块大小限制、字段非空验证
        boolean basicVerify = BlockUtil.basicVerify(chainId, block);
        if (localInit) {
            commonLog.debug("basicVerify-" + basicVerify);
            if (basicVerify) {
                return Result.getSuccess(BlockErrorCode.SUCCESS);
            } else {
                return Result.getFailed(BlockErrorCode.BLOCK_VERIFY_ERROR);
            }
        }

        //分叉验证
        boolean forkVerify = BlockUtil.forkVerify(chainId, block);
        if (!forkVerify) {
            commonLog.debug("forkVerify-" + forkVerify);
            return Result.getFailed(BlockErrorCode.BLOCK_VERIFY_ERROR);
        }
        //共识验证
        Result consensusVerify = ConsensusUtil.verify(chainId, block, download);
        if (consensusVerify.isFailed()) {
            commonLog.debug("consensusVerify-" + consensusVerify);
            return Result.getFailed(BlockErrorCode.BLOCK_VERIFY_ERROR);
        }
        return consensusVerify;
    }

    private boolean initLocalBlocks(int chainId) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        Block block;
        Block genesisBlock;
        try {
            genesisBlock = getGenesisBlock(chainId);
            //1.判断有没有创世块,如果没有就初始化创世块并保存
            if (null == genesisBlock) {
                ChainParameters chainParameters = context.getParameters();
                if (StringUtils.isBlank(chainParameters.getGenesisBlockPath())) {
                    genesisBlock = GenesisBlock.getInstance(chainId, chainParameters.getAssetId());
                } else {
                    genesisBlock = GenesisBlock.getInstance(chainId, chainParameters.getAssetId(), Files.readString(Path.of(chainParameters.getGenesisBlockPath())));
                }
                boolean b = saveBlock(chainId, genesisBlock, true, 0, false, false, false);
                if (!b) {
                    throw new NulsRuntimeException(BlockErrorCode.SAVE_GENESIS_ERROR);
                }
            }

            //2.获取缓存的最新区块高度（缓存的最新高度与实际的最新高度最多相差1,理论上不会有相差多个高度的情况,所以异常场景也只考虑了高度相差1）
            long latestHeight = blockStorageService.queryLatestHeight(chainId);

            //3.查询有没有这个高度的区块头
            BlockHeaderPo blockHeader = blockStorageService.query(chainId, latestHeight);
            //如果没有对应高度的header,说明缓存的本地高度错误,更新高度
            if (blockHeader == null) {
                latestHeight = latestHeight - 1;
                blockStorageService.setLatestHeight(chainId, latestHeight);
            }
            //4.latestHeight已经维护成功,上面的步骤保证了latestHeight这个高度的区块数据在本地是完整的,但是区块数据的内容并不一定是正确的,区块同步之前会继续验证latestBlock
            block = getBlock(chainId, latestHeight);
            //5.本地区块维护成功
            context.setLatestBlock(block);
            context.setGenesisBlock(genesisBlock);
            BlockChainManager.setMasterChain(chainId, ChainGenerator.generateMasterChain(chainId, block, this));
        } catch (Exception e) {
            commonLog.error("", e);
            return false;
        }
        return true;
    }

    @Override
    public void init(int chainId) {
        boolean initLocalBlocks = initLocalBlocks(chainId);
        if (!initLocalBlocks) {
            throw new NulsRuntimeException(BlockErrorCode.INIT_ERROR);
        }
    }

    @Override
    public NulsHash getBlockHash(int chainId, long height) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            byte[] key = SerializeUtils.uint64ToByteArray(height);
            byte[] value = RocksDBService.get(BLOCK_HEADER_INDEX + chainId, key);
            if (value == null) {
                return null;
            }
            NulsHash hash = new NulsHash(value);
            return hash;
        } catch (Exception e) {
            commonLog.error("", e);
            return null;
        }
    }
}
