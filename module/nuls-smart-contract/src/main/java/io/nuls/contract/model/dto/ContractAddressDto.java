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


import io.nuls.base.basic.AddressTool;
import io.nuls.contract.enums.ContractStatus;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.core.exception.NulsException;

/**
 * @author: PierreLuo
 * @date: 2018/8/15
 */
public class ContractAddressDto {

    private String contractAddress;
    private boolean isCreate;
    private long createTime;
    private long height;
    private long confirmCount;
    private String alias;
    /**
     *  enum - ContractStatus
     */
    private int status;
    private String msg;

    public ContractAddressDto() {
    }

    public ContractAddressDto(ContractAddressInfoPo po, long bestBlockHeight, int status) throws NulsException {
        this.contractAddress = AddressTool.getStringAddressByBytes(po.getContractAddress());
        this.createTime = po.getCreateTime();
        this.height = po.getBlockHeight();
        this.alias = po.getAlias();
        this.status = status;
        if (this.height > 0) {
            this.confirmCount = bestBlockHeight - this.height;
            if (this.confirmCount == 0) {
                this.status = ContractStatus.NOT_EXISTS_OR_CONFIRMING.status();
            } else if (this.confirmCount < 7) {
                this.status = ContractStatus.LOCKED.status();
            }
        } else {
            this.confirmCount = 0L;
        }
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public void setCreate(boolean create) {
        isCreate = create;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getConfirmCount() {
        return confirmCount;
    }

    public void setConfirmCount(long confirmCount) {
        this.confirmCount = confirmCount;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
