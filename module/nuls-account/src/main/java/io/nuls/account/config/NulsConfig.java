/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.account.config;


/**
 * 用来管理配置项
 * <p>
 * Used to manage system configuration items.
 *
 * @author: Niels Wang
 */
public class NulsConfig {

    /**
     * 系统使用的编码方式
     * The encoding used by the nuls system.
     */
    public static String DEFAULT_ENCODING = "UTF-8";

    /**
     * 导出keystore备份文件目录
     */
    public static String ACCOUNTKEYSTORE_FOLDER_NAME = "keystore/backup";

    /**
     * 数据库存储地址
     * database path
     */
    public static String DATA_PATH;

    /**
     * config file path
     */
    public static final String CONFIG_FILE_PATH = "account-config.json";

    /**
     * 主网链ID（卫星链ID）
     */
    public static int MAIN_CHAIN_ID;
    /**
     * 黑洞地址，该地址的资产无法找回
     */
    public static byte[] BLACK_HOLE_PUB_KEY = null;

    /**
     * 主网链资产ID（卫星链资产ID，NULS资产）
     */
    public static int MAIN_ASSETS_ID;

}
