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
 * constant
 *
 * @author captain
 * @version 1.0
 * @date 19-1-22 afternoon3:34
 */
public interface Constant {

    /**
     * Protocol version configuration file name
     * Module configuration file name.
     */
    String PROTOCOL_CONFIG_FILE = "versions.json";

    /**
     * Store configuration information for each chain
     */
    String PROTOCOL_CONFIG = "protocol_config_json";
    /**
     * Store version statistics for each chain
     */
    String STATISTICS = "statistics_info_";
    /**
     * Cache version information for each chain
     */
    String CACHED_INFO = "cached_info_";
    /**
     * Effective agreement information
     */
    String PROTOCOL_VERSION_PO = "protocol_version_po_";

    /**
     * Default scan package path
     */
    String DEFAULT_SCAN_PACKAGE = "io.nuls.protocol";
    /**
     * RPCDefault scan package path
     */
    String RPC_DEFAULT_SCAN_PACKAGE = "io.nuls.protocol.rpc";
}
