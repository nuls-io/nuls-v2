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
import io.nuls.base.data.*;
import io.nuls.block.cache.TemporaryCacheManager;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.message.HashMessage;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.block.utils.NetworkUtil;
import io.nuls.block.utils.SmallBlockDuplicateRemoval;
import io.nuls.block.utils.TransactionUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.SMALL_BLOCK_MESSAGE;

/**
 * 处理收到的{@link HashMessage}
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component
public class SmallBlockHandler extends BaseCmd {

    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();
    @Autowired
    private BlockService blockService;

    @CmdAnnotation(cmd = SMALL_BLOCK_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public Response process(List<Object> params) {

        Integer chainId = Integer.parseInt(params.get(0).toString());
        String nodeId = params.get(1).toString();
        SmallBlockMessage message = new SmallBlockMessage();

        byte[] decode = HexUtil.decode(params.get(2).toString());
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            Log.warn(e.getMessage());
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        if (message == null || nodeId == null) {
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        SmallBlock smallBlock = message.getSmallBlock();

        if (null == smallBlock) {
            Log.warn("recieved a null smallBlock!");
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        BlockHeader header = smallBlock.getHeader();
        //阻止恶意节点提前出块
        int validBlockInterval = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.VALID_BLOCK_INTERVAL));
        if (header.getTime() > (TimeService.currentTimeMillis() + validBlockInterval)) {
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        if (!SmallBlockDuplicateRemoval.needProcess(header.getHash())) {
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        BlockHeader blockHeader = blockService.getBlockHeader(chainId, header.getHash());
        if (null != blockHeader) {
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        Log.debug("recieve new block from(" + nodeId + "), tx count : " + header.getTxCount() + " , header height:" + header.getHeight() + ", preHash:" + header.getPreHash() + " , hash:" + header.getHash());

        Map<NulsDigestData, Transaction> txMap = new HashMap<>((int)blockHeader.getTxCount());
        for (Transaction tx : smallBlock.getSubTxList()) {
            txMap.put(tx.getHash(), tx);
        }
        List<NulsDigestData> needHashList = new ArrayList<>();
        for (NulsDigestData hash : smallBlock.getTxHashList()) {
            Transaction tx = txMap.get(hash);
            if (null == tx) {
                tx = TransactionUtil.getTransaction(chainId, hash);
                if (tx != null) {
                    smallBlock.getSubTxList().add(tx);
                    txMap.put(hash, tx);
                }
            }
            if (null == tx) {
                needHashList.add(hash);
            }
        }
        if (!needHashList.isEmpty()) {
            Log.info("block height : " + header.getHeight() + ", tx count : " + header.getTxCount() + " , get group tx of " + needHashList.size());
            HashListMessage request = new HashListMessage();
            request.setTxHashList(needHashList);
            NetworkUtil.sendToNode(chainId, request, nodeId);
            NulsDigestData requestHash = null;
            try {
                requestHash = NulsDigestData.calcDigestData(request.serialize());
            } catch (IOException e) {
                Log.error(e);
            }
            temporaryCacheManager.cacheSmallBlockWithRequest(requestHash, smallBlock);
            return success();
        }

        Block block = BlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
        blockService.saveBlock(chainId, block);

        return success();
    }

}
