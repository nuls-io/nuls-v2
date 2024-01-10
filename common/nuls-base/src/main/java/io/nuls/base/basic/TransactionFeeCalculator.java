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

package io.nuls.base.basic;


import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.exception.NulsRuntimeException;

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
     * Calculate the required transaction fees based on the size of the transaction
     * According to the transaction size calculate the handling fee.
     * @param size Transaction size/size of the transaction
     */
    public static final BigInteger getNormalTxFee(int size) {
        BigInteger fee = NORMAL_PRICE_PRE_1024_BYTES.multiply(new BigInteger(String.valueOf(size/KB)));
        if (size % KB > 0) {
            fee = fee.add(NORMAL_PRICE_PRE_1024_BYTES);
        }
        return fee;
    }

    /**
     * Calculate the required transaction fees based on the size of the transaction
     * According to the transaction size calculate the handling fee.
     * @param size Transaction size/size of the transaction
     */
    public static final BigInteger getConsensusTxFee(int size,long unit) {
        BigInteger unitBigInteger = BigInteger.valueOf(unit);
        BigInteger fee = unitBigInteger.multiply(new BigInteger(String.valueOf(size/KB)));
        if (size % KB > 0) {
            fee = fee.add(unitBigInteger);
        }
        return fee;
    }

    /**
     * Calculate the handling fee to be paid based on the size of unsigned transactions
     * @param size Unsigned transaction size/ size of the unsigned transaction
     * @return Transaction fees
     */
    public static final BigInteger getNormalUnsignedTxFee(int size) {
        size += P2PHKSignature.SERIALIZE_LENGTH;
        BigInteger fee = NORMAL_PRICE_PRE_1024_BYTES.multiply(new BigInteger(String.valueOf(size/KB)));
        if (size % KB > 0) {
            fee = fee.add(NORMAL_PRICE_PRE_1024_BYTES);
        }
        return fee;
    }

    /**
     * Calculate the required transaction fees based on the size of the transaction
     * According to the transaction size calculate the handling fee.
     * @param size Transaction size/size of the transaction
     */
    public static final BigInteger getCrossTxFee(int size) {
        BigInteger fee = CROSSTX_PRICE_PRE_1024_BYTES.multiply(new BigInteger(String.valueOf(size/KB)));
        if (size % KB > 0) {
            fee = fee.add(CROSSTX_PRICE_PRE_1024_BYTES);
        }
        return fee;
    }

    /**
     * Calculate the required transaction fees based on the size of the transaction
     * According to the transaction size calculate the handling fee.
     * @param size Transaction size/size of the transaction
     */
    public static final BigInteger getFee(int size, BigInteger price) {
        if(price.compareTo(NORMAL_PRICE_PRE_1024_BYTES)<0){
            throw new NulsRuntimeException(new Exception("entity is error"));
        }
        if(price.compareTo(CROSSTX_PRICE_PRE_1024_BYTES)>0) {
            throw new NulsRuntimeException(new Exception("entity is error"));
        }
        BigInteger fee = price.multiply(new BigInteger(String.valueOf(size/KB)));
        if (size % KB > 0) {
            fee = fee.add(price);
        }
        return fee;
    }
}
