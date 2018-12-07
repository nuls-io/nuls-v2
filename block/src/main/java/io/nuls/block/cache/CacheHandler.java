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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * 异步请求响应结果缓存类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午5:35
 */
public class CacheHandler {

    private static Map<Integer, DataCacher<Block>> blockByHashCacher = new ConcurrentHashMap<>();
    private static Map<Integer, DataCacher<Block>> blockByHeightCacher = new ConcurrentHashMap<>();
    private static Map<Integer, DataCacher<CompleteMessage>> synTaskCacher = new ConcurrentHashMap<>();

    public static CompletableFuture<Block> addGetBlockByHeightRequest(int chainId, NulsDigestData requestHash) {
        DataCacher<Block> dataCacher = blockByHeightCacher.get(chainId);
        if (dataCacher != null) {
            return dataCacher.addFuture(requestHash);
        } else {
            dataCacher = new DataCacher<>();
            blockByHeightCacher.put(chainId, dataCacher);
            return dataCacher.addFuture(requestHash);
        }
    }

    public static CompletableFuture<Block> addGetBlockByHashRequest(int chainId, NulsDigestData requestHash) {
        DataCacher<Block> dataCacher = blockByHashCacher.get(chainId);
        if (dataCacher != null) {
            return dataCacher.addFuture(requestHash);
        } else {
            dataCacher = new DataCacher<>();
            blockByHashCacher.put(chainId, dataCacher);
            return dataCacher.addFuture(requestHash);
        }
    }

    public static void receiveBlock(int chainId, Block block) {
        NulsDigestData hash = NulsDigestData.calcDigestData(SerializeUtils.uint64ToByteArray(block.getHeader().getHeight()));
        boolean result = blockByHeightCacher.get(chainId).success(hash, block);
        if (!result) {
            blockByHashCacher.get(chainId).success(block.getHeader().getHash(), block);
        }
    }

    public static Future<CompleteMessage> newRequest(int chainId, NulsDigestData hash) {
        DataCacher<CompleteMessage> dataCacher = synTaskCacher.get(chainId);
        if (dataCacher != null) {
            return dataCacher.addFuture(hash);
        } else {
            dataCacher = new DataCacher<>();
            synTaskCacher.put(chainId, dataCacher);
            return dataCacher.addFuture(hash);
        }
    }

    public static void requestComplete(int chainId, CompleteMessage message) {
        if (message.isSuccess()) {
            synTaskCacher.get(chainId).success(message.getRequestHash(), message);
        } else {
            synTaskCacher.get(chainId).fail(message.getRequestHash());
        }
    }

    public static void removeBlockByHeightFuture(int chainId, NulsDigestData hash) {
        blockByHeightCacher.get(chainId).removeFuture(hash);
    }

    public static void removeBlockByHashFuture(int chainId, NulsDigestData hash) {
        blockByHashCacher.get(chainId).removeFuture(hash);
    }

    public static void removeRequest(int chainId, NulsDigestData hash) {
        synTaskCacher.get(chainId).removeFuture(hash);
    }

    public static void init(int chainId){

    }

}
