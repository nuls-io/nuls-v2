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

package io.nuls.provider.model.dto;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinTo;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

import static io.nuls.v2.util.ContractUtil.bigInteger2String;


/**
 * @author: PierreLuo
 * @date: 2019-03-14
 */
@ApiModel
public class OutputDto {
    @ApiModelProperty(description = "输出地址")
    private String address;
    @ApiModelProperty(description = "资产链ID")
    private int assetsChainId;
    @ApiModelProperty(description = "资产ID")
    private int assetsId;
    @ApiModelProperty(description = "输出金额")
    private String amount;
    @ApiModelProperty(description = "锁定时间")
    private long lockTime;

    public OutputDto() {}

    public OutputDto(CoinTo to) {
        this.address = AddressTool.getStringAddressByBytes(to.getAddress());
        this.assetsChainId = to.getAssetsChainId();
        this.assetsId = to.getAssetsId();
        this.amount = bigInteger2String(to.getAmount());
        this.lockTime = to.getLockTime();
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }
}
