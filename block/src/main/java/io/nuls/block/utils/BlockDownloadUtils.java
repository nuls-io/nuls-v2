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
import io.nuls.block.message.HashMessage;
import io.nuls.block.model.Node;
import io.nuls.block.utils.module.NetworkUtil;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.nuls.block.constant.CommandConstant.GET_BLOCK_MESSAGE;
import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * 区块下载工具类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午5:31
 */
public class BlockDownloadUtils {

    /**
     * 根据区块hash从节点下载区块
     *
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
        Future<Block> future = CacheHandler.addSingleBlockRequest(chainId, hash);
        boolean result = NetworkUtil.sendToNode(chainId, message, node.getId(), GET_BLOCK_MESSAGE);
        if (!result) {
            CacheHandler.removeBlockByHashFuture(chainId, hash);
            return null;
        }
        try {
            return future.get(10L, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.error("get block-" + hash + " from " + node.getId() + "fail", e);
            return null;
        } finally {
            CacheHandler.removeBlockByHashFuture(chainId, hash);
        }
    }

}
