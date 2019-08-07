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

package io.nuls.block.constant;

import io.nuls.base.data.Block;

import java.util.Comparator;

/**
 * 常量
 *
 * @author captain
 * @version 1.0
 * @date 19-1-22 下午3:34
 */
public interface Constant {

    /**
     * 存储每条链的配置信息
     */
    String CHAIN_PARAMETERS = "chain_parameters";
    /**
     * 存储每条链的协议配置信息
     */
    String PROTOCOL_CONFIG = "protocol_config";
    /**
     * 存储每条链的最新高度
     */
    String CHAIN_LATEST_HEIGHT = "chain_latest_height";
    /**
     * 存储区块头数据
     */
    String BLOCK_HEADER = "block_header_";
    /**
     * 存储区块头高度与hash的键值对
     */
    String BLOCK_HEADER_INDEX = "block_header_index_";
    /**
     * 分叉链、孤儿链区块数据库前缀
     */
    String CACHED_BLOCK = "cached_block_";

    /**
     * 工作状态
     */
    int MODULE_WORKING = 1;
    /**
     * 等待状态
     */
    int MODULE_WAITING = 0;

    /**
     * 区块排序器
     */
    Comparator<Block> BLOCK_COMPARATOR = (o1, o2) -> (int) (o1.getHeader().getHeight() - o2.getHeader().getHeight());

}
