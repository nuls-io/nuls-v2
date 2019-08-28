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
public interface ApiConstant {


    /**
     * 模块配置文件名称
     * Module configuration file name.
     */
    String MODULES_CONFIG_FILE = "module.json";
    /**
     * 链ID
     */
    String CHAIN_ID = "chainId";

    String ASSET_ID = "assetId";

    int INIT_CAPACITY_8 = 8;

    String DB_MODULE_CONFIG = "api-config";

    /**
     * 默认扫描包路径
     */
    String DEFAULT_SCAN_PACKAGE = "io.nuls";

    /**
     * 日志级别
     */
    String LOG_LEVEL = "logLevel";

    //黄牌惩罚
    int PUBLISH_YELLOW = 1;
    //红牌惩罚
    int PUBLISH_RED = 2;

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
    //删除共识节点
    int STOP_AGENT = 2;

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
    long BlOCK_HEIGHT_TIME_DIVIDE = 1000000000000L;
    //高度冻结类型
    int FREEZE_HEIGHT_LOCK_TYPE = 1;
    //时间冻结类型
    int FREEZE_TIME_LOCK_TYPE = 2;
    //共识锁定冻结类型
    int FREEZE_CONSENSUS_LOCK_TYPE = 3;

    //合约不存在错误码
    int CONTRACT_NOT_EXIST = 100002;
    //资产转出类型
    int TRANSFER_FROM_TYPE = -1;
    //资产转入类型
    int TRANSFER_TO_TYPE = 1;

    //未确认交易
    int TX_UNCONFIRM = 0;
    //已确认交易
    int TX_CONFIRM = 1;

    int ENABLE = 1;

    int DISABLE = 0;

    //设置别名金额
    BigInteger ALIAS_AMOUNT = BigInteger.valueOf(100000000L);
    //最小委托共识金额
    BigInteger MIN_DEPOSIT = BigInteger.valueOf(20000000000000L);
}
