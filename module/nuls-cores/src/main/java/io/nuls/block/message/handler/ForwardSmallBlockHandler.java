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

import io.nuls.base.RPCUtil;
import io.nuls.base.data.NulsHash;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.message.HashMessage;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.TxGroupTask;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.thread.monitor.TxGroupRequestor;
import io.nuls.block.utils.SmallBlockCacher;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;

import java.util.List;

import static io.nuls.block.BlockBootstrap.blockConfig;
import static io.nuls.block.constant.CommandConstant.FORWARD_SMALL_BLOCK_MESSAGE;
import static io.nuls.block.constant.CommandConstant.GET_SMALL_BLOCK_MESSAGE;

/**
 * Process received{@link HashMessage},Broadcasting and forwarding for blocks
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 afternoon4:23
 */
@Component("ForwardSmallBlockHandlerV1")
public class ForwardSmallBlockHandler implements MessageProcessor {

    @Override
    public String getCmd() {
        return FORWARD_SMALL_BLOCK_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        HashMessage message = RPCUtil.getInstanceRpcStr(msgStr, HashMessage.class);
        if (message == null) {
            return;
        }
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        NulsHash blockHash = message.getRequestHash();
        Long height = context.getCachedHashHeightMap().get(blockHash);
        if (height != null) {
            NetworkCall.setHashAndHeight(chainId, blockHash, height, nodeId);
        }
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
//        logger.debug("recieve " + message + " from node-" + nodeId + ", hash:" + blockHash);
        List<String> nodes = context.getOrphanBlockRelatedNodes().get(blockHash);
        if (nodes != null && !nodes.contains(nodeId)) {
            nodes.add(nodeId);
            logger.debug("add OrphanBlockRelatedNodes, blockHash-{}, nodeId-{}", blockHash, nodeId);
        }
        //1.Received complete block,discard
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return;
        }
        //2.Received partial blocks,Transaction information is still missing,sendHashListMessageTo source node
        if (BlockForwardEnum.INCOMPLETE.equals(status) && !context.getStatus().equals(StatusEnum.SYNCHRONIZING)) {
            CachedSmallBlock block = SmallBlockCacher.getCachedSmallBlock(chainId, blockHash);
            if (block == null) {
                return;
            }
            HashListMessage request = new HashListMessage();
            request.setBlockHash(blockHash);
            request.setTxHashList(block.getMissingTransactions());
            TxGroupTask task = new TxGroupTask();
            task.setId(System.nanoTime());
            task.setNodeId(nodeId);
            task.setRequest(request);
            task.setExcuteTime(blockConfig.getTxGroupTaskDelay());
            TxGroupRequestor.addTask(chainId, blockHash.toString(), task);
            return;
        }
        //3.Block not received
        if (BlockForwardEnum.EMPTY.equals(status)) {
            HashMessage request = new HashMessage();
            request.setRequestHash(blockHash);
            NetworkCall.sendToNode(chainId, request, nodeId, GET_SMALL_BLOCK_MESSAGE);
        }
    }
}
