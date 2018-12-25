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

package io.nuls.block.service;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.SmallBlock;
import io.nuls.block.model.Node;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.tools.core.annotation.Service;

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
     * @param chainId 链ID
     * @return
     */
    Block getGenesisBlock(int chainId);

    /**
     * 获取最新的区块头
     *
     * @param chainId 链ID
     * @return
     */
    BlockHeader getLatestBlockHeader(int chainId);

    /**
     * 获取最新的区块
     *
     * @param chainId 链ID
     * @return
     */
    Block getLatestBlock(int chainId);

    /**
     * 根据区块高度获取区块头
     *
     * @param chainId 链ID
     * @param height  区块高度
     * @return
     */
    BlockHeaderPo getBlockHeader(int chainId, long height);

    /**
     * 根据区块高度获取区块
     *
     * @param chainId 链ID
     * @param height  区块高度
     * @return
     */
    Block getBlock(int chainId, long height);

    /**
     * 根据区块高度区间获取区块头
     *
     * @param chainId     链ID
     * @param startHeight 起始高度
     * @param endHeight   结束高度
     * @return
     */
    List<BlockHeader> getBlockHeader(int chainId, long startHeight, long endHeight);

    /**
     * 根据区块hash获取区块头
     *
     * @param chainId 链ID
     * @param hash    区块hash
     * @return
     */
    BlockHeader getBlockHeader(int chainId, NulsDigestData hash);

    /**
     * 根据区块hash获取区块
     *
     * @param chainId 链ID
     * @param hash    区块hash
     * @return
     */
    Block getBlock(int chainId, NulsDigestData hash);

    /**
     * 根据区块高度区间获取区块
     *
     * @param chainId     链ID
     * @param startHeight 起始高度
     * @param endHeight   结束高度
     * @return
     */
    List<Block> getBlock(int chainId, long startHeight, long endHeight);

    /**
     * 保存区块,已经考虑失败回滚操作,不抛出异常情况下,不会有垃圾数据
     *
     * @param chainId 链ID
     * @param block   待保存区块
     * @return
     */
    boolean saveBlock(int chainId, Block block);

    /**
     * 保存区块,已经考虑失败回滚操作,不抛出异常情况下,不会有垃圾数据
     *
     * @param chainId 链ID
     * @param block   待保存区块
     * @param download   是否最新区块,最新区块-1,非最新区块-0
     * @return
     */
    boolean saveBlock(int chainId, Block block, int download);

    /**
     * 回滚区块,已经考虑失败回滚操作,不抛出异常情况下,不会有垃圾数据
     *
     * @param chainId       链ID
     * @param blockHeaderPo 待回滚区块头
     * @return
     */
    boolean rollbackBlock(int chainId, BlockHeaderPo blockHeaderPo);

    /**
     * 转发区块给连接的其他对等节点,允许一个例外（不转发给它）
     *
     * @param chainId
     * @param hash        区块
     * @param excludeNode 需要排除的节点,因为从该节点处接收的本区块
     * @return
     */
    boolean forwardBlock(int chainId, NulsDigestData hash, String excludeNode);

    /**
     * 广播区块给连接的其他对等节点
     *
     * @param chainId
     * @param block
     * @return
     */
    boolean broadcastBlock(int chainId, Block block);

    /**
     * todo 待实现
     * 启动一条链
     *
     * @param chainId 链ID
     * @return
     */
    boolean startChain(int chainId);

    /**
     * todo 待实现
     * 停止一条链
     *
     * @param chainId   链ID
     * @param cleanData 是否清理数据
     * @return
     */
    boolean stopChain(int chainId, boolean cleanData);

    /**
     * 初始化方法
     *
     * @param chainId
     */
    void init(int chainId);

}
