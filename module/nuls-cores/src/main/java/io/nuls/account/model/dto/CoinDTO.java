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

package io.nuls.account.model.dto;

import io.nuls.account.util.LoggerUtil;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

import java.math.BigInteger;

/**
 * Unified processing objects for assembling transaction data
 * @author: Charlie
 * @date: 2019/07/18
 */
@ApiModel
public class CoinDTO extends BaseCoinDTO implements Cloneable {

    /**
     * addressCorresponding password for signing
     */
    @ApiModelProperty(description = "Transfer out of account(from)Password for, Assembly recipient(to)Ignoring data", required = false)
    private String password;

    @ApiModelProperty(description = "Unlock time, -1To keep locked, 0To not lock(default)", required = false)
    private long lockTime = 0;

    public CoinDTO() {
    }

    public CoinDTO(String address, Integer assetsChainId, Integer assetsId, BigInteger amount, String password, long lockTime) {
        this.address = address;
        this.assetsChainId = assetsChainId;
        this.assetsId = assetsId;
        this.amount = amount;
        this.password = password;
        this.lockTime = lockTime;
    }

    @Override
    public String getAddress() {
        return super.getAddress();
    }

    @Override
    public void setAddress(String address) {
        super.setAddress(address);
    }

    @Override
    public Integer getAssetsChainId() {
        return super.getAssetsChainId();
    }

    @Override
    public void setAssetsChainId(Integer assetsChainId) {
        super.setAssetsChainId(assetsChainId);
    }

    @Override
    public Integer getAssetsId() {
        return super.getAssetsId();
    }

    @Override
    public void setAssetsId(Integer assetsId) {
        super.setAssetsId(assetsId);
    }

    @Override
    public BigInteger getAmount() {
        return super.getAmount();
    }

    @Override
    public void setAmount(BigInteger amount) {
        super.setAmount(amount);
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
