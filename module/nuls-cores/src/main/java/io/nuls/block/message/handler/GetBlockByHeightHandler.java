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
import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.BlockMessage;
import io.nuls.block.message.HashMessage;
import io.nuls.block.message.HeightMessage;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.service.BlockService;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.ByteUtils;

import static io.nuls.block.constant.CommandConstant.BLOCK_MESSAGE;
import static io.nuls.block.constant.CommandConstant.GET_BLOCK_BY_HEIGHT_MESSAGE;

/**
 * Process received{@link HashMessage},Used for maintaining orphan chains
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 afternoon4:23
 */
@Component("GetBlockByHeightHandlerV1")
public class GetBlockByHeightHandler implements MessageProcessor {

    @Autowired
    private BlockService service;

    private void sendBlock(int chainId, Block block, String nodeId, NulsHash requestHash) {
        BlockMessage message = new BlockMessage();
        message.setRequestHash(requestHash);
        if (block != null) {
            message.setBlock(block);
        }
        message.setSyn(false);
        NetworkCall.sendToNode(chainId, message, nodeId, BLOCK_MESSAGE);
    }

    @Override
    public String getCmd() {
        return GET_BLOCK_BY_HEIGHT_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        HeightMessage message = RPCUtil.getInstanceRpcStr(msgStr, HeightMessage.class);
        if (message == null) {
            return;
        }
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        long height = message.getHeight();
        logger.debug("recieve " + message + " from node-" + nodeId + ", height:" + height);
        sendBlock(chainId, service.getBlock(chainId, height), nodeId, NulsHash.calcHash(ByteUtils.longToBytes(height)));
    }
}
