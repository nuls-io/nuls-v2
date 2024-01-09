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

package io.nuls.test.cases.transcation.batch.fasttx;


import com.google.common.primitives.UnsignedBytes;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.crypto.HexUtil;

import java.math.BigInteger;
import java.util.Comparator;

/**
 * @author: qinyifeng
 * @description: Configure Constants
 */
public interface AccountConstant {

    String MODULE_DB_PATH = "/ac";

    /**
     * ----[ System] ----
     */
    /**
     * System configuration itemssectionname
     * The configuration item section name of the kernel module.
     */
    String CFG_SYSTEM_SECTION = "system";

    /**
     * Field names for language settings in system configuration
     * The field name of the language set in the system configuration.
     */
    String CFG_SYSTEM_LANGUAGE = "language";

    /**
     * Field names for coding settings in system configuration
     * The field name of the code setting in the system configuration.
     */
    String CFG_SYSTEM_DEFAULT_ENCODING = "encoding";

    /**
     * Field names for language settings in system configuration
     * The field name of the language set in the system configuration.
     */
    String CFG_SYSTEM_TKEYSTORE_FOLDER = "keyDir";

    /**
     * Kernel module address port
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
     * Main chainID（Satellite chainID）
     */
    String MAIN_CHAIN_ID = "mainChainId";

    /**
     * Main chain assetsID（Satellite chain assetsID）
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
     * The cost of setting an alias(Burn down)
     * The cost of setting an alias
     */
    BigInteger ALIAS_FEE = BigInteger.valueOf(100000000);

    /**
     * exportaccountkeystoreThe suffix name of the file
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
     * Black hole address, the assets at this address cannot be retrieved
     * //TODO Test address, to be modified later
     */
    byte[] BLACK_HOLE_ADDRESS = AddressTool.getAddress("tNULSeBaMkqeHbTxwKqyquFcbewVTUDHPkF11o");

    /**
     * --------[EVENT constant] -------
     */
    /**
     * Theme for creating account events
     * topic of account create events
     */
    String EVENT_TOPIC_CREATE_ACCOUNT = "evt_ac_createAccount";

    /**
     * Remove the theme of account events
     * topic of account remove events
     */
    String EVENT_TOPIC_REMOVE_ACCOUNT = "evt_ac_removeAccount";

    /**
     * The theme of the account password modification event
     * topic of update account password events
     */
    String EVENT_TOPIC_UPDATE_PASSWORD = "evt_ac_updatePassword";

    /**
     * --------[OTHER constant] -------
     */
    /**
     * MapInitial value
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
     * Ordinary transactions are non unlocked transactions：0Unlock amount transaction（Exit consensus, exit delegation）：-1
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
     * Operating System Name
     */
    String OS_NAME = "os.name";
    /**
     * WINDOWSsystem
     */
    String OS_WINDOWS = "WINDOWS";
    /**
     * Path slash
     */
    String SLASH = "/";

    /**
     * Transaction related
     */
    int TX_REMARK_MAX_LEN = 100;
//    int TX_HASH_DIGEST_BYTE_MAX_LEN = 70;
//    int TX_MAX_BYTES = 300;
//    int TX_MAX_SIZE = TX_MAX_BYTES * 1024;
//    /**
//     * Local computingnonceValue ofhashCache validity time 30second
//     */
//    int HASH_TTL = 30000;
}
