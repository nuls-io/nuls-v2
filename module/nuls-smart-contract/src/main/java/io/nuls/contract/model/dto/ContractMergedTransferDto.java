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
package io.nuls.contract.model.dto;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.contract.model.bo.ContractMergedTransfer;
import io.nuls.contract.model.bo.Output;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.contract.util.ContractUtil.bigInteger2String;

/**
 * @author: PierreLuo
 */
@ApiModel
public class ContractMergedTransferDto {
    @ApiModelProperty(description = "合约生成交易：合约转账交易hash")
    private String txHash;
    @ApiModelProperty(description = "转出的合约地址")
    private String from;
    @ApiModelProperty(description = "转账金额")
    private String value;
    @ApiModelProperty(description = "转入的地址列表", type = @TypeDescriptor(value = List.class, collectionElement = ContractOutputDto.class))
    private List<ContractOutputDto> outputs;
    @ApiModelProperty(description = "调用合约交易hash（源交易hash，合约交易由调用合约交易派生而来）")
    private String orginTxHash;

    public ContractMergedTransferDto(ContractMergedTransfer transfer) {
        this.from = AddressTool.getStringAddressByBytes(transfer.getFrom());
        this.value = bigInteger2String(transfer.getValue());
        NulsHash thatHash = transfer.getHash();
        this.txHash = thatHash == null ? null : thatHash.toHex();
        NulsHash thatOrginTxHash = transfer.getOrginHash();
        this.orginTxHash = thatOrginTxHash == null ? null : thatOrginTxHash.toHex();
        this.makeOutputs(transfer.getOutputs());
    }

    private void makeOutputs(List<Output> outputs) {
        if (outputs != null && !outputs.isEmpty()) {
            this.outputs = new ArrayList<>(outputs.size());
            for (Output output : outputs) {
                this.outputs.add(new ContractOutputDto(output));
            }
        }
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<ContractOutputDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ContractOutputDto> outputs) {
        this.outputs = outputs;
    }

    public String getOrginTxHash() {
        return orginTxHash;
    }

    public void setOrginTxHash(String orginTxHash) {
        this.orginTxHash = orginTxHash;
    }
}
