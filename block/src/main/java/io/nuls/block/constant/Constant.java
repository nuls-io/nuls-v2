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

package io.nuls.block.constant;

/**
 * 配置常量
 *
 * @author lan
 */
public interface Constant {

    /**
     * 模块配置文件名称
     * Module configuration file name.
     */
    String MODULES_CONFIG_FILE = "modules.json";

    /**
     * db文件存放目录
     */
    String DATA_PATH = "../../data";
    /**
     * 存储每条链的配置信息
     */
    String CHAIN_PARAMETERS = "ChainParameters";
    /**
     * 存储每条链的最新高度
     */
    String CHAIN_LATEST_HEIGHT = "ChainLatestHeight";
    /**
     * 存储区块头数据
     */
    String BLOCK_HEADER = "BlockHeader";
    /**
     * 存储区块头高度与hash的键值对
     */
    String BLOCK_HEADER_INDEX = "BlockHeaderIndex";
    /**
     * 分叉链、孤儿链区块数据库前缀
     */
    String CACHED_BLOCK = "CachedBlock";

    /**
     * 每次清理几分之一
     */
    int CLEAN_PARAM = 2;

    /**
     * 默认扫描包路径
     */
    String DEFAULT_SCAN_PACKAGE = "io.nuls.block";
    /**
     * RPC默认扫描包路径
     */
    String RPC_DEFAULT_SCAN_PACKAGE = "io.nuls.block.rpc";

    /**
     * 共识工作状态
     */
    int CONSENSUS_WORKING = 1;
    /**
     * 共识等待状态
     */
    int CONSENSUS_WAITING = 0;
}
