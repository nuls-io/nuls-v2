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

import io.nuls.base.data.NulsDigestData;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * 异步请求响应结果缓存类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-12 下午2:35
 */
@NoArgsConstructor
public class DataCacher<T> {

    private Map<NulsDigestData, CompletableFuture<T>> cacher = new HashMap<>();

    CompletableFuture<T> addFuture(NulsDigestData hash) {
        var future = new CompletableFuture<T>();
        cacher.put(hash, future);
        return future;
    }

    public boolean complete(NulsDigestData hash, T t) {
        CompletableFuture<T> future = cacher.get(hash);
        if (future == null) {
            Log.debug("DataCacher Time out:" + hash.getDigestHex());
            return false;
        }
        future.complete(t);
        cacher.remove(hash);
        return true;
    }

    void removeFuture(NulsDigestData hash) {
        cacher.remove(hash);
    }
}
