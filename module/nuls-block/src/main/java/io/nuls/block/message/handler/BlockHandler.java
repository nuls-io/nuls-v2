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

import io.nuls.base.data.Block;
import io.nuls.block.cache.BlockCacher;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.BlockMessage;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;

import static io.nuls.block.constant.CommandConstant.BLOCK_MESSAGE;


/**
 * 处理收到的{@link BlockMessage},用于区块的同步
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("BlockHandlerV1")
public class BlockHandler implements MessageProcessor {

    @Override
    public String getCmd() {
        return BLOCK_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        BlockMessage message = RPCUtil.getInstanceRpcStr(msgStr, BlockMessage.class);
        if (message == null) {
            return;
        }
        Block block = message.getBlock();
        if (block == null) {
            messageLog.debug("recieve null BlockMessage from node-" + nodeId + ", chainId:" + chainId + ", msghash:" + message.getRequestHash());
            return;
        } else {
            messageLog.debug("recieve BlockMessage from node-" + nodeId + ", chainId:" + chainId + ", hash:" + block.getHeader().getHash() + ", height-" + block.getHeader().getHeight());
        }
        BlockCacher.receiveBlock(chainId, message);
    }
}
