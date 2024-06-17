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

package io.nuls.block.utils;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.SmallBlock;
import io.nuls.base.data.Transaction;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.service.BlockService;
import io.nuls.common.ConfigBean;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Broadcast of cached blocks during normal system operation、relay the message
 * 1.Cache receivedSmallBlock
 * 2.Cache status markers
 *
 * @author captain
 * @version 1.0
 * @date 18-12-6 morning10:49
 */
@Component
public class SmallBlockCacher {

    @Autowired
    private static BlockService service;

    /**
     * Cache block forwarding、Received during broadcast{@link SmallBlock},Can be used to exclude duplicate messages,
     */
    private static Map<Integer, Map<NulsHash, CachedSmallBlock>> smallBlockCacheMap = new ConcurrentHashMap<>();

    /**
     * Record the propagation status of each block
     */
    private static Map<Integer, Map<NulsHash, BlockForwardEnum>> statusCacheMap = new ConcurrentHashMap<>();

    /**
     * Add aSmallBlockPut into memory,If not deleted proactively,If the cache is full or exists for more than1000Second hour,Automatic cleaning
     *
     * @param chainId          chainId/chain id
     * @param cachedSmallBlock
     */
    public static void cacheSmallBlock(int chainId, CachedSmallBlock cachedSmallBlock) {
        SmallBlock smallBlock = cachedSmallBlock.getSmallBlock();
        smallBlockCacheMap.get(chainId).put(smallBlock.getHeader().getHash(), cachedSmallBlock);
    }

    /**
     * according tohashGet cached{@link SmallBlock}
     * TODO  Annotation Integrity
     *
     * @param chainId   chainId/chain id
     * @param blockHash
     * @return
     */
    public static CachedSmallBlock getCachedSmallBlock(int chainId, NulsHash blockHash) {
        CachedSmallBlock cachedSmallBlock = smallBlockCacheMap.get(chainId).get(blockHash);
        if (cachedSmallBlock == null) {
            Block block = service.getBlock(chainId, blockHash);
            if (block == null) {
                return null;
            }
            SmallBlock smallBlock = BlockUtil.getSmallBlock(chainId, block);
            List<Transaction> txs = block.getTxs();
            Map<NulsHash, Transaction> txMap = new HashMap<>(txs.size());
            txs.forEach(e -> txMap.put(e.getHash(), e));
            cachedSmallBlock = new CachedSmallBlock(null, smallBlock, txMap, null);
        }
        return cachedSmallBlock;
    }

    /**
     * according tohashGet cached{@link SmallBlock}
     * TODO  Annotation Integrity
     *
     * @param chainId   chainId/chain id
     * @param blockHash
     * @return
     */
    public static SmallBlock getSmallBlock(int chainId, NulsHash blockHash) {
        CachedSmallBlock cachedSmallBlock = getCachedSmallBlock(chainId, blockHash);
        if (cachedSmallBlock == null) {
            return null;
        }
        return cachedSmallBlock.getSmallBlock();
    }

    /**
     * Get Status
     *
     * @param chainId   chainId/chain id
     * @param blockHash
     * @return
     */
    public static BlockForwardEnum getStatus(int chainId, NulsHash blockHash) {
        Map<NulsHash, BlockForwardEnum> map = statusCacheMap.get(chainId);
        return map.computeIfAbsent(blockHash, k -> BlockForwardEnum.EMPTY);
    }

    /**
     * Set Status
     *
     * @param chainId          chainId/chain id
     * @param blockHash
     * @param blockForwardEnum
     * @return
     */
    public static void setStatus(int chainId, NulsHash blockHash, BlockForwardEnum blockForwardEnum) {
        statusCacheMap.get(chainId).put(blockHash, blockForwardEnum);
    }

    /**
     * Cache initialization
     *
     * @param chainId chainId/chain id
     */
    public static void init(int chainId) {
        ConfigBean parameters = ContextManager.getContext(chainId).getParameters();
        int config = parameters.getSmallBlockCache();
        Map<NulsHash, CachedSmallBlock> map = CollectionUtils.getSynSizedMap(config);
        smallBlockCacheMap.put(chainId, map);
        Map<NulsHash, BlockForwardEnum> statusMap = CollectionUtils.getSynSizedMap(config);
        statusCacheMap.put(chainId, statusMap);
    }

}
