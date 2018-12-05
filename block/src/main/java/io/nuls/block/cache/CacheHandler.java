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
import io.nuls.block.message.CompleteMessage;
import io.nuls.tools.parse.SerializeUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 异步请求响应结果缓存类
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午5:35
 */
public class CacheHandler {

    private static DataCacher<Block> blockByHashCacher = new DataCacher<>();
    private static DataCacher<Block> blockByHeightCacher = new DataCacher<>();
    private static DataCacher<CompleteMessage> taskCacher = new DataCacher<>();

    public static CompletableFuture<Block> addGetBlockByHeightRequest(NulsDigestData requestHash) {
        return blockByHeightCacher.addFuture(requestHash);
    }

    public static CompletableFuture<Block> addGetBlockByHashRequest(NulsDigestData requestHash) {
        return blockByHashCacher.addFuture(requestHash);
    }

    public static void receiveBlock(Block block) {
        NulsDigestData hash = NulsDigestData.calcDigestData(SerializeUtils.uint64ToByteArray(block.getHeader().getHeight()));
        boolean result = blockByHeightCacher.success(hash, block);
        if (!result) {
            blockByHashCacher.success(block.getHeader().getHash(), block);
        }
    }

    public static Future<CompleteMessage> newRequest(NulsDigestData hash) {
        return taskCacher.addFuture(hash);
    }

    public static void requestComplete(CompleteMessage message) {
        if (message.isSuccess()) {
            taskCacher.success(message.getRequestHash(), message);
        } else {
            taskCacher.fail(message.getRequestHash());
        }
    }

    public static void removeBlockByHeightFuture(NulsDigestData hash) {
        blockByHeightCacher.removeFuture(hash);
    }

    public static void removeBlockByHashFuture(NulsDigestData hash) {
        blockByHashCacher.removeFuture(hash);
    }

    public static void removeRequest(NulsDigestData hash) {
        taskCacher.removeFuture(hash);
    }

}
