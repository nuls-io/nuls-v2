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
import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.BlockMessage;
import io.nuls.block.message.HeightRangeMessage;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.Map;

import static io.nuls.block.constant.CommandConstant.BLOCK_MESSAGE;
import static io.nuls.block.constant.CommandConstant.GET_BLOCKS_BY_HEIGHT_MESSAGE;


/**
 * 处理收到的{@link HeightRangeMessage},用于区块的同步
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component
public class GetBlocksHandler extends BaseCmd {

    private static final int MAX_SIZE = 1000;
    @Autowired
    private BlockService service;

    @CmdAnnotation(cmd = GET_BLOCKS_BY_HEIGHT_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public Response process(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        String nodeId = map.get("nodeId").toString();
        HeightRangeMessage message = new HeightRangeMessage();

        byte[] decode = HexUtil.decode(map.get("messageBody").toString());
        message.parse(new NulsByteBuffer(decode));
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        long startHeight = message.getStartHeight();
        long endHeight = message.getEndHeight();
        if (startHeight < 0L || startHeight > endHeight || endHeight - startHeight > MAX_SIZE) {
            messageLog.error("PARAMETER_ERROR");
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }
        messageLog.info("recieve HeightRangeMessage from node-" + nodeId + ", chainId:" + chainId + ", start:" + startHeight + ", end:" + endHeight);
        NulsDigestData requestHash;
        try {
            requestHash = NulsDigestData.calcDigestData(message.serialize());
            Block block;
            do {
                block = service.getBlock(chainId, startHeight++);
                if (block == null) {
                    NetworkUtil.sendFail(chainId, requestHash, nodeId);
                    return failed(BlockErrorCode.PARAMETER_ERROR);
                }
                sendBlock(chainId, block, nodeId, requestHash);
            } while (endHeight >= startHeight);
            NetworkUtil.sendSuccess(chainId, requestHash, nodeId);
        } catch (Exception e) {
            e.printStackTrace();
            messageLog.error("error occur when send block");
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }
        return success();
    }

    private void sendBlock(int chainId, Block block, String nodeId, NulsDigestData requestHash) {
        BlockMessage blockMessage = new BlockMessage(requestHash, block);
        NetworkUtil.sendToNode(chainId, blockMessage, nodeId, BLOCK_MESSAGE);
    }

}
