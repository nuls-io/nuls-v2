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

import lombok.NoArgsConstructor;
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

    private String txHash;

    private BigInteger spendAmount = BigInteger.ZERO;

    private BigInteger  earnAmount = BigInteger.ZERO;

    private BigInteger fromUnLockedAmount = BigInteger.ZERO;

    private BigInteger toLockedAmount = BigInteger.ZERO;

    private String  address = "";


    private int  assetChainId = 0;


    private int  assetId = 0;

    public UnconfirmedTx(String address,int assetChainId,int assetId){
        this.address = address;
        this.assetChainId = assetChainId;
        this.assetId = assetId;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public BigInteger getSpendAmount() {
        return spendAmount;
    }

    public void setSpendAmount(BigInteger spendAmount) {
        this.spendAmount = spendAmount;
    }

    public BigInteger getEarnAmount() {
        return earnAmount;
    }

    public void setEarnAmount(BigInteger earnAmount) {
        this.earnAmount = earnAmount;
    }

    public BigInteger getFromUnLockedAmount() {
        return fromUnLockedAmount;
    }

    public void setFromUnLockedAmount(BigInteger fromUnLockedAmount) {
        this.fromUnLockedAmount = fromUnLockedAmount;
    }

    public BigInteger getToLockedAmount() {
        return toLockedAmount;
    }

    public void setToLockedAmount(BigInteger toLockedAmount) {
        this.toLockedAmount = toLockedAmount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }
}
