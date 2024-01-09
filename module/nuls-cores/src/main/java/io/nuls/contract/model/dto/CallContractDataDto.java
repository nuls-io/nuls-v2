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
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

import static io.nuls.contract.util.ContractUtil.bigInteger2String;

/**
 * @author: PierreLuo
 */
@ApiModel
public class CallContractDataDto {
    @ApiModelProperty(description = "Transaction creator address")
    private String sender;
    @ApiModelProperty(description = "Contract address")
    private String contractAddress;
    @ApiModelProperty(description = "The amount of main network assets transferred by the caller to the contracted address, to be filled in when this service is not availableBigInteger.ZERO")
    private String value;
    @ApiModelProperty(description = "GASlimit")
    private long gasLimit;
    @ApiModelProperty(description = "GASunit price")
    private long price;
    @ApiModelProperty(description = "Contract method")
    private String methodName;
    @ApiModelProperty(description = "Contract method description, if the method in the contract is not overloaded, this parameter can be empty")
    private String methodDesc;
    @ApiModelProperty(description = "parameter list")
    private String[][] args;

    public CallContractDataDto(ContractData call) {
        this.sender = AddressTool.getStringAddressByBytes(call.getSender());
        this.contractAddress = AddressTool.getStringAddressByBytes(call.getContractAddress());
        this.value = bigInteger2String(call.getValue());
        this.gasLimit = call.getGasLimit();
        this.price = call.getPrice();
        this.methodName = call.getMethodName();
        this.methodDesc = call.getMethodDesc();
        this.args = call.getArgs();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public String[][] getArgs() {
        return args;
    }

    public void setArgs(String[][] args) {
        this.args = args;
    }
}
