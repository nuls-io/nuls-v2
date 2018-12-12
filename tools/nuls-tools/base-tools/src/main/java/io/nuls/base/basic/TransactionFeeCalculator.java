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


import io.nuls.tools.exception.NulsRuntimeException;

import java.math.BigInteger;


/**
 * @author tag
 * 2018/11/27
 */
public class TransactionFeeCalculator {

    public static final BigInteger NORMAL_PRICE_PRE_1024_BYTES = BigInteger.valueOf(100000);
    public static final BigInteger CROSSTX_PRICE_PRE_1024_BYTES = BigInteger.valueOf(1000000);

    public static final int KB = 1024;

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     * @param size 交易大小/size of the transaction
     */
    public static final BigInteger getNormalTxFee(int size) {
        BigInteger fee = NORMAL_PRICE_PRE_1024_BYTES.multiply(new BigInteger(String.valueOf(size/KB)));
        if (size % KB > 0) {
            fee = fee.add(NORMAL_PRICE_PRE_1024_BYTES);
        }
        return fee;
    }

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     * @param size 交易大小/size of the transaction
     */
    public static final BigInteger getCrossTxFee(int size) {
        BigInteger fee = CROSSTX_PRICE_PRE_1024_BYTES.multiply(new BigInteger(String.valueOf(size/KB)));
        if (size % KB > 0) {
            fee = fee.add(CROSSTX_PRICE_PRE_1024_BYTES);
        }
        return fee;
    }

    /**
     * 根据交易大小计算需要交纳的手续费
     * According to the transaction size calculate the handling fee.
     * @param size 交易大小/size of the transaction
     */
    public static final BigInteger getFee(int size, BigInteger price) {
        if(price.compareTo(NORMAL_PRICE_PRE_1024_BYTES)<0){
            throw new NulsRuntimeException(new Exception("data is error"));
        }
        if(price.compareTo(CROSSTX_PRICE_PRE_1024_BYTES)>0) {
            throw new NulsRuntimeException(new Exception("data is error"));
        }
        BigInteger fee = price.multiply(new BigInteger(String.valueOf(size/KB)));
        if (size % KB > 0) {
            fee = fee.add(price);
        }
        return fee;
    }
}
