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

package io.nuls.block.cache;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.message.BlockMessage;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.thread.BlockWorker;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * 主要缓存区块同步过程中收到的区块,还有孤儿链维护线程请求的单个区块
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午5:35
 */
public class CacheHandler {

    /**
     * 批量下载区块请求-缓存下载完成消息
     */
    private static Map<Integer, DataCacher<CompleteMessage>> completeCacher = new ConcurrentHashMap<>();

    /**
     * 批量下载区块请求-排序前的区块缓存队列,由BlockDownloader放入队列,供BlockCollector取出排序后放入共享队列
     */
    private static Map<Integer, Map<NulsDigestData, List<Block>>> workerBlockCacher = new ConcurrentHashMap<>();

    /**
     * 单个下载区块请求-区块缓存
     */
    private static Map<Integer, DataCacher<Block>> singleBlockCacher = new ConcurrentHashMap<>();

    /**
     * 初始化
     *
     * @param chainId 链Id/chain id
     */
    public static void init(int chainId) {
        workerBlockCacher.put(chainId, new ConcurrentHashMap<>(2));
        singleBlockCacher.put(chainId, new DataCacher<>());
        completeCacher.put(chainId, new DataCacher<>());
    }

    /**
     * 下载单个区块任务开始时,添加缓存
     *
     * @param chainId 链Id/chain id
     * @param requestHash
     * @return
     */
    public static CompletableFuture<Block> addSingleBlockRequest(int chainId, NulsDigestData requestHash) {
        return singleBlockCacher.get(chainId).addFuture(requestHash);
    }

    /**
     * 一个{@link BlockWorker}开始工作时,进行缓存初始化
     *
     * @param chainId 链Id/chain id
     * @param hash
     * @return
     */
    public static Future<CompleteMessage> addBatchBlockRequest(int chainId, NulsDigestData hash) {
        workerBlockCacher.get(chainId).put(hash, new ArrayList<>());
        return completeCacher.get(chainId).addFuture(hash);
    }

    /**
     * 根据requestHash判断该区块是同步区块过程中收到的区块,还是孤儿链维护过程收到的区块,还是恶意区块,分别放入不同的缓存
     *
     * @param chainId 链Id/chain id
     * @param message
     */
    public static void receiveBlock(int chainId, BlockMessage message) {
        NulsDigestData requestHash = message.getRequestHash();
        List<Block> blockList = workerBlockCacher.get(chainId).get(requestHash);
        Block block = message.getBlock();
        if (blockList != null && block != null && !blockList.contains(block)) {
            blockList.add(block);
            return;
        }
        singleBlockCacher.get(chainId).complete(requestHash, block);
    }

    /**
     * 获取{@link BlockWorker}下载到的区块
     *
     * @param chainId 链Id/chain id
     * @param requestHash
     * @return
     */
    public static List<Block> getBlockList(int chainId, NulsDigestData requestHash) {
        return workerBlockCacher.get(chainId).get(requestHash);
    }

    /**
     * 标记一个{@link BlockWorker}的下载任务结束
     *
     * @param chainId 链Id/chain id
     * @param message
     */
    public static void batchComplete(int chainId, CompleteMessage message) {
        completeCacher.get(chainId).complete(message.getRequestHash(), message);
    }

    /**
     * 移除缓存
     *
     * @param chainId 链Id/chain id
     * @param hash
     */
    public static void removeBlockByHashFuture(int chainId, NulsDigestData hash) {
        singleBlockCacher.get(chainId).removeFuture(hash);
    }

    /**
     * 移除缓存
     *
     * @param chainId 链Id/chain id
     * @param hash
     */
    public static void removeRequest(int chainId, NulsDigestData hash) {
        completeCacher.get(chainId).removeFuture(hash);
    }
}
