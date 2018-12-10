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
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.constant.CommandConstant;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.message.HashMessage;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.utils.NetworkUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.Map;

import static io.nuls.block.constant.CommandConstant.FORWARD_SMALL_BLOCK_MESSAGE;

/**
 * 处理收到的{@link HashMessage}，用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component
public class ForwardSmallBlockHandler extends BaseCmd {

    @CmdAnnotation(cmd = FORWARD_SMALL_BLOCK_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public Object process(Map map) {

        Integer chainId = Integer.parseInt(map.get("chainId").toString());
        String nodeId = map.get("nodes").toString();
        HashMessage message = new HashMessage();

        byte[] decode = HexUtil.decode(map.get("messageBody").toString());
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            Log.error(e);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        if (message == null || nodeId == null) {
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        NulsDigestData blockHash = message.getRequestHash();
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
        //1.已收到完整区块，丢弃
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return success();
        }

        //2.已收到部分区块，还缺失交易信息，发送HashListMessage到源节点
        if (BlockForwardEnum.INCOMPLETE.equals(status)) {
            CachedSmallBlock block = SmallBlockCacher.getSmallBlock(chainId, blockHash);
            HashListMessage request = new HashListMessage();
            request.setBlockHash(blockHash);
            request.setTxHashList(block.getMissingTransactions());
            request.setCommand(CommandConstant.GET_TXGROUP_MESSAGE);
            NetworkUtil.sendToNode(chainId, request, nodeId);
            return success();
        }

        //3.未收到区块
        if (BlockForwardEnum.EMPTY.equals(status)) {
            HashMessage request = new HashMessage();
            request.setRequestHash(blockHash);
            request.setCommand(CommandConstant.GET_SMALL_BLOCK_MESSAGE);
            NetworkUtil.sendToNode(chainId, request, nodeId);
            return success();
        }

        return success();
    }


}
