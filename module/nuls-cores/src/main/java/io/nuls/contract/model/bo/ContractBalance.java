/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.model.bo;


import java.io.Serializable;
import java.math.BigInteger;

import static io.nuls.contract.util.ContractUtil.minus;

/**
 * @author: PierreLuo
 * @date: 2018/6/7
 */
public class ContractBalance implements Serializable {

    private BigInteger balance;
    private BigInteger freeze;
    private String nonce;
    /**
     * 存储合约内部连续转账的第一个交易的nonce，用于回滚连续转账(当连续转账发生错误，那么这一次连续转账全部回滚)
     */
    private String preNonce;

    public static ContractBalance newInstance() {
        return new ContractBalance();
    }

    private ContractBalance() {
        this.balance = BigInteger.ZERO;
        this.freeze = BigInteger.ZERO;
    }

    public BigInteger getTotal() {
        return balance.add(freeze);
    }

    public void minusTemp(BigInteger amount) {
        this.balance = minus(balance, amount);
    }

    public void addTemp(BigInteger amount) {
        this.balance = balance.add(amount);
    }

    public void minusLockedTemp(BigInteger amount) {
        this.freeze = minus(freeze, amount);
    }

    public void addLockedTemp(BigInteger amount) {
        this.freeze = freeze.add(amount);
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getFreeze() {
        return freeze;
    }

    public void setFreeze(BigInteger freeze) {
        this.freeze = freeze;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getPreNonce() {
        return preNonce;
    }

    public void setPreNonce(String preNonce) {
        this.preNonce = preNonce;
    }
}