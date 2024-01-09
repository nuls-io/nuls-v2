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
import io.nuls.base.data.*;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.TxGroupMessage;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.service.BlockService;
import io.nuls.block.thread.monitor.TxGroupRequestor;
import io.nuls.block.utils.BlockUtil;
import io.nuls.block.utils.SmallBlockCacher;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.DateUtils;

import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.BlockForwardEnum.ERROR;
import static io.nuls.block.constant.CommandConstant.TXGROUP_MESSAGE;

/**
 * Process received{@link TxGroupMessage},Broadcasting and forwarding for blocks
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 afternoon4:23
 */
@Component("TxGroupHandlerV1")
public class TxGroupHandler implements MessageProcessor {

    @Autowired
    private BlockService blockService;

    @Override
    public String getCmd() {
        return TXGROUP_MESSAGE;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        TxGroupMessage message = RPCUtil.getInstanceRpcStr(msgStr, TxGroupMessage.class);
        if (message == null) {
            return;
        }
        NulsLogger logger = ContextManager.getContext(chainId).getLogger();
        List<Transaction> transactions = message.getTransactions();
        if (null == transactions || transactions.isEmpty()) {
            logger.warn("recieved a null txGroup form " + nodeId);
            return;
        }
//        logger.debug("recieve TxGroupMessage from network node-" + nodeId + ", txcount:" + transactions.size());
        NulsHash blockHash = message.getBlockHash();
        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
        //1.Received complete block,discard
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return;
        }
        //2.Received partial blocks,Transaction information is still missing,What I received should be the missing transaction information
        if (BlockForwardEnum.INCOMPLETE.equals(status)) {
            CachedSmallBlock cachedSmallBlock = SmallBlockCacher.getCachedSmallBlock(chainId, blockHash);
            if (cachedSmallBlock == null) {
                return;
            }
            SmallBlock smallBlock = cachedSmallBlock.getSmallBlock();
            BlockHeader header = smallBlock.getHeader();
            Map<NulsHash, Transaction> txMap = cachedSmallBlock.getTxMap();
            for (Transaction tx : transactions) {
                txMap.put(tx.getHash(), tx);
            }

            Block block = BlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
            block.setNodeId(nodeId);
            TxGroupRequestor.removeTask(chainId, blockHash);
//            logger.debug("record recv block, block create time-" + DateUtils.timeStamp2DateStr(block.getHeader().getTime() * 1000) + ", hash-" + block.getHeader().getHash());
            boolean b = blockService.saveBlock(chainId, block, 1, true, false, true);
            if (!b) {
                SmallBlockCacher.setStatus(chainId, blockHash, ERROR);
            }
            return;
        }
        //3.Block not received
        if (BlockForwardEnum.EMPTY.equals(status)) {
            logger.error("It is theoretically impossible to enter this branch");
        }
    }
}
