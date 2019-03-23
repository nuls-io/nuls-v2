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
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.tools.exception.NulsException;
import lombok.Data;

/**
 * @author: PierreLuo
 * @date: 2018/8/15
 */
@Data
public class ContractAddressDto {

    private String contractAddress;
    private boolean isCreate;
    private long createTime;
    private long height;
    private long confirmCount;
    private String remarkName;
    private int status;
    private String msg;

    public ContractAddressDto() {
    }

    public ContractAddressDto(int chainId, ContractAddressInfoPo po, long bestBlockHeight, boolean isCreate, int status) throws NulsException {
        this.contractAddress = AddressTool.getStringAddressByBytes(po.getContractAddress());
        this.createTime = po.getCreateTime();
        this.isCreate = isCreate;
        this.height = po.getBlockHeight();
        this.status = status;
        if (this.height > 0) {
            this.confirmCount = bestBlockHeight - this.height;
            if(this.confirmCount == 0) {
                this.status = 0;
            } else if(this.confirmCount < 7) {
                this.status = 4;
            }
        } else {
            this.confirmCount = 0L;
        }
    }
}
