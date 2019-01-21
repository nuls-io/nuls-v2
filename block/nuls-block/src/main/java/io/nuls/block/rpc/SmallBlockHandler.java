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
import io.nuls.base.data.*;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.message.SmallBlockMessage;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.block.utils.module.TransactionUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.GET_TXGROUP_MESSAGE;
import static io.nuls.block.constant.CommandConstant.SMALL_BLOCK_MESSAGE;


/**
 * 处理收到的{@link SmallBlockMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component
public class SmallBlockHandler extends BaseCmd {

    @Autowired
    private BlockService blockService;

    @CmdAnnotation(cmd = SMALL_BLOCK_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public Response process(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        ChainContext context = ContextManager.getContext(chainId);
//        if (!context.getStatus().equals(RUNNING)) {
//            return success();
//        }
        String nodeId = map.get("nodeId").toString();
        SmallBlockMessage message = new SmallBlockMessage();
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        byte[] decode = HexUtil.decode(map.get("messageBody").toString());
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            e.printStackTrace();
            messageLog.error(e);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        SmallBlock smallBlock = message.getSmallBlock();
        if (null == smallBlock) {
            messageLog.warn("recieved a null smallBlock!");
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        BlockHeader header = smallBlock.getHeader();
        NulsDigestData blockHash = header.getHash();
        //阻止恶意节点提前出块,拒绝接收未来一定时间外的区块
        ChainParameters parameters = context.getParameters();
        int validBlockInterval = parameters.getValidBlockInterval();
        long currentTime = NetworkUtil.currentTime();
        if (header.getTime() > (currentTime + validBlockInterval)) {
            messageLog.error("header.getTime()-" + header.getTime());
            messageLog.error("currentTime-" + currentTime);
            messageLog.error("validBlockInterval-" + validBlockInterval);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        BlockForwardEnum status = SmallBlockCacher.getStatus(chainId, blockHash);
        messageLog.debug("recieve smallBlockMessage from node-" + nodeId + ", chainId:" + chainId + ", height:" + header.getHeight() + ", hash:" + header.getHash());
        NetworkUtil.setHashAndHeight(chainId, blockHash, header.getHeight(), nodeId);
        //1.已收到完整区块,丢弃
        if (BlockForwardEnum.COMPLETE.equals(status)) {
            return success();
        }

        //2.已收到部分区块,还缺失交易信息,发送HashListMessage到源节点
        if (BlockForwardEnum.INCOMPLETE.equals(status)) {
            CachedSmallBlock block = SmallBlockCacher.getSmallBlock(chainId, blockHash);
            HashListMessage request = new HashListMessage();
            request.setBlockHash(blockHash);
            request.setTxHashList(block.getMissingTransactions());
            NetworkUtil.sendToNode(chainId, request, nodeId, GET_TXGROUP_MESSAGE);
            return success();
        }

        //3.未收到区块
        if (BlockForwardEnum.EMPTY.equals(status)) {
            if (!BlockUtil.headerVerify(chainId, header)) {
                messageLog.info("recieve error SmallBlockMessage from " + nodeId);
                return success();
            }
            //共识节点打包的交易包括两种交易,一种是在网络上已经广播的普通交易,一种是共识节点生成的特殊交易(如共识奖励、红黄牌),后面一种交易其他节点的未确认交易池中不可能有,所以都放在SubTxList中
            //还有一种场景时收到smallBlock时,有一些普通交易还没有缓存在未确认交易池中,此时要再从源节点请求
            Map<NulsDigestData, Transaction> txMap = new HashMap<>((int) header.getTxCount());
            List<Transaction> subTxList = smallBlock.getSubTxList();
            for (Transaction tx : subTxList) {
                txMap.put(tx.getHash(), tx);
            }
            List<NulsDigestData> needHashList = new ArrayList<>();
            for (NulsDigestData hash : smallBlock.getTxHashList()) {
                Transaction tx = txMap.get(hash);
                if (null == tx) {
                    tx = TransactionUtil.getTransaction(chainId, hash);
                    if (tx != null) {
                        subTxList.add(tx);
                        txMap.put(hash, tx);
                    }
                }
                if (null == tx) {
                    needHashList.add(hash);
                }
            }
            //获取没有的交易
            if (!needHashList.isEmpty()) {
                messageLog.info("block height : " + header.getHeight() + ", tx count : " + header.getTxCount() + " , get group tx of " + needHashList.size());
                HashListMessage request = new HashListMessage();
                request.setBlockHash(blockHash);
                request.setTxHashList(needHashList);
                NetworkUtil.sendToNode(chainId, request, nodeId, GET_TXGROUP_MESSAGE);
                //这里的smallBlock的subTxList中包含一些非系统交易,用于跟TxGroup组合成完整区块
                CachedSmallBlock cachedSmallBlock = new CachedSmallBlock(needHashList, smallBlock);
                SmallBlockCacher.cacheSmallBlock(chainId, cachedSmallBlock);
                SmallBlockCacher.setStatus(chainId, blockHash, BlockForwardEnum.INCOMPLETE);
                return success();
            }

            Block block = BlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
            if (blockService.saveBlock(chainId, block, 1, true)) {
                SmallBlock newSmallBlock = BlockUtil.getSmallBlock(chainId, block);
                CachedSmallBlock cachedSmallBlock = new CachedSmallBlock(null, newSmallBlock);
                SmallBlockCacher.cacheSmallBlock(chainId, cachedSmallBlock);
                SmallBlockCacher.setStatus(chainId, blockHash, BlockForwardEnum.COMPLETE);
                blockService.forwardBlock(chainId, blockHash, nodeId);
            }
        }
        return success();
    }

}
