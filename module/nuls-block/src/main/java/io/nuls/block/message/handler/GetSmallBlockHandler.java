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
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.SmallBlock;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashMessage;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.rpc.call.NetworkUtil;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.Map;

import static io.nuls.block.constant.CommandConstant.GET_SMALL_BLOCK_MESSAGE;
import static io.nuls.block.constant.CommandConstant.SMALL_BLOCK_MESSAGE;


/**
 * 处理收到的{@link HashMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Service
public class GetSmallBlockHandler extends BaseCmd {

    @CmdAnnotation(cmd = GET_SMALL_BLOCK_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public Response process(Map map) {
        int chainId = Integer.parseInt(map.get(Constants.CHAIN_ID).toString());
        String nodeId = map.get("nodeId").toString();
        HashMessage message = new HashMessage();
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        byte[] decode = RPCUtil.decode(map.get("messageBody").toString());
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            messageLog.error("", e);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        NulsHash blockHash = message.getRequestHash();
        messageLog.debug("recieve HashMessage from node-" + nodeId + ", chainId:" + chainId + ", hash:" + blockHash);
        SmallBlock smallBlock = SmallBlockCacher.getSmallBlock(chainId, blockHash);
        if (smallBlock != null) {
            SmallBlockMessage smallBlockMessage = new SmallBlockMessage();
            smallBlockMessage.setSmallBlock(smallBlock);
            NetworkUtil.sendToNode(chainId, smallBlockMessage, nodeId, SMALL_BLOCK_MESSAGE);
        }
        return success();
    }

}
