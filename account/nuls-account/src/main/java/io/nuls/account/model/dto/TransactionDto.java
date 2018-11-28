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

package io.nuls.account.model.dto;

/**
 * @author: EdwardChan
 */

public class TransactionDto<T> {
    /**
     * the type of transaction
     */
    private int type;

    /**
     * transaction time
     */
    private String time;

    /**
     * scriptSig
     */
    private String scriptSig;

    /**
     * hash
     */
    private String hash;

    /**
     * coinData
     */
    private CoinDataDto coinData;

    /**
     * transaction data
     */
    private T txData;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(String scriptSig) {
        this.scriptSig = scriptSig;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public CoinDataDto getCoinData() {
        return coinData;
    }

    public void setCoinData(CoinDataDto coinData) {
        this.coinData = coinData;
    }

    public T getTxData() {
        return txData;
    }

    public void setTxData(T txData) {
        this.txData = txData;
    }
}
