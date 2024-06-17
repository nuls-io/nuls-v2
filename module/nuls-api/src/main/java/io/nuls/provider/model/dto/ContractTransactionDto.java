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

package io.nuls.provider.model.dto;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.Key;
import io.nuls.core.rpc.model.TypeDescriptor;

import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 */
@ApiModel
public class ContractTransactionDto {

    @ApiModelProperty(description = "transactionhash")
    private String hash;
    @ApiModelProperty(description = "Transaction type")
    private Integer type;
    @ApiModelProperty(description = "Transaction time")
    private Long time;
    @ApiModelProperty(description = "block height")
    private Long blockHeight;
    @ApiModelProperty(description = "Transaction fees")
    private String fee;
    @ApiModelProperty(description = "Transaction amount")
    private String value;
    @ApiModelProperty(description = "Remarks")
    private String remark;
    @ApiModelProperty(description = "Signature information")
    private String scriptSig;
    @ApiModelProperty(description = "Transaction status（0 - Confirming,1 - Confirmed）")
    private Integer status;
    @ApiModelProperty(description = "Number of transaction confirmations")
    private Long confirmCount;
    @ApiModelProperty(description = "Transaction size")
    private int size;
    @ApiModelProperty(description = "Transaction Input Set", type = @TypeDescriptor(value = List.class, collectionElement = InputDto.class))
    private List<InputDto> inputs;
    @ApiModelProperty(description = "Transaction output set", type = @TypeDescriptor(value = List.class, collectionElement = OutputDto.class))
    private List<OutputDto> outputs;
    @ApiModelProperty(description = "Contract trading business data", type = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "data", description = "Reflect different business data based on contract transaction types（Here, in order to describe four situations, the four businesses are described together, but in reality, they do not exist simultaneously, only one exists）")
    }))
    private Map<String, Object> txData;
    @ApiModelProperty(description = "Contract execution results")
    private ContractResultDto contractResult;


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(String scriptSig) {
        this.scriptSig = scriptSig;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getConfirmCount() {
        return confirmCount;
    }

    public void setConfirmCount(Long confirmCount) {
        this.confirmCount = confirmCount;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<InputDto> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputDto> inputs) {
        this.inputs = inputs;
    }

    public List<OutputDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<OutputDto> outputs) {
        this.outputs = outputs;
    }

    public Map<String, Object> getTxData() {
        return txData;
    }

    public void setTxData(Map<String, Object> txData) {
        this.txData = txData;
    }

    public ContractResultDto getContractResult() {
        return contractResult;
    }

    public void setContractResult(ContractResultDto contractResult) {
        this.contractResult = contractResult;
    }
}
