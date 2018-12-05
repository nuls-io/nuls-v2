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
import io.nuls.block.model.Node;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 区块服务
 * @author captain
 * @date 18-11-6 下午4:57
 * @version 1.0
 */
public interface BlockService {

    /**
     * 获取创世块
     * @param chainId       链ID
     * @return
     * @throws Exception
     */
    Block getGenesisBlock(int chainId);

    /**
     * 获取最新的区块头
     * @param chainId       链ID
     * @return
     */
    BlockHeader getLatestBlockHeader(int chainId);

    /**
     * 获取最新的区块
     * @param chainId       链ID
     * @return
     */
    Block getLatestBlock(int chainId);

    /**
     * 根据区块高度获取区块头
     * @param chainId       链ID
     * @param height        区块高度
     * @return
     * @throws UnsupportedEncodingException
     * @throws NulsException
     */
    BlockHeader getBlockHeader(int chainId, long height);

    /**
     * 根据区块高度获取区块
     * @param chainId       链ID
     * @param height        区块高度
     * @return
     * @throws Exception
     */
    Block getBlock(int chainId, long height) throws Exception;

    /**
     * 根据区块高度区间获取区块头
     * @param chainId       链ID
     * @param startHeight   起始高度
     * @param endHeight     结束高度
     * @return
     * @throws UnsupportedEncodingException
     * @throws NulsException
     */
    List<BlockHeader> getBlockHeader(int chainId, long startHeight, long endHeight);

    /**
     * 根据区块hash获取区块头
     * @param chainId       链ID
     * @param hash          区块hash
     * @return
     * @throws NulsException
     */
    BlockHeader getBlockHeader(int chainId, NulsDigestData hash);

    /**
     * 根据区块hash获取区块
     * @param chainId       链ID
     * @param hash          区块hash
     * @return
     * @throws NulsException
     * @throws IOException
     */
    Block getBlock(int chainId, NulsDigestData hash);

    /**
     * 根据区块高度区间获取区块
     * @param chainId       链ID
     * @param startHeight   起始高度
     * @param endHeight     结束高度
     * @return
     * @throws IOException
     * @throws NulsException
     */
    List<Block> getBlock(int chainId, long startHeight, long endHeight);

    /**
     * 保存区块
     * @param chainId       链ID
     * @param block         待保存区块
     * @return
     * @throws Exception
     */
    boolean saveBlock(int chainId, Block block);

    /**
     * 回滚区块
     * @param chainId       链ID
     * @param block         待回滚区块
     * @return
     * @throws Exception
     */
    boolean rollbackBlock(int chainId, Block block);

    /**
     * 回滚区块
     * @param chainId       链ID
     * @param height        待回滚区块的高度
     * @return
     * @throws Exception
     */
    Block rollbackBlock(int chainId, long height);

    /**
     * 转发区块给连接的其他对等节点，允许一个例外（不转发给它）
     * @param chainId
     * @param hash                  区块hash
     * @param excludeNode           需要排除的节点，因为从该节点处接收的本区块
     * @return
     */
    boolean forwardBlock(int chainId, NulsDigestData hash, Node excludeNode);

    /**
     * 广播区块给连接的其他对等节点
     * @param chainId
     * @param hash
     * @return
     * @throws IOException
     * @throws NulsException
     */
    boolean broadcastBlock(int chainId, NulsDigestData hash);

    /**
     * todo 待实现
     * 启动一条链
     * @param chainId       链ID
     * @return
     */
    boolean startChain(int chainId);

    /**
     * todo 待实现
     * 停止一条链
     * @param chainId       链ID
     * @param cleanData     是否清理数据
     * @return
     */
    boolean stopChain(int chainId, boolean cleanData);

    /**
     * 验证区块正确性，需要调用共识模块、交易管理模块的接口
     * @param chainId       链ID
     * @param block         待验证区块
     * @return
     * @throws Exception
     */
    boolean verifyBlock(int chainId, Block block);

    /**
     * 初始化方法
     * @param chainId
     */
    void init(int chainId);
}
