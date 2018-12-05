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
import io.nuls.base.data.SmallBlock;
import io.nuls.tools.cache.LimitHashMap;

/**
 * Used for sharing temporary data between multiple hander.
 * 用于不同的handler之间共享交易数据，交易缓存池中的数据已经放入，直到自动销毁前，不做清理
 *
 * @author Niels
 */
public class TemporaryCacheManager {
    private static final TemporaryCacheManager INSTANCE = new TemporaryCacheManager();

    private LimitHashMap<NulsDigestData, SmallBlock> smallBlockCacheMap = new LimitHashMap<>(100);
    private LimitHashMap<NulsDigestData, NulsDigestData> smallBlockHashCacheMap = new LimitHashMap<>(100);

    private TemporaryCacheManager() {

    }

    public static TemporaryCacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * 将一个SmallBlock放入内存中，若不主动删除，则在缓存存满或者存在时间超过1000秒时，自动清理
     * Store a SmallBlock in memory, cache it full or exist for over 1000 seconds, and clean it automatically.
     *
     * @param smallBlock 要放入内存中的对象
     */
    public void cacheSmallBlock(SmallBlock smallBlock) {
        smallBlockCacheMap.put(smallBlock.getHeader().getHash(), smallBlock);
    }

    public void cacheSmallBlockWithRequest(NulsDigestData requestHash, SmallBlock smallBlock) {
        NulsDigestData blockHash = smallBlock.getHeader().getHash();
        smallBlockHashCacheMap.put(requestHash, blockHash);
        smallBlockCacheMap.put(blockHash, smallBlock);
    }

    /**
     * 根据区块hash获取完整的SmallBlock
     * get SmallBlock by block header digest data
     *
     * @param requestHash getTxGroupRequestHash
     * @return SmallBlock
     */
    public SmallBlock getSmallBlockByRequest(NulsDigestData requestHash) {

        return getSmallBlockByHash(smallBlockHashCacheMap.get(requestHash));
    }

    public SmallBlock getSmallBlockByHash(NulsDigestData blockHash) {

        return smallBlockCacheMap.get(blockHash);
    }

    /**
     * 根据区块摘要对象从缓存中移出一个SmallBlock，移除后再获取时将返回null
     * A SmallBlock is removed from the cache based on the block summary object, and null is returned when it is removed.
     *
     * @param hash transaction digest data
     */
    public void removeSmallBlock(NulsDigestData hash) {
        if (null == smallBlockCacheMap) {
            return;
        }
        smallBlockCacheMap.remove(hash);
    }


    /**
     * 清空所有缓存的数据
     * Empty all cached data.
     */
    public void clear() {
        this.smallBlockCacheMap.clear();
    }

    /**
     * 销毁缓存
     * destroy cache
     */
    public void destroy() {
        this.smallBlockCacheMap.clear();
    }

    public int getSmallBlockCount() {
        return smallBlockCacheMap.size();
    }

}
