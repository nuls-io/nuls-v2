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


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * @author: PierreLuo
 */
@ApiModel
public class ContractToken1155TransferDto {
    @ApiModelProperty(description = "合约地址")
    private String contractAddress;
    @ApiModelProperty(description = "操作方")
    private String operator;
    @ApiModelProperty(description = "付款方")
    private String from;
    @ApiModelProperty(description = "收款方")
    private String to;
    @ApiModelProperty(description = "tokenId")
    private String[] ids;
    @ApiModelProperty(description = "转账数量")
    private String[] values;
    @ApiModelProperty(description = "token名称")
    private String name;
    @ApiModelProperty(description = "token符号")
    private String symbol;

    public ContractToken1155TransferDto(ContractTokenTransferInfo info) {
        this.contractAddress = info.getContractAddress();
        this.operator = info.getOperator();
        this.from = info.getFrom();
        this.to = info.getTo();
        this.name = info.getName();
        this.symbol = info.getSymbol();
        if (info.getIds() != null) {
            this.ids = info.getIds();
            this.values = info.getValues();
        } else {
            this.ids = new String[]{info.getId().toString()};
            this.values = new String[]{info.getValue().toString()};
        }
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
