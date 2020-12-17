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
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.provider.model.form.Base;
import io.nuls.v2.util.ContractUtil;

/**
 * @author: PierreLuo
 * @date: 2019-07-05
 */
@ApiModel(description = "验证调用智能合约表单数据")
public class ContractValidateCall extends Base {

    @ApiModelProperty(description = "交易创建者", required = true)
    private String sender;
    @ApiModelProperty(description = "调用者向合约地址转入的主网资产金额，没有此业务时填0", required = false)
    private long value;
    @ApiModelProperty(description = "调用者向合约地址转入的其他资产金额，没有此业务时填空，规则: [[<value>,<assetChainId>,<assetId>]]", required = false)
    private String[][] multyAssetValues;
    @ApiModelProperty(description = "最大gas消耗", required = true)
    private long gasLimit;
    @ApiModelProperty(description = "执行合约单价", required = true)
    private long price;
    @ApiModelProperty(description = "智能合约地址", required = true)
    private String contractAddress;
    @ApiModelProperty(description = "方法名称", required = true)
    private String methodName;
    @ApiModelProperty(description = "方法描述，若合约内方法没有重载，则此参数可以为空", required = false)
    private String methodDesc;
    @ApiModelProperty(description = "参数列表", required = false)
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

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
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
