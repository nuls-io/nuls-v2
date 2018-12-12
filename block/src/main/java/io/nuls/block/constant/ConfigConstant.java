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
 * @author captain
 * @date 18-11-21 下午3:41
 * @version 1.0
 */
public interface ConfigConstant {

    /**
     * 链ID
     */
    String CHAIN_ID = "chainId";
    /**
     * 链名
     */
    String CHAIN_NAME = "chainName";
    /**
     * 服务IP
     */
    String SERVER_IP = "serverIp";
    /**
     * 服务端口
     */
    String SERVER_PORT = "serverPort";
    /**
     * 区块大小阈值
     */
    String BLOCK_MAX_SIZE = "blockMaxSize";
    /**
     * 网络重置阈值
     */
    String RESET_TIME = "resetTime";
    /**
     * 分叉链比主链高几个区块就进行链切换
     */
    String CHAIN_SWTICH_THRESHOLD = "chainSwtichThreshold";
    /**
     * 分叉链、孤儿链区块最大缓存数量
     */
    String CACHE_SIZE = "cacheSize";
    /**
     * 接收新区块的范围
     */
    String HEIGHT_RANGE = "heightRange";
    /**
     * 每次回滚区块最大值
     */
    String MAX_ROLLBACK = "maxRollback";
    /**
     * 一致节点比例
     */
    String CONSISTENCY_NODE_PERCENT = "consistencyNodePercent";
    /**
     * 系统运行最小节点数
     */
    String MIN_NODE_AMOUNT = "minNodeAmount";
    /**
     * 每次从一个节点下载多少区块
     */
    String DOWNLOAD_NUMBER = "downloadNumber";
    /**
     * 区块头中扩展字段的最大长度
     */
    String EXTEND_MAX_SIZE = "extendMaxSize";
    /**
     * 为阻止恶意节点提前出块，设置此参数
     * 区块时间戳大于当前时间多少就丢弃该区块
     */
    String VALID_BLOCK_INTERVAL = "validBlockInterval";
    /**
     * 同步区块时最多缓存多少个区块
     */
    String BLOCK_CACHE = "blockCache";
    /**
     * 系统正常运行时最多缓存多少个从别的节点接收到的小区块
     */
    String SMALL_BLOCK_CACHE = "smallBlockCache";
}
