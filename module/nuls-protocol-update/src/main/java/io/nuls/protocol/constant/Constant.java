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

package io.nuls.protocol.constant;

/**
 * 常量
 *
 * @author captain
 * @version 1.0
 * @date 19-1-22 下午3:34
 */
public interface Constant {

    /**
     * 协议版本配置文件名称
     * Module configuration file name.
     */
    String PROTOCOL_CONFIG_FILE = "versions.json";

    /**
     * 存储每条链的配置信息
     */
    String VERSION = "version";
    /**
     * 存储每条链的配置信息
     */
    String PROTOCOL_CONFIG = "protocol_config_json";
    /**
     * 存储每条链的版本统计信息
     */
    String STATISTICS = "statistics_info_";
    /**
     * 缓存每条链的版本信息
     */
    String CACHED_INFO = "cached_info_";
    /**
     * 已生效的协议信息
     */
    String PROTOCOL_VERSION_PO = "protocol_version_po_";

    /**
     * 默认扫描包路径
     */
    String DEFAULT_SCAN_PACKAGE = "io.nuls.protocol";
    /**
     * RPC默认扫描包路径
     */
    String RPC_DEFAULT_SCAN_PACKAGE = "io.nuls.protocol.rpc";
}
