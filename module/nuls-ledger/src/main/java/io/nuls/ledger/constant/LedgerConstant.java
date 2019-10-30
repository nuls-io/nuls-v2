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
     * 基础类型与合约类型
     */
    public static final short COMMON_ASSET_TYPE = 1;
    public static final short CONTRACT_ASSET_TYPE = 2;
    /**
     * 资产小数分割位
     */
    public static final int DECIMAL_PLACES_MIN = 0;
    public static final int DECIMAL_PLACES_MAX = 18;

    public static int UNCONFIRMED_NONCE = 0;
    public static int CONFIRMED_NONCE = 1;


    /**
     * 高度解锁的阈值，大于这个值就是时间锁
     */
    public static final int MAX_HEIGHT_VALUE = 1000000000;
    public static final long LOCKED_ML_TIME_VALUE = 1000000000000L;
    /**
     * 重新统计锁定的时间 1s
     */
    public static final int TIME_RECALCULATE_FREEZE = 1;
    /**
     * FROM locked 解锁常量 0 普通交易，-1 时间解锁,1 高度解锁
     */
    public static final int UNLOCKED_TIME = -1;
    public static final int UNLOCKED_HEIGHT = 1;
    /**
     * To 永久锁定lockTime值 0 不锁定 -1 普通永久锁定，-2 dex永久锁定，x 锁定时间(s或ms)
     */
    public static final int PERMANENT_LOCK_COMMON = -1;
    public static final int PERMANENT_LOCK_DEX = -2;


    public static byte[] blackHolePublicKey = null;

    /**
     * 缓存的账户区块数量
     */
    public static final int CACHE_ACCOUNT_BLOCK = 1000;
    /**
     * 缓存同步统计数据的区块信息
     */
    public static final int CACHE_NONCE_INFO_BLOCK = 100;

    /**
     * 缓存的账户初始化nonce
     */

    public static byte[] getInitNonceByte() {
        return new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    }

    public static final int NONCE_LENGHT = 8;
    public static String DEFAULT_ENCODING = "UTF-8";
    /**
     * 未确认交易的过期时间-s，配置加载会重置该值
     */
    public static int UNCONFIRM_NONCE_EXPIRED_TIME = 100;

    public static final String COMMA = ",";
    public static final String COLON = ":";
    public static final String DOWN_LINE = "_";
}
