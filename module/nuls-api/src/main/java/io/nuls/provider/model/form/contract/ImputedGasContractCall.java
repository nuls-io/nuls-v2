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
package io.nuls.provider.model.form.contract;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.provider.model.form.Base;
import io.nuls.v2.util.ContractUtil;

import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2018/7/18
 */
@ApiModel(description = "Estimate the number of calls to the smart contractGasConsumed form data")
public class ImputedGasContractCall extends Base {

    @ApiModelProperty(description = "Transaction Creator", required = true)
    private String sender;
    @ApiModelProperty(description = "The amount of main network assets transferred by the caller to the contracted address, to be filled in when this service is not available0", required = false)
    private BigInteger value;
    @ApiModelProperty(description = "The amount of other assets transferred by the caller to the contract address, fill in the blank if there is no such business, rule: [[\\<value\\>,\\<assetChainId\\>,\\<assetId\\>]]", required = false)
    private String[][] multyAssetValues;
    @ApiModelProperty(description = "Smart contract address", required = true)
    private String contractAddress;
    @ApiModelProperty(description = "Method Name", required = true)
    private String methodName;
    @ApiModelProperty(description = "Method description, if the method in the contract is not overloaded, this parameter can be empty", required = false)
    private String methodDesc;
    @ApiModelProperty(description = "parameter list", required = false)
    private Object[] args;

    public String[][] getMultyAssetValues() {
        return multyAssetValues;
    }

    public void setMultyAssetValues(String[][] multyAssetValues) {
        this.multyAssetValues = multyAssetValues;
    }

    public String[][] getArgs(String[] types) {
        return ContractUtil.twoDimensionalArray(args, types);
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
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

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }
}
