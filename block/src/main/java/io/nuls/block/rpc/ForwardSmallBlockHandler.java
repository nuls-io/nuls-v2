/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.message.ForwardSmallBlockMessage;
import io.nuls.block.message.GetSmallBlockMessage;
import io.nuls.block.message.body.GetSmallBlockMessageBody;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.NetworkUtil;
import io.nuls.block.utils.SmallBlockDuplicateRemoval;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.List;

import static io.nuls.block.constant.CommandConstant.FORWARD_SMALL_BLOCK_MESSAGE;

/**
 * 处理收到的{@link ForwardSmallBlockMessage}
 * @author captain
 * @date 18-11-14 下午4:23
 * @version 1.0
 */
@Component
public class ForwardSmallBlockHandler extends BaseCmd {

    private static final int MAX_SIZE = 1000;
    @Autowired
    private BlockService service;

    @CmdAnnotation(cmd = FORWARD_SMALL_BLOCK_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public CmdResponse process(List<Object> params){

        Integer chainId = Integer.parseInt(params.get(0).toString());
        String nodeId = params.get(1).toString();
        ForwardSmallBlockMessage message = new ForwardSmallBlockMessage();

        byte[] decode = HexUtil.decode(params.get(2).toString());
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            Log.warn(e.getMessage());
            return failed(BlockErrorCode.PARAMETER_ERROR, "");
        }

        if(message == null || nodeId == null) {
            return failed(BlockErrorCode.PARAMETER_ERROR, "");
        }

        NulsDigestData blockHash = message.getMsgBody().getBlockHash();
        if (SmallBlockDuplicateRemoval.needDownloadSmallBlock(blockHash)) {
            GetSmallBlockMessage getSmallBlockMessage = new GetSmallBlockMessage();
            GetSmallBlockMessageBody body = new GetSmallBlockMessageBody();
            body.setRequestHash(blockHash);
            getSmallBlockMessage.setMsgBody(body);
            boolean b = NetworkUtil.sendToNode(chainId, getSmallBlockMessage, nodeId);
            if (!b) {
                SmallBlockDuplicateRemoval.removeForward(blockHash);
            }
        }
        return success();
    }

}
