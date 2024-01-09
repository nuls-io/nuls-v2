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
 * Used to manage configuration items
 * <p>
 * Used to manage system configuration items.
 *
 * @author: Niels Wang
 */
public class NulsConfig {

    /**
     * The encoding method used by the system
     * The encoding used by the nuls system.
     */
    public static String DEFAULT_ENCODING = "UTF-8";

    /**
     * exportkeystoreBackup file directory
     */
    public static String ACCOUNTKEYSTORE_FOLDER_NAME = "keystore/backup";

    /**
     * Database storage address
     * database path
     */
    public static String DATA_PATH;

    /**
     * config file path
     */
    public static final String CONFIG_FILE_PATH = "account-config.json";

    /**
     * Main network chainID（Satellite chainID）
     */
    public static int MAIN_CHAIN_ID;
    /**
     * Black hole address, the assets at this address cannot be retrieved
     */
    public static byte[] BLACK_HOLE_PUB_KEY = null;

    /**
     * Main network chain assetsID（Satellite chain assetsID,NULSasset）
     */
    public static int MAIN_ASSETS_ID;

}
