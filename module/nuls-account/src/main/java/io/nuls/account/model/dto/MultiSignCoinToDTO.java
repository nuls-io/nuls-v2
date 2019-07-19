/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.account.model.dto;

import io.nuls.core.rpc.model.ApiModelProperty;

import java.math.BigInteger;

/**
 * @author: Charlie
 * @date: 2019/7/18
 */
public class MultiSignCoinToDTO extends BaseCoinDTO {


    @ApiModelProperty(description = "解锁时间, -1为一直锁定, 0为不锁定(默认)", required = false)
    private long lockTime = 0;

    /**
     * 将多签交易客户端参数转换成交易统一处理对象
     * @return
     */
    @Override
    public CoinDTO convert(){
        return new CoinDTO(super.address, assetsChainId, assetsId, amount, null, lockTime);
    }
    @Override
    public String getAddress() {
        return address;
    }
    @Override
    public void setAddress(String address) {
        this.address = address;
    }
    @Override
    public Integer getAssetsChainId() {
        return assetsChainId;
    }
    @Override
    public void setAssetsChainId(Integer assetsChainId) {
        this.assetsChainId = assetsChainId;
    }
    @Override
    public Integer getAssetsId() {
        return assetsId;
    }
    @Override
    public void setAssetsId(Integer assetsId) {
        this.assetsId = assetsId;
    }
    @Override
    public BigInteger getAmount() {
        return amount;
    }
    @Override
    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "MultiSignCoinToDTO{" +
                "address='" + address + '\'' +
                ", assetsChainId=" + assetsChainId +
                ", assetsId=" + assetsId +
                ", amount=" + amount +
                ", lockTime=" + lockTime +
                '}';
    }
}
