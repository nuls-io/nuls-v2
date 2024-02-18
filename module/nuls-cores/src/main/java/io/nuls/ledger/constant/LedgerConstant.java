/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.constant;

/**
 * Created by ljs on 2018/11/19.
 *
 * @author lanjinsheng
 */
public class LedgerConstant {
    /**
     * Basic type and contract type
     */
    public static final short COMMON_ASSET_TYPE = 1;
    public static final short CONTRACT_ASSET_TYPE = 2;
    /**
     * Decimal Division of Assets
     */
    public static final int DECIMAL_PLACES_MIN = 0;
    public static final int DECIMAL_PLACES_MAX = 18;

    public static int UNCONFIRMED_NONCE = 0;
    public static int CONFIRMED_NONCE = 1;


    /**
     * The threshold for height unlocking, greater than which is the time lock
     */
    public static final int MAX_HEIGHT_VALUE = 1000000000;
    public static final long LOCKED_ML_TIME_VALUE = 1000000000000L;
    /**
     * Recalculate the locked time 1s
     */
    public static final int TIME_RECALCULATE_FREEZE = 1;
    /**
     * FROM locked Unlocking Constants 0 Ordinary transactions,-1 Time unlocking,1 Height unlocking
     */
    public static final int UNLOCKED_TIME = -1;
    public static final int UNLOCKED_HEIGHT = 1;
    /**
     * To Permanent locklockTimevalue 0 Not locked -1 Normal permanent lock,-2 dexPermanent lock,x Lock time(sorms)
     */
    public static final int PERMANENT_LOCK_COMMON = -1;
    public static final int PERMANENT_LOCK_DEX = -2;


    public static byte[] blackHolePublicKey = null;

    /**
     * Number of cached account blocks
     */
    public static final int CACHE_ACCOUNT_BLOCK = 1000;
    /**
     * Block information of cache synchronization statistics data
     */
    public static final int CACHE_NONCE_INFO_BLOCK = 100;

    /**
     * Cache account initializationnonce
     */

    public static byte[] getInitNonceByte() {
        return new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    }

    public static final int NONCE_LENGHT = 8;
    public static String DEFAULT_ENCODING = "UTF-8";
    /**
     * Expiration time of unconfirmed transactions-sConfiguration loading will reset this value
     */
    public static int UNCONFIRM_NONCE_EXPIRED_TIME = 100;

    public static final String COMMA = ",";
    public static final String COLON = ":";
    public static final String DOWN_LINE = "_";
}
