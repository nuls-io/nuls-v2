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
package io.nuls.core.constant;

/**
 * 交易状态枚举
 * Enumeration of transaction status
 *
 * @author Niels
 */
public enum TxStatusEnum {

    /**
     * 未确认状态
     * not packaged
     */
    UNCONFIRM((byte)0),
    /**
     * 已确认状态
     * packaged and saved
     */
    CONFIRMED((byte)1),
    /**
     * 已打包状态
     * packaged and saved
     */
    COMMITTED((byte)2);

    private byte status;

    TxStatusEnum(byte status) {
        this.status = status;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public static TxStatusEnum getStatus(int status) {
        if(status == 0) {
            return UNCONFIRM;
        }
        if(status == 1) {
            return CONFIRMED;
        }
        return null;
    }
}
