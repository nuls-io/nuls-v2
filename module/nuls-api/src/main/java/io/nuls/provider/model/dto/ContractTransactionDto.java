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

    @ApiModelProperty(description = "交易hash")
    private String hash;
    @ApiModelProperty(description = "交易类型")
    private Integer type;
    @ApiModelProperty(description = "交易时间")
    private Long time;
    @ApiModelProperty(description = "区块高度")
    private Long blockHeight;
    @ApiModelProperty(description = "交易手续费")
    private String fee;
    @ApiModelProperty(description = "交易金额")
    private String value;
    @ApiModelProperty(description = "备注")
    private String remark;
    @ApiModelProperty(description = "签名信息")
    private String scriptSig;
    @ApiModelProperty(description = "交易状态（0 - 确认中，1 - 已确认）")
    private Integer status;
    @ApiModelProperty(description = "交易确认次数")
    private Long confirmCount;
    @ApiModelProperty(description = "交易大小")
    private int size;
    @ApiModelProperty(description = "交易输入集合", type = @TypeDescriptor(value = List.class, collectionElement = InputDto.class))
    private List<InputDto> inputs;
    @ApiModelProperty(description = "交易输出集合", type = @TypeDescriptor(value = List.class, collectionElement = OutputDto.class))
    private List<OutputDto> outputs;
    @ApiModelProperty(description = "合约交易业务数据", type = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "data", description = "根据合约交易类型反映不同的业务数据（这里为了描述四种情况，四种业务放在一起描述，实际上不同时存在，只存在一个）")
    }))
    private Map<String, Object> txData;
    @ApiModelProperty(description = "合约执行结果")
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
