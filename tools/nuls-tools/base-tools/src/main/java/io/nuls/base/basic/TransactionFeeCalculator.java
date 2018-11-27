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

package io.nuls.base.basic;


import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsRuntimeException;

/**
 * @author tag
 * 2018/11/27
 */
public class TransactionFeeCalculator {

    public static final String MIN_PRECE_PRE_1024_BYTES = String.valueOf(100000);
    public static final String OTHER_PRECE_PRE_1024_BYTES = String.valueOf(1000000);

    public static final int KB = 1024;

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     * @param size 交易大小/size of the transaction
     */
    public static final String getTransferFee(int size) {
        String fee = BigIntegerUtils.mulToString(MIN_PRECE_PRE_1024_BYTES,String.valueOf(size/KB));
        if (size % KB > 0) {
            fee = BigIntegerUtils.addToString(fee,MIN_PRECE_PRE_1024_BYTES);
        }
        return fee;
    }

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     * @param size 交易大小/size of the transaction
     */
    public static final String getMaxFee(int size) {
        String fee = BigIntegerUtils.mulToString(OTHER_PRECE_PRE_1024_BYTES,String.valueOf(size/KB));
        if (size % KB > 0) {
            fee = BigIntegerUtils.addToString(fee,OTHER_PRECE_PRE_1024_BYTES);
        }
        return fee;
    }

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     * @param size 交易大小/size of the transaction
     */
    public static final String getFee(int size, String price) {
        if (BigIntegerUtils.compare(price,MIN_PRECE_PRE_1024_BYTES)<0) {
            throw new NulsRuntimeException(new Exception("data is error"));
        }
        if (BigIntegerUtils.compare(price,OTHER_PRECE_PRE_1024_BYTES)>0) {
            throw new NulsRuntimeException(new Exception("data is error"));
        }
        String fee =  BigIntegerUtils.mulToString(price,String.valueOf(size/KB));
        if (size % KB > 0) {
            fee = BigIntegerUtils.addToString(fee,price);
        }
        return fee;
    }
}
