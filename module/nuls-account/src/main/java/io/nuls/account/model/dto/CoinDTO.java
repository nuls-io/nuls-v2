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

import io.nuls.account.util.LoggerUtil;

import java.math.BigInteger;

/**
 * 转账资产
 * @author: qinyifeng
 * @date: 2018/12/10
 */
public class CoinDTO implements Cloneable {

    private String address;

    private Integer assetsChainId;

    private Integer assetsId;

    /**
     * uint128 转出数量
     */
    private BigInteger amount;

    /**
     * address对应的密码，用于签名
     */
    private String password;

    private long lockTime = 0L;

    public CoinDTO() {
    }

    public CoinDTO(String address, Integer assetsChainId, Integer assetsId, BigInteger amount, String password) {
        this.address = address;
        this.assetsChainId = assetsChainId;
        this.assetsId = assetsId;
        this.amount = amount;
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAssetsChainId() {
        return assetsChainId;
    }

    public void setAssetsChainId(int assetsChainId) {
        this.assetsChainId = assetsChainId;
    }

    public int getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(int assetsId) {
        this.assetsId = assetsId;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    @Override
    public String toString() {
        return "CoinDto{" +
                "address='" + address + '\'' +
                ", assetsChainId=" + assetsChainId +
                ", assetsId=" + assetsId +
                ", amount=" + amount +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public Object clone(){
        CoinDTO coinDto = null;
        try {
            coinDto = (CoinDTO)super.clone();
        } catch (CloneNotSupportedException e) {
            LoggerUtil.LOG.error("", e);
        }
        return coinDto;
    }
}
