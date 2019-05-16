/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.account.constant;


import com.google.common.primitives.UnsignedBytes;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.crypto.HexUtil;

import java.math.BigInteger;
import java.util.Comparator;

/**
 * @author: qinyifeng
 * @description: 配置常量
 */
public interface AccountConstant {

    String MODULE_DB_PATH = "/ac";

    /**
     * ----[ System] ----
     */
    /**
     * 系统配置项section名称
     * The configuration item section name of the kernel module.
     */
    String CFG_SYSTEM_SECTION = "system";

    /**
     * 系统配置中语言设置的字段名
     * The field name of the language set in the system configuration.
     */
    String CFG_SYSTEM_LANGUAGE = "language";

    /**
     * 系统配置中编码设置的字段名
     * The field name of the code setting in the system configuration.
     */
    String CFG_SYSTEM_DEFAULT_ENCODING = "encoding";

    /**
     * 系统配置中语言设置的字段名
     * The field name of the language set in the system configuration.
     */
    String CFG_SYSTEM_TKEYSTORE_FOLDER = "keyDir";

    /**
     * 内核模块地址端口
     * Kernel module address port
     */
    String KERNEL_MODULE_PORT = "kernelPort";

    /**
     * --------[storage configs] -------
     */
    String CFG_DB_SECTION = "storage";
    String DB_DATA_PATH = "dataDir";

    /**
     * --------[chain constant] -------
     */
    String CFG_CHAIN_SECTION = "chain";
    /**
     * 主链ID（卫星链ID）
     */
    String MAIN_CHAIN_ID = "mainChainId";

    /**
     * 主链资产ID（卫星链资产ID）
     */
    String MAIN_ASSETS_ID = "mainAssetsId";

    /**
     * --------[account constant] -------
     */
    /**
     * The name of accouts cache
     */
    String ACCOUNT_LIST_CACHE = "ACCOUNT_LIST";

    /**
     * 设置别名的费用(烧毁)
     * The cost of setting an alias
     */
    BigInteger ALIAS_FEE = BigInteger.valueOf(100000000);

    /**
     * 导出accountkeystore文件的后缀名
     * The suffix of the accountkeystore file
     */
    String ACCOUNTKEYSTORE_FILE_SUFFIX = ".keystore";

    /**
     * --------[RPC constant] -------
     */
    /**
     * SUCCESS_CODE
     */
    String SUCCESS_CODE = "1";
    /**
     * ERROR_CODE
     */
    String ERROR_CODE = "0";
    /**
     * SUCCESS_MSG
     */
    String SUCCESS_MSG = "success";
    /**
     * RPC_VERSION
     */
    double RPC_VERSION = 1.0;
    /**
     * DEFAULT PAGE_SIZE
     */
    int PAGE_SIZE = 20;


    /**
     * --------[EVENT constant] -------
     */
    /**
     * 创建账户事件的主题
     * topic of account create events
     */
    String EVENT_TOPIC_CREATE_ACCOUNT = "evt_ac_createAccount";

    /**
     * 移除账户事件的主题
     * topic of account remove events
     */
    String EVENT_TOPIC_REMOVE_ACCOUNT = "evt_ac_removeAccount";

    /**
     * 修改账户密码事件的主题
     * topic of update account password events
     */
    String EVENT_TOPIC_UPDATE_PASSWORD = "evt_ac_updatePassword";

    /**
     * --------[OTHER constant] -------
     */
    /**
     * Map初始值
     */
    int INIT_CAPACITY_16 = 16;
    int INIT_CAPACITY_8 = 8;
    int INIT_CAPACITY_4 = 4;
    int INIT_CAPACITY_2 = 2;

    /**
     * account root path
     */
    String ACCOUNT_ROOT_PATH = "io.nuls.account";

    /**
     * rpc file path
     */
    String RPC_PATH = "io.nuls.account.rpc";

    /**
     * 普通交易为非解锁交易：0，解锁金额交易（退出共识，退出委托）：-1
     */
    byte NORMAL_TX_LOCKED = 0;

    Comparator<String> PUBKEY_COMPARATOR = new Comparator<String>() {
        private Comparator<byte[]> COMPARATOR = UnsignedBytes.lexicographicalComparator();

        @Override
        public int compare(String k1, String k2) {
            return COMPARATOR.compare(HexUtil.decode(k1), HexUtil.decode(k2));
        }
    };
    /**
     * 操作系统名称
     */
    String OS_NAME = "os.name";
    /**
     * WINDOWS系统
     */
    String OS_WINDOWS = "WINDOWS";
    /**
     * 路径斜杠
     */
    String SLASH = "/";

    /**
     * 交易相关
     */
    int TX_REMARK_MAX_LEN = 100;
//    int TX_HASH_DIGEST_BYTE_MAX_LEN = 70;
//    int TX_MAX_BYTES = 300;
//    int TX_MAX_SIZE = TX_MAX_BYTES * 1024;
//    /**
//     * 本地计算nonce值的hash缓存有效时间 30秒
//     */
//    int HASH_TTL = 30000;
}
