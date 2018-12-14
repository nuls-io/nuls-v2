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

package io.nuls.block.cache;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.message.BlockMessage;
import io.nuls.block.message.CompleteMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 主要缓存区块同步过程中收到的区块，还有孤儿链维护线程请求的单个区块
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午5:35
 */
public class CacheHandler {

    /**
     * 批量下载区块请求-hash缓存
     */
    private static Map<Integer, DataCacher<CompleteMessage>> batchBlockHashCacher = new ConcurrentHashMap<>();
    /**
     * 批量下载区块请求-区块缓存
     */
    private static Map<Integer, BlockingQueue<Block>> batchBlockCacher = new ConcurrentHashMap<>();

    /**
     * 单个下载区块请求-hash缓存
     */
    private static Map<Integer, List<NulsDigestData>> singleBlockHashCacher = new ConcurrentHashMap<>();
    /**
     * 单个下载区块请求-区块缓存
     */
    private static Map<Integer, DataCacher<Block>> singleBlockCacher = new ConcurrentHashMap<>();

    /**
     * 初始化
     *
     * @param chainId
     */
    public static void init(int chainId){
        int blockCache = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.BLOCK_CACHE));
        singleBlockCacher.put(chainId, new DataCacher<>());
        singleBlockHashCacher.put(chainId, new LinkedList<>());
        batchBlockCacher.put(chainId, new ArrayBlockingQueue<>(blockCache));
        batchBlockHashCacher.put(chainId, new DataCacher<>());
    }

    public static CompletableFuture<Block> addSingleBlockRequest(int chainId, NulsDigestData requestHash) {
        return singleBlockCacher.get(chainId).addFuture(requestHash);
    }

    public static Future<CompleteMessage> addBatchBlockRequest(int chainId, NulsDigestData hash) {
        return batchBlockHashCacher.get(chainId).addFuture(hash);
    }

    /**
     * 根据requestHash判断该区块是同步区块过程中收到的区块，还是孤儿链维护过程收到的区块，还是恶意区块
     *
     * @param chainId
     * @param message
     */
    public static void receiveBlock(int chainId, BlockMessage message) {
        DataCacher<CompleteMessage> cacher = batchBlockHashCacher.get(chainId);
        NulsDigestData requestHash = message.getRequestHash();
        Block block = message.getBlock();
        if (cacher.contains(requestHash)) {
            batchBlockCacher.get(chainId).offer(block);
            return;
        }
        if (singleBlockHashCacher.get(chainId).contains(requestHash)) {
            singleBlockCacher.get(chainId).success(block.getHeader().getHash(), block);
        }
    }

    public static void batchComplete(int chainId, CompleteMessage message) {
        if (message.isSuccess()) {
            batchBlockHashCacher.get(chainId).success(message.getRequestHash(), message);
        } else {
            batchBlockHashCacher.get(chainId).fail(message.getRequestHash());
        }
    }

    public static void removeBlockByHashFuture(int chainId, NulsDigestData hash) {
        singleBlockCacher.get(chainId).removeFuture(hash);
    }

    public static void removeRequest(int chainId, NulsDigestData hash) {
        batchBlockHashCacher.get(chainId).removeFuture(hash);
    }

    public static BlockingQueue<Block> getBlockQueue(int chainId) {
        return batchBlockCacher.get(chainId);
    }
}
