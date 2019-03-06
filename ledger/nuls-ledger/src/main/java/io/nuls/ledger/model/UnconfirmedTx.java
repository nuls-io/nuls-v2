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
package io.nuls.ledger.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

/**
 * @author lan
 * @description
 * @date 2019/01/10
 **/
@ToString
@NoArgsConstructor
public class UnconfirmedTx {

    @Setter
    @Getter
    private String txHash;
    @Setter
    @Getter
    private BigInteger spendAmount = BigInteger.ZERO;
    @Setter
    @Getter
    private BigInteger  earnAmount = BigInteger.ZERO;
    @Setter
    @Getter
    private BigInteger fromUnLockedAmount = BigInteger.ZERO;
    @Setter
    @Getter
    private BigInteger toLockedAmount = BigInteger.ZERO;
    @Setter
    @Getter
    private String  address = "";

    @Setter
    @Getter
    private int  assetChainId = 0;

    @Setter
    @Getter
    private int  assetId = 0;

    public UnconfirmedTx(String address,int assetChainId,int assetId){
        this.address = address;
        this.assetChainId = assetChainId;
        this.assetId = assetId;
    }
}
