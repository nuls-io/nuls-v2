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
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.TxGroupMessage;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.model.ChainContext;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.TXGROUP_MESSAGE;
import static io.nuls.block.constant.RunningStatusEnum.RUNNING;
import static io.nuls.block.utils.LoggerUtil.messageLog;

/**
 * 处理收到的{@link TxGroupMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component
public class TxGroupHandler extends BaseCmd {

    @Autowired
    private BlockService blockService;

    @CmdAnnotation(cmd = TXGROUP_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public Response process(Map map) {
        Integer chainId = Integer.parseInt(map.get("chainId").toString());
        ChainContext context = ContextManager.getContext(chainId);
        if (!context.getStatus().equals(RUNNING)) {
            return success();
        }
        String nodeId = map.get("nodeId").toString();
        TxGroupMessage message = new TxGroupMessage();

        byte[] decode = HexUtil.decode(map.get("messageBody").toString());
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            messageLog.error(e);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        if (nodeId == null) {
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        List<Transaction> transactions = message.getTransactions();
        if (null == transactions || transactions.size() == 0) {
            messageLog.warn("recieved a null txGroup form " + nodeId);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }
        messageLog.info("recieve TxGroupMessage from network node-" + nodeId + ", chainId:" + chainId + ", txcount:" + transactions.size());
        NulsDigestData blockHash = message.getBlockHash();
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
        //1.已收到完整区块,丢弃
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return success();
        }

        //2.已收到部分区块,还缺失交易信息,收到的应该就是缺失的交易信息
        if (BlockForwardEnum.INCOMPLETE.equals(status)) {
            SmallBlock smallBlock = SmallBlockCacher.getSmallBlock(chainId, blockHash).getSmallBlock();
            if (null == smallBlock) {
                return failed(BlockErrorCode.PARAMETER_ERROR);
            }

            BlockHeader header = smallBlock.getHeader();
            NulsDigestData headerHash = header.getHash();
            Map<NulsDigestData, Transaction> txMap = new HashMap<>((int) header.getTxCount());
            for (Transaction tx : smallBlock.getSubTxList()) {
                txMap.put(tx.getHash(), tx);
            }
            for (Transaction tx : transactions) {
                txMap.put(tx.getHash(), tx);
            }

            Block block = BlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
            if (blockService.saveBlock(chainId, block, 1, true)) {
                SmallBlock newSmallBlock = BlockUtil.getSmallBlock(chainId, block);
                CachedSmallBlock cachedSmallBlock = new CachedSmallBlock(null, newSmallBlock);
                SmallBlockCacher.cacheSmallBlock(chainId, cachedSmallBlock);
                SmallBlockCacher.setStatus(chainId, blockHash, BlockForwardEnum.COMPLETE);
                blockService.forwardBlock(chainId, headerHash, nodeId);
            }
            return success();
        }

        //3.未收到区块
        if (BlockForwardEnum.EMPTY.equals(status)) {
            messageLog.error("It is theoretically impossible to enter this branch");
        }
        return success();
    }

}
