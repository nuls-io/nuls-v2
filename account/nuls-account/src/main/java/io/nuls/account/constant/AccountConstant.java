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


import io.nuls.base.basic.AddressTool;

/**
 * @author: qinyifeng
 * @description: 配置常量
 */
public interface AccountConstant {

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
    String CFG_SYSTEM_TKEYSTORE_FOLDER = "keydir";

    /**
     * 内核模块地址
     * Kernel module address
     */
    String KERNEL_MODULE_URL = "kernelUrl";

    /**
     * --------[db configs] -------
     */
    String CFG_DB_SECTION = "db";
    String DB_DATA_PATH = "rocksdb.datapath";

    /**
     * --------[chain constant] -------
     */
    String CFG_CHAIN_SECTION = "chain";
    /**
     * 主链ID（卫星链ID）
     * */
    String MAIN_CHAIN_ID = "mainChainId";

    /**
     * 主链资产ID（卫星链资产ID）
     * */
    String MAIN_ASSETS_ID ="mainAssetsId";

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
    //Na ALIAS_NA = Na.parseNuls(1);

    /**
     * 转账交易的类型
     * the type of the transfer transaction
     */
    int TX_TYPE_TRANSFER = 2;

    /**
     * 设置账户别名的交易类型
     * Set the transaction type of account alias.
     */
    int TX_TYPE_ACCOUNT_ALIAS = 3;

    /**
     * 导出accountkeystore文件的后缀名
     * The suffix of the accountkeystore file
     */
    String ACCOUNTKEYSTORE_FILE_SUFFIX=".keystore";

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
    int PAGE_SIZE = 10;

    /**
     * 黑洞地址，该地址的资产无法找回
     * //TODO 该地址需要加上链ID，否则无法适配新的地址规则
     */
    byte[] BLACK_HOLE_ADDRESS = AddressTool.getAddress("Nse5FeeiYk1opxdc5RqYpEWkiUDGNuLs");

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
     * */
    int  INIT_CAPACITY =16;

    /**
     * account root path
     * */
    String ACCOUNT_ROOT_PATH = "io.nuls.account";

    /**
     * rpc file path
     * */
    String RPC_PATH = "io.nuls.account.rpc";
}
