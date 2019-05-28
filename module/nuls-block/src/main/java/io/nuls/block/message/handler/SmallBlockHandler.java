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

package io.nuls.block.message.handler;

import io.nuls.base.data.*;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.rpc.call.NetworkUtil;
import io.nuls.block.rpc.call.TransactionUtil;
import io.nuls.block.service.BlockService;
import io.nuls.block.thread.TxGroupTask;
import io.nuls.block.thread.monitor.TxGroupRequestor;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.rpc.util.NulsDateUtils;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.BlockBootstrap.blockConfig;
import static io.nuls.block.constant.CommandConstant.GET_TXGROUP_MESSAGE;
import static io.nuls.block.constant.CommandConstant.SMALL_BLOCK_MESSAGE;

/**
 * 处理收到的{@link SmallBlockMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("SmallBlockHandlerV1")
public class SmallBlockHandler implements MessageProcessor {

    @Autowired
    private BlockService blockService;

    @Override
    public String getCmd() {
        return SMALL_BLOCK_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        ChainContext context = ContextManager.getContext(chainId);
        SmallBlockMessage message = RPCUtil.getInstanceRpcStr(msgStr, SmallBlockMessage.class);
        if (message == null) {
            return;
        }
        NulsLogger messageLog = context.getMessageLog();
        SmallBlock smallBlock = message.getSmallBlock();
        if (null == smallBlock) {
            messageLog.warn("recieved a null smallBlock!");
            return;
        }

        BlockHeader header = smallBlock.getHeader();
        NulsHash blockHash = header.getHash();
        //阻止恶意节点提前出块,拒绝接收未来一定时间外的区块
        ChainParameters parameters = context.getParameters();
        int validBlockInterval = parameters.getValidBlockInterval();
        long currentTime = NulsDateUtils.getCurrentTimeMillis();
        if (header.getTime() * 1000 > (currentTime + validBlockInterval)) {
            messageLog.error("header.getTime()-" + header.getTime() + ", currentTime-" + currentTime + ", validBlockInterval-" + validBlockInterval);
            return;
        }

        messageLog.debug("recieve smallBlockMessage from node-" + nodeId + ", chainId:" + chainId + ", height:" + header.getHeight() + ", hash:" + header.getHash());
        context.getCachedHashHeightMap().put(blockHash, header.getHeight());
        NetworkUtil.setHashAndHeight(chainId, blockHash, header.getHeight(), nodeId);
        if (context.getStatus().equals(StatusEnum.SYNCHRONIZING)) {
            return;
        }
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
        //1.已收到完整区块,丢弃
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return;
        }

        //2.已收到部分区块,还缺失交易信息,发送HashListMessage到源节点
        if (BlockForwardEnum.INCOMPLETE.equals(status)) {
            CachedSmallBlock block = SmallBlockCacher.getCachedSmallBlock(chainId, blockHash);
            List<NulsHash> missingTransactions = block.getMissingTransactions();
            if (missingTransactions == null) {
                return;
            }
            HashListMessage request = new HashListMessage();
            request.setBlockHash(blockHash);
            request.setTxHashList(missingTransactions);
            TxGroupTask task = new TxGroupTask();
            task.setId(System.nanoTime());
            task.setNodeId(nodeId);
            task.setRequest(request);
            task.setExcuteTime(blockConfig.getTxGroupTaskDelay());
            TxGroupRequestor.addTask(chainId, blockHash.toString(), task);
            return;
        }

        //3.未收到区块
        if (BlockForwardEnum.EMPTY.equals(status)) {
            if (!BlockUtil.headerVerify(chainId, header)) {
                messageLog.info("recieve error SmallBlockMessage from " + nodeId);
                return;
            }
            //共识节点打包的交易包括两种交易,一种是在网络上已经广播的普通交易,一种是共识节点生成的特殊交易(如共识奖励、红黄牌),后面一种交易其他节点的未确认交易池中不可能有,所以都放在systemTxList中
            //还有一种场景时收到smallBlock时,有一些普通交易还没有缓存在未确认交易池中,此时要再从源节点请求
            //txMap用来组装区块
            Map<NulsHash, Transaction> txMap = new HashMap<>(header.getTxCount());
            List<Transaction> systemTxList = smallBlock.getSystemTxList();
            List<NulsHash> systemTxHashList = new ArrayList<>();
            //先把系统交易放入txMap
            for (Transaction tx : systemTxList) {
                txMap.put(tx.getHash(), tx);
                systemTxHashList.add(tx.getHash());
            }
            ArrayList<NulsHash> txHashList = smallBlock.getTxHashList();
            List<NulsHash> missTxHashList = (List<NulsHash>) txHashList.clone();
            //移除系统交易hash后请求交易管理模块,批量获取区块中交易
            missTxHashList = ListUtils.removeAll(missTxHashList, systemTxHashList);

            List<Transaction> existTransactions = TransactionUtil.getTransactions(chainId, missTxHashList, false);
            if (existTransactions != null) {
                //把普通交易放入txMap
                List<NulsHash> existTransactionHashs = new ArrayList<>();
                existTransactions.forEach(e -> existTransactionHashs.add(e.getHash()));
                for (Transaction existTransaction : existTransactions) {
                    txMap.put(existTransaction.getHash(), existTransaction);
                }
                missTxHashList = ListUtils.removeAll(missTxHashList, existTransactionHashs);
            }

            //获取没有的交易
            if (!missTxHashList.isEmpty()) {
                messageLog.info("block height:" + header.getHeight() + ", total tx count:" + header.getTxCount() + " , get group tx of " + missTxHashList.size());
                //这里的smallBlock的subTxList中包含一些非系统交易,用于跟TxGroup组合成完整区块
                CachedSmallBlock cachedSmallBlock = new CachedSmallBlock(missTxHashList, smallBlock, txMap);
                SmallBlockCacher.cacheSmallBlock(chainId, cachedSmallBlock);
                SmallBlockCacher.setStatus(chainId, blockHash, BlockForwardEnum.INCOMPLETE);
                HashListMessage request = new HashListMessage();
                request.setBlockHash(blockHash);
                request.setTxHashList(missTxHashList);
                NetworkUtil.sendToNode(chainId, request, nodeId, GET_TXGROUP_MESSAGE);
                return;
            }

            CachedSmallBlock cachedSmallBlock = new CachedSmallBlock(null, smallBlock, txMap);
            SmallBlockCacher.cacheSmallBlock(chainId, cachedSmallBlock);
            SmallBlockCacher.setStatus(chainId, blockHash, BlockForwardEnum.COMPLETE);
            TxGroupRequestor.removeTask(chainId, blockHash.toString());
            Block block = BlockUtil.assemblyBlock(header, txMap, txHashList);
            blockService.saveBlock(chainId, block, 1, true, false, true);
        }
    }
}
