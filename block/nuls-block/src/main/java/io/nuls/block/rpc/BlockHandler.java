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

package io.nuls.block.rpc;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.BlockMessage;
import io.nuls.block.utils.module.ProtocolUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.Map;

import static io.nuls.block.constant.CommandConstant.BLOCK_MESSAGE;


/**
 * 处理收到的{@link BlockMessage},用于区块的同步
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component
public class BlockHandler extends BaseCmd {

    @CmdAnnotation(cmd = BLOCK_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public Response process(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        if (!ProtocolUtil.meaasgeValidate(chainId, BlockMessage.class, this.getClass())) {
            messageLog.error("The message or message handler is not available in the current version!");
        }
        String nodeId = map.get("nodeId").toString();
        BlockMessage message = new BlockMessage();
        try {
            byte[] decode = HexUtil.decode(map.get("messageBody").toString());
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            e.printStackTrace();
            messageLog.error(e);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }
        if (message.getBlock() == null) {
            messageLog.debug("recieve null BlockMessage from node-" + nodeId + ", chainId:" + chainId + ", hash:" + message.getRequestHash());
        } else {
            messageLog.debug("recieve BlockMessage from node-" + nodeId + ", chainId:" + chainId + ", hash:" + message.getRequestHash());
        }
        CacheHandler.receiveBlock(chainId, message);
        return success();
    }

}
