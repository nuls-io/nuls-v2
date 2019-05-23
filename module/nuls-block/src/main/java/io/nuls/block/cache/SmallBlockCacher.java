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
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.SmallBlock;
import io.nuls.block.constant.BlockForwardEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.CachedSmallBlock;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统正常运行时缓存区块的广播、转发消息
 * 1.缓存收到的SmallBlock
 * 2.缓存状态标记
 *
 * @author captain
 * @version 1.0
 * @date 18-12-6 上午10:49
 */
@Component
public class SmallBlockCacher {

    @Autowired
    private static BlockService service;

    /**
     * 缓存区块转发、广播过程中收到的{@link SmallBlock},可以用来排除重复消息,
     */
    private static Map<Integer, Map<NulsHash, CachedSmallBlock>> smallBlockCacheMap = new ConcurrentHashMap<>();

    /**
     * 记录每一个区块的传播状态
     */
    private static Map<Integer, Map<NulsHash, BlockForwardEnum>> statusCacheMap = new ConcurrentHashMap<>();

    /**
     * 将一个SmallBlock放入内存中,若不主动删除,则在缓存存满或者存在时间超过1000秒时,自动清理
     *
     * @param chainId 链Id/chain id
     * @param cachedSmallBlock
     */
    public static void cacheSmallBlock(int chainId, CachedSmallBlock cachedSmallBlock) {
        SmallBlock smallBlock = cachedSmallBlock.getSmallBlock();
        smallBlockCacheMap.get(chainId).put(smallBlock.getHeader().getHash(), cachedSmallBlock);
    }

    /**
     * 根据hash获取缓存的{@link SmallBlock}
     * TODO  注释完整性
     *
     * @param chainId 链Id/chain id
     * @param blockHash
     * @return
     */
    public static CachedSmallBlock getCachedSmallBlock(int chainId, NulsHash blockHash) {
        CachedSmallBlock cachedSmallBlock = smallBlockCacheMap.get(chainId).get(blockHash);
        if (cachedSmallBlock == null) {
            Block block = service.getBlock(chainId, blockHash);
            SmallBlock smallBlock = BlockUtil.getSmallBlock(chainId, block);
            cachedSmallBlock = new CachedSmallBlock(null, smallBlock, null);
        }
        return cachedSmallBlock;
    }

    /**
     * 根据hash获取缓存的{@link SmallBlock}
     * TODO  注释完整性
     *
     * @param chainId   链Id/chain id
     * @param blockHash
     * @return
     */
    public static SmallBlock getSmallBlock(int chainId, NulsHash blockHash) {
        return getCachedSmallBlock(chainId, blockHash).getSmallBlock();
    }

    /**
     * 获取状态
     *
     * @param chainId 链Id/chain id
     * @param blockHash
     * @return
     */
    public static BlockForwardEnum getStatus(int chainId, NulsHash blockHash) {
        Map<NulsHash, BlockForwardEnum> map = statusCacheMap.get(chainId);
        return map.computeIfAbsent(blockHash, k -> BlockForwardEnum.EMPTY);
    }

    /**
     * 设置状态
     *
     * @param chainId 链Id/chain id
     * @param blockHash
     * @param blockForwardEnum
     * @return
     */
    public static void setStatus(int chainId, NulsHash blockHash, BlockForwardEnum blockForwardEnum) {
        statusCacheMap.get(chainId).put(blockHash, blockForwardEnum);
    }

    /**
     * 缓存初始化
     *
     * @param chainId 链Id/chain id
     */
    public static void init(int chainId) {
        ChainParameters parameters = ContextManager.getContext(chainId).getParameters();
        int config = parameters.getSmallBlockCache();
        Map<NulsHash, CachedSmallBlock> map = CollectionUtils.getSynSizedMap(config);
        smallBlockCacheMap.put(chainId, map);
        Map<NulsHash, BlockForwardEnum> statusMap = CollectionUtils.getSynSizedMap(config);
        statusCacheMap.put(chainId, statusMap);
    }

}
