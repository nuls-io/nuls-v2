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
     * --------[db configs] -------
     */
    String DB_SECTION = "db";
    String DB_DATA_PATH = "rocksdb.datapath";

}
