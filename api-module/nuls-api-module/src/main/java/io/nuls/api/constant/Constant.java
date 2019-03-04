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

package io.nuls.api.constant;

import java.math.BigInteger;

/**
 * 常量
 *
 * @author captain
 * @version 1.0
 * @date 19-1-22 下午3:34
 */
public interface Constant {


    /**
     * 模块配置文件名称
     * Module configuration file name.
     */
    String MODULES_CONFIG_FILE = "modules.json";
    /**
     * 链ID
     */
    String CHAIN_ID = "chainId";

    String DB_IP = "dbIp";

    String DB_PORT = "dbPort";

    int INIT_CAPACITY_8 = 8;
    /**
     * db文件存放目录
     */
    String DATA_PATH = "../../../../data/api";

    String DB_MODULE_CONFIG = "api-config";

    /**
     * 默认扫描包路径
     */
    String DEFAULT_SCAN_PACKAGE = "io.nuls.api";
    /**
     * RPC默认扫描包路径
     */
    String RPC_DEFAULT_SCAN_PACKAGE = "io.nuls.api.rpc";

    /**
     * 日志级别
     */
    String LOG_LEVEL = "logLevel";
    /**
     * 日志级别
     */
    String INTERVAL = "interval";
    /**
     * 日志级别
     */
    String EFFECTIVE_RATIO_MINIMUM = "effectiveRatioMinimum";
    /**
     * 日志级别
     */
    String EFFECTIVE_RATIO_MAXIMUM = "effectiveRatioMaximum";
    /**
     * 日志级别
     */
    String CONTINUOUS_INTERVAL_COUNT_MAXIMUM = "continuousIntervalCountMaximum";
    /**
     * 日志级别
     */
    String CONTINUOUS_INTERVAL_COUNT_MINIMUM = "continuousIntervalCountMinimum";


    //黄牌惩罚
    int PUBLISH_YELLOW = 1;
    //红牌惩罚
    int PUTLISH_RED = 2;
    //尝试分叉
    int TRY_FORK = 1;
    //打包双花交易
    int DOUBLE_SPEND = 2;
    //太多黄牌惩罚
    int TOO_MUCH_YELLOW_PUNISH = 3;
    //委托共识
    int JOIN_CONSENSUS = 0;
    //取消委托共识
    int CANCEL_CONSENSUS = 1;
    //创建合约成功
    int CONTRACT_STATUS_NORMAL = 0;
    //创建合约失败
    int CONTRACT_STATUS_FAIL = -1;
    //合约代码正在审核中
    int CONTRACT_STATUS_APPROVING = 1;
    //合约代码审核通过
    int CONTRACT_STATUS_PASSED = 2;
    //合约已失效
    int CONTRACT_STATUS_DELETE = 3;
    //时间高度分界线
    long BlOCKHEIGHT_TIME_DIVIDE = 1000000000000L;
    //合约不存在错误码
    int CONTRACT_NOT_EXIST = 100002;

    int TX_TYPE_COINBASE = 1;
    int TX_TYPE_TRANSFER = 2;
    int TX_TYPE_ALIAS = 3;
    int TX_TYPE_REGISTER_AGENT = 4;
    int TX_TYPE_JOIN_CONSENSUS = 5;
    int TX_TYPE_CANCEL_DEPOSIT = 6;
    int TX_TYPE_YELLOW_PUNISH = 7;
    int TX_TYPE_RED_PUNISH = 8;
    int TX_TYPE_STOP_AGENT = 9;
    int TX_TYPE_DATA = 10;
    int TX_TYPE_CREATE_CONTRACT = 100;
    int TX_TYPE_CALL_CONTRACT = 101;
    int TX_TYPE_DELETE_CONTRACT = 102;
    int TX_TYPE_CONTRACT_TRANSFER = 103;

    BigInteger ALIAS_AMOUNT = BigInteger.valueOf(100000000L);
}
