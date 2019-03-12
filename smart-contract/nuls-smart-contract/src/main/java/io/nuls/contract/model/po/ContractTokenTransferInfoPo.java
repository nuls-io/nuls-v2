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

package io.nuls.contract.model.po;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * @author: PierreLuo
 * @date: 2018/7/23
 */
@Getter
@Setter
public class ContractTokenTransferInfoPo implements Serializable {

    private byte[] from;
    private byte[] to;
    private BigInteger value;
    private String contractAddress;
    private String name;
    private String symbol;
    private long decimals;
    private long time;
    private byte status;
    private byte[] txHash;
    private long blockHeight;


    public String getInfo(byte[] address) {
        BigDecimal result = BigDecimal.ZERO;
        if (this.status == 2) {
            return result.toPlainString();
        }

        if (Arrays.equals(from, address)) {
            result = result.subtract(new BigDecimal(value).divide(BigDecimal.TEN.pow((int) decimals)));
        }
        if (Arrays.equals(to, address)) {
            result = result.add(new BigDecimal(value).divide(BigDecimal.TEN.pow((int) decimals)));
        }
        if (result.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + result.toPlainString();
        } else {
            return result.toPlainString();
        }
    }

}
