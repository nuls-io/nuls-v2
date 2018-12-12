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

package io.nuls.block.utils;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.constant.CommandConstant;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.message.GetBlocksByHeightMessage;
import io.nuls.block.message.HashMessage;
import io.nuls.block.model.Node;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 区块下载工具类
 * @author captain
 * @date 18-11-14 下午5:31
 * @version 1.0
 */
public class BlockDownloadUtils {

    /**
     * 根据区块hash从节点下载区块
     * @param chainId
     * @param hash
     * @param node
     * @return
     */
    public static Block getBlockByHash(int chainId, NulsDigestData hash, Node node) {
        if (hash == null || node == null) {
            return null;
        }
        HashMessage message = new HashMessage();
        message.setRequestHash(hash);
        message.setCommand(CommandConstant.GET_BLOCK_MESSAGE);
        Future<Block> future = CacheHandler.addGetBlockByHashRequest(chainId, hash);
        boolean result = NetworkUtil.sendToNode(chainId, message, node.getId());
        if (!result) {
            CacheHandler.removeBlockByHashFuture(chainId, hash);
            return null;
        }
        try {
            return future.get(30L, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.error(node.getId(), e);
            return null;
        } finally {
            CacheHandler.removeBlockByHashFuture(chainId, hash);
        }
    }

    /**
     * 从节点下载某高度区间内的区块
     * @param chainId
     * @param node
     * @param startHeight
     * @param endHeight
     * @return
     * @throws Exception
     */
    public static List<Block> getBlocks(int chainId, Node node, long startHeight, long endHeight) throws Exception {
        Log.info("getBlocks:{}->{} ,from:{}", startHeight, endHeight, node.getId());
        //下载到的区块默认以区块高度排序
        List<Block> blockList = new ArrayList<>();
        if (startHeight < 0L || startHeight > endHeight) {
            return blockList;
        }
        //组装批量获取区块消息
        GetBlocksByHeightMessage message = new GetBlocksByHeightMessage(startHeight, endHeight);
        //计算本次请求hash，用来跟踪本次异步请求，缓存taskFuture，用于跟踪异步请求完成
        NulsDigestData messageHash = message.getHash();
        Future<CompleteMessage> requestFuture = CacheHandler.newRequest(chainId, messageHash);

        //blockFutures用来保存收到的blocks
        List<Map<NulsDigestData, Future<Block>>> blockFutures = new ArrayList<>();
        for (long i = startHeight; i <= endHeight; i++) {
            NulsDigestData hash = NulsDigestData.calcDigestData(SerializeUtils.uint64ToByteArray(i));
            Future<Block> blockFuture = CacheHandler.addGetBlockByHeightRequest(chainId, hash);
            Map<NulsDigestData, Future<Block>> blockFutureMap = new HashMap<>(1);
            blockFutureMap.put(hash, blockFuture);
            blockFutures.add(blockFutureMap);
        }

        //发送消息给目标节点
        boolean result = NetworkUtil.sendToNode(chainId, message, node.getId());

        //发送失败清空数据
        if (!result) {
            CacheHandler.removeRequest(chainId, messageHash);
            for (Map<NulsDigestData, Future<Block>> blockFutureMap : blockFutures) {
                for (Map.Entry<NulsDigestData, Future<Block>> entry : blockFutureMap.entrySet()) {
                    CacheHandler.removeBlockByHeightFuture(chainId, entry.getKey());
                }
            }
            return blockList;
        }

        try {
            CompleteMessage complete = requestFuture.get(60L, TimeUnit.SECONDS);
            if (complete.isSuccess()) {
                for (Map<NulsDigestData, Future<Block>> blockFutureMap : blockFutures) {
                    for (Map.Entry<NulsDigestData, Future<Block>> entry : blockFutureMap.entrySet()) {
                        Block block = entry.getValue().get(30L, TimeUnit.SECONDS);
                        if (block != null) {
                            blockList.add(block);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.error(e);
        } finally {
            CacheHandler.removeRequest(chainId, messageHash);
            for (Map<NulsDigestData, Future<Block>> blockFutureMap : blockFutures) {
                for (Map.Entry<NulsDigestData, Future<Block>> entry : blockFutureMap.entrySet()) {
                    CacheHandler.removeBlockByHeightFuture(chainId, entry.getKey());
                }
            }
        }
        return blockList;
    }

}
