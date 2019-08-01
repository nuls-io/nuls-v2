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
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.block.cache.BlockCacher;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.BlockMessage;
import io.nuls.block.model.ChainContext;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.Map;

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

    private Map<Integer, Map<Long, Block>> map = new HashMap<>(2);

    @Override
    public String getCmd() {
        return BLOCK_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger messageLog = context.getLogger();
        BlockMessage message = RPCUtil.getInstanceRpcStr(msgStr, BlockMessage.class);
        if (message == null) {
            return;
        }
        Block block = message.getBlock();
        if (block == null) {
            messageLog.debug("recieve null BlockMessage from node-" + nodeId + ", chainId:" + chainId + ", msghash:" + message.getRequestHash());
        } else {
            //接收到的区块用于区块同步
            if (message.isSyn()) {
                synchronized (this) {
                    long synHeight = context.getSynHeight();
                    long height = block.getHeader().getHeight();
                    if (height == (synHeight + 1)) {
                        context.getDeque().addLast(block);
                        context.setSynHeight(synHeight + 1);
                    } else if (height > synHeight) {
                        Map<Long, Block> blockMap = map.computeIfAbsent(chainId, e -> new HashMap<>(100));
                        blockMap.put(height, block);
                        while (true) {
                            Block block1 = blockMap.get(synHeight + 1);
                            if (block1 == null) {
                                break;
                            } else {
                                synHeight++;
                                context.getDeque().addLast(block);
                                context.setSynHeight(synHeight);
                            }
                        }
                    }
                }
            }
            messageLog.debug("recieve BlockMessage from node-" + nodeId + ", chainId:" + chainId + ", hash:" + block.getHeader().getHash() + ", height-" + block.getHeader().getHeight());
        }
        BlockCacher.receiveBlock(chainId, message);
    }
}
