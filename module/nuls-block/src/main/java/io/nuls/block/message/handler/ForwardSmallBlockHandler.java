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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.SmallBlock;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.message.HashMessage;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.model.ChainContext;
import io.nuls.block.rpc.call.NetworkUtil;
import io.nuls.block.thread.TxGroupTask;
import io.nuls.block.thread.monitor.TxGroupRequestor;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.protocol.MessageHandler;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.Map;

import static io.nuls.block.BlockBootstrap.blockConfig;
import static io.nuls.block.constant.CommandConstant.FORWARD_SMALL_BLOCK_MESSAGE;
import static io.nuls.block.constant.CommandConstant.GET_SMALL_BLOCK_MESSAGE;

/**
 * 处理收到的{@link HashMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Service
public class ForwardSmallBlockHandler extends BaseCmd {

    @CmdAnnotation(cmd = FORWARD_SMALL_BLOCK_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    @MessageHandler(message = HashMessage.class)
    public Response process(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        String nodeId = map.get("nodeId").toString();
        HashMessage message = new HashMessage();
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger messageLog = context.getMessageLog();
        byte[] decode = RPCUtil.decode(map.get("messageBody").toString());
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            e.printStackTrace();
            messageLog.error(e);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }
        NulsDigestData blockHash = message.getRequestHash();
        Long height = context.getCachedHashHeightMap().get(blockHash);
        if (height != null) {
            NetworkUtil.setHashAndHeight(chainId, blockHash, height, nodeId);
        } else {
            context.getCommonLog().debug("can't set node height, nodeId-" + nodeId + "hash-" + blockHash);
        }
        if (context.getStatus().equals(StatusEnum.SYNCHRONIZING)) {
            return success();
        }
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
        messageLog.debug("recieve HashMessage from node-" + nodeId + ", chainId:" + chainId + ", hash:" + blockHash);
        //1.已收到完整区块,丢弃
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return success();
        }
        //2.已收到部分区块,还缺失交易信息,发送HashListMessage到源节点
        if (BlockForwardEnum.INCOMPLETE.equals(status)) {
            CachedSmallBlock block = SmallBlockCacher.getCachedSmallBlock(chainId, blockHash);
            HashListMessage request = new HashListMessage();
            request.setBlockHash(blockHash);
            request.setTxHashList(block.getMissingTransactions());
            TxGroupTask task = new TxGroupTask();
            task.setId(System.nanoTime());
            task.setNodeId(nodeId);
            task.setRequest(request);
            task.setExcuteTime(blockConfig.getTxGroupTaskDelay());
            TxGroupRequestor.addTask(chainId, blockHash.toString(), task);
            return success();
        }
        //3.未收到区块
        if (BlockForwardEnum.EMPTY.equals(status)) {
            HashMessage request = new HashMessage();
            request.setRequestHash(blockHash);
            NetworkUtil.sendToNode(chainId, request, nodeId, GET_SMALL_BLOCK_MESSAGE);
            return success();
        }
        return success();
    }
}
