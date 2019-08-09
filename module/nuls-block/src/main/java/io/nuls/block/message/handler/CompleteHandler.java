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
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.block.constant.NodeEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.model.ChainContext;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;

import static io.nuls.block.constant.CommandConstant.COMPLETE_MESSAGE;

/**
 * 处理收到的{@link CompleteMessage},用于区块的同步
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component("CompleteHandlerV1")
public class CompleteHandler implements MessageProcessor {

    @Override
    public String getCmd() {
        return COMPLETE_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        CompleteMessage message = RPCUtil.getInstanceRpcStr(msgStr, CompleteMessage.class);
        if (message == null) {
            return;
        }
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        logger.debug("recieve " + message + " from node-" + nodeId);
        context.getDownloaderParams().getNodeMap().get(nodeId).setNodeEnum(NodeEnum.IDLE);
    }
}
