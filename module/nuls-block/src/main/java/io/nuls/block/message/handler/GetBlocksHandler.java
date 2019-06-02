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
import io.nuls.block.message.HeightRangeMessage;
import io.nuls.block.rpc.call.NetworkUtil;
import io.nuls.block.service.BlockService;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;

import static io.nuls.block.constant.CommandConstant.BLOCK_MESSAGE;
import static io.nuls.block.constant.CommandConstant.GET_BLOCKS_BY_HEIGHT_MESSAGE;

/**
 * 处理收到的{@link HeightRangeMessage},用于区块的同步
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("GetBlocksHandlerV1")
public class GetBlocksHandler implements MessageProcessor {

    private static final int MAX_SIZE = 1000;
    @Autowired
    private BlockService service;

    private void sendBlock(int chainId, Block block, String nodeId, NulsHash requestHash) {
        BlockMessage blockMessage = new BlockMessage(requestHash, block);
        NetworkUtil.sendToNode(chainId, blockMessage, nodeId, BLOCK_MESSAGE);
    }

    @Override
    public String getCmd() {
        return GET_BLOCKS_BY_HEIGHT_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        HeightRangeMessage message = RPCUtil.getInstanceRpcStr(msgStr, HeightRangeMessage.class);
        if (message == null) {
            return;
        }
        NulsLogger messageLog = ContextManager.getContext(chainId).getLogger();
        long startHeight = message.getStartHeight();
        long endHeight = message.getEndHeight();
        if (startHeight < 0L || startHeight > endHeight || endHeight - startHeight > MAX_SIZE) {
            messageLog.error("PARAMETER_ERROR");
            return;
        }
        messageLog.debug("recieve HeightRangeMessage from node-" + nodeId + ", chainId:" + chainId + ", start:" + startHeight + ", end:" + endHeight);
        NulsHash requestHash;
        try {
            requestHash = NulsHash.calcHash(message.serialize());
            Block block;
            do {
                block = service.getBlock(chainId, startHeight++);
                if (block == null) {
                    NetworkUtil.sendFail(chainId, requestHash, nodeId);
                    return;
                }
                sendBlock(chainId, block, nodeId, requestHash);
            } while (endHeight >= startHeight);
            NetworkUtil.sendSuccess(chainId, requestHash, nodeId);
        } catch (Exception e) {
            messageLog.error("error occur when send block", e);
        }
    }
}
