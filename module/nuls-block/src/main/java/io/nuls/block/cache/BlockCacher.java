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

import io.nuls.base.cache.DataCacher;
import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.block.message.BlockMessage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主要缓存孤儿链维护线程请求的单个区块
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午5:35
 */
public class BlockCacher {

    /**
     * 单个下载区块请求-区块缓存
     */
    private static Map<Integer, DataCacher<Block>> blockCacher = new ConcurrentHashMap<>();

    /**
     * 初始化
     *
     * @param chainId 链Id/chain id
     */
    public static void init(int chainId) {
        blockCacher.put(chainId, new DataCacher<>());
    }

    /**
     * 下载单个区块任务开始时,添加缓存
     *
     * @param chainId 链Id/chain id
     * @param requestHash
     * @return
     */
    public static CompletableFuture<Block> addSingleBlockRequest(int chainId, NulsHash requestHash) {
        return blockCacher.get(chainId).addFuture(requestHash);
    }

    /**
     * 根据requestHash判断该区块是同步区块过程中收到的区块,还是孤儿链维护过程收到的区块,还是恶意区块,分别放入不同的缓存
     *
     * @param chainId 链Id/chain id
     * @param message
     */
    public static void receiveBlock(int chainId, BlockMessage message) {
        NulsHash requestHash = message.getRequestHash();
        Block block = message.getBlock();
        blockCacher.get(chainId).complete(requestHash, block);
    }

    /**
     * 移除缓存
     *
     * @param chainId 链Id/chain id
     * @param hash
     */
    public static void removeBlockByHashFuture(int chainId, NulsHash hash) {
        blockCacher.get(chainId).removeFuture(hash);
    }

}
