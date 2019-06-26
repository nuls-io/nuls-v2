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

package io.nuls.contract.model.dto;


import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinFrom;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

import static io.nuls.contract.util.ContractUtil.bigInteger2String;

/**
 * @author: PierreLuo
 * @date: 2019-03-14
 */
@ApiModel
public class InputDto {
    @ApiModelProperty(description = "输入地址")
    private String address;
    @ApiModelProperty(description = "资产链ID")
    private int assetsChainId;
    @ApiModelProperty(description = "资产ID")
    private int assetsId;
    @ApiModelProperty(description = "花费金额")
    private String amount;
    @ApiModelProperty(description = "地址的账本nonce值")
    private String nonce;
    @ApiModelProperty(description = "解锁交易的标签（0 - 非解锁交易，1 - 解锁交易）")
    private byte locked;


    public InputDto(CoinFrom from) {
        this.address = AddressTool.getStringAddressByBytes(from.getAddress());
        this.assetsChainId = from.getAssetsChainId();
        this.assetsId = from.getAssetsId();
        this.amount = bigInteger2String(from.getAmount());
        this.nonce = RPCUtil.encode(from.getNonce());
        this.locked = from.getLocked();
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

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public byte getLocked() {
        return locked;
    }

    public void setLocked(byte locked) {
        this.locked = locked;
    }
}
