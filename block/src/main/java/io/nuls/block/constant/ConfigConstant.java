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

    String CHAIN_ID = "chainId";
    String CHAIN_NAME = "chainName";
    String SERVER_IP = "serverIp";
    String SERVER_PORT = "serverPort";
    String BLOCK_MAX_SIZE = "blockMaxSize";
    String RESET_TIME = "resetTime";
    /**
     * 分叉链比主链高几个区块就进行链切换
     */
    String CHAIN_SWTICH_THRESHOLD = "chainSwtichThreshold";
    String CACHE_SIZE = "cacheSize";
    String HEIGHT_RANGE = "heightRange";
    String MAX_ROLLBACK = "maxRollback";
    String CONSISTENCY_NODE_PERCENT = "consistencyNodePercent";
    String MIN_NODE_AMOUNT = "minNodeAmount";
    /**
     * 每次从一个节点下载多少区块
     */
    String DOWNLOAD_NUMBER = "downloadNumber";
    String EXTEND_MAX_SIZE = "extendMaxSize";

    String ORPHAN_CHAIN_MAX_AGE = "orphanChainMaxAge";
}
