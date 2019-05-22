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

package io.nuls.block.service;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.po.BlockHeaderPo;

import java.util.List;

/**
 * 区块服务
 *
 * @author captain
 * @version 1.0
 * @date 18-11-6 下午4:57
 */
public interface BlockService {

    /**
     * 获取创世块
     *
     * @param chainId 链Id/chain id
     * @return
     */
    Block getGenesisBlock(int chainId);

    /**
     * 获取最新的区块头
     *
     * @param chainId 链Id/chain id
     * @return
     */
    BlockHeader getLatestBlockHeader(int chainId);

    /**
     * 获取最新的区块头PO
     *
     * @param chainId 链Id/chain id
     * @return
     */
    BlockHeaderPo getLatestBlockHeaderPo(int chainId);

    /**
     * 获取最新的区块
     *
     * @param chainId 链Id/chain id
     * @return
     */
    Block getLatestBlock(int chainId);

    /**
     * 根据区块高度获取区块头
     *
     * @param chainId 链Id/chain id
     * @param height  区块高度
     * @return
     */
    BlockHeader getBlockHeader(int chainId, long height);

    /**
     * 根据区块高度获取区块头
     *
     * @param chainId 链Id/chain id
     * @param height  区块高度
     * @return
     */
    BlockHeaderPo getBlockHeaderPo(int chainId, long height);

    /**
     * 根据区块高度获取区块
     *
     * @param chainId 链Id/chain id
     * @param height  区块高度
     * @return
     */
    Block getBlock(int chainId, long height);

    /**
     * 根据区块高度区间获取区块头
     *
     * @param chainId 链Id/chain id
     * @param startHeight 起始高度
     * @param endHeight   结束高度
     * @return
     */
    List<BlockHeader> getBlockHeader(int chainId, long startHeight, long endHeight);

    /**
     * 获取若干轮区块头(POC共识专用)
     *
     * @param chainId 链Id/chain id
     * @param height  从这个高度向前获取
     * @param round   轮次
     * @return
     */
    List<BlockHeader> getBlockHeaderByRound(int chainId, long height, int round);

    /**
     * 根据区块hash获取区块头
     *
     * @param chainId 链Id/chain id
     * @param hash    区块hash
     * @return
     */
    BlockHeader getBlockHeader(int chainId, NulsHash hash);

    /**
     * 根据区块hash获取区块头PO
     *
     * @param chainId 链Id/chain id
     * @param hash    区块hash
     * @return
     */
    BlockHeaderPo getBlockHeaderPo(int chainId, NulsHash hash);

    /**
     * 根据区块hash获取区块
     *
     * @param chainId 链Id/chain id
     * @param hash    区块hash
     * @return
     */
    Block getBlock(int chainId, NulsHash hash);

    /**
     * 根据区块高度区间获取区块
     *
     * @param chainId 链Id/chain id
     * @param startHeight 起始高度
     * @param endHeight   结束高度
     * @return
     */
    List<Block> getBlock(int chainId, long startHeight, long endHeight);

    /**
     * 保存区块,已经考虑失败回滚操作,不抛出异常情况下,不会有垃圾数据
     *
     * @param chainId 链Id/chain id
     * @param block    待保存区块
     * @param needLock 是否需要加锁
     * @return
     */
    boolean saveBlock(int chainId, Block block, boolean needLock);

    /**
     * 保存区块,已经考虑失败回滚操作,不抛出异常情况下,不会有垃圾数据
     *
     * @param chainId 链Id/chain id
     * @param block    待保存区块
     * @param download 是否最新区块,最新区块-1,非最新区块-0
     * @param needLock 是否需要加同步锁
     * @param broadcast 是否需要广播该区块
     * @param forward 是否需要转发该区块
     * @return
     */
    boolean saveBlock(int chainId, Block block, int download, boolean needLock, boolean broadcast, boolean forward);

    /**
     * 回滚区块,已经考虑失败回滚操作,不抛出异常情况下,不会有垃圾数据
     *
     * @param chainId 链Id/chain id
     * @param blockHeaderPo 待回滚区块头
     * @param needLock 是否需要加同步锁
     * @return
     */
    boolean rollbackBlock(int chainId, BlockHeaderPo blockHeaderPo, boolean needLock);

    /**
     * 回滚区块,已经考虑失败回滚操作,不抛出异常情况下,不会有垃圾数据
     *
     * @param chainId 链Id/chain id
     * @param height  待回滚区块高度
     * @param needLock 是否需要加同步锁
     * @return
     */
    boolean rollbackBlock(int chainId, long height, boolean needLock);

    /**
     * 转发区块给连接的其他对等节点,允许一个例外（不转发给它）
     *
     * @param chainId 链Id/chain id
     * @param hash        区块
     * @param excludeNode 需要排除的节点,因为从该节点处接收的本区块
     * @return
     */
    boolean forwardBlock(int chainId, NulsHash hash, String excludeNode);

    /**
     * 广播区块给连接的其他对等节点
     *
     * @param chainId 链Id/chain id
     * @param block
     * @return
     */
    boolean broadcastBlock(int chainId, Block block);

    /**
     * 初始化方法
     *
     * @param chainId 链Id/chain id
     */
    void init(int chainId);

    /**
     * 根据高度获取区块hash
     *
     * @param chainId 链Id/chain id
     * @param height 区块高度
     * @return
     */
    NulsHash getBlockHash(int chainId, long height);


}
