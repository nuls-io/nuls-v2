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
import io.nuls.tools.parse.SerializeUtils;

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

    private static Map<Integer, DataCacher<CompleteMessage>> synTaskCacher = new ConcurrentHashMap<>();
    private static Map<Integer, BlockingQueue<Block>> blockingQueueMap = new ConcurrentHashMap<>();

    private static Map<Integer, DataCacher<Block>> blockByHashCacher = new ConcurrentHashMap<>();

    public static CompletableFuture<Block> addGetBlockByHashRequest(int chainId, NulsDigestData requestHash) {
        return blockByHashCacher.get(chainId).addFuture(requestHash);
    }

    public static void receiveBlock(int chainId, BlockMessage message) {
        DataCacher<CompleteMessage> cacher = synTaskCacher.get(chainId);
        NulsDigestData requestHash = message.getRequestHash();
        Block block = message.getBlock();
        if (cacher.contains(requestHash)) {
            blockingQueueMap.get(chainId).offer(block);
            return;
        }
        blockByHashCacher.get(chainId).success(block.getHeader().getHash(), block);

    }

    public static Future<CompleteMessage> newRequest(int chainId, NulsDigestData hash) {
        return synTaskCacher.get(chainId).addFuture(hash);
    }

    public static void requestComplete(int chainId, CompleteMessage message) {
        if (message.isSuccess()) {
            synTaskCacher.get(chainId).success(message.getRequestHash(), message);
        } else {
            synTaskCacher.get(chainId).fail(message.getRequestHash());
        }
    }

    public static void removeBlockByHashFuture(int chainId, NulsDigestData hash) {
        blockByHashCacher.get(chainId).removeFuture(hash);
    }

    public static void removeRequest(int chainId, NulsDigestData hash) {
        synTaskCacher.get(chainId).removeFuture(hash);
    }

    public static void init(int chainId){
        int blockCache = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.BLOCK_CACHE));
        blockingQueueMap.put(chainId, new ArrayBlockingQueue<>(blockCache));
        blockByHashCacher.put(chainId, new DataCacher<>());
        synTaskCacher.put(chainId, new DataCacher<>());
    }

    public static BlockingQueue<Block> getBlockQueue(int chainId) {
        return blockingQueueMap.get(chainId);
    }
}
