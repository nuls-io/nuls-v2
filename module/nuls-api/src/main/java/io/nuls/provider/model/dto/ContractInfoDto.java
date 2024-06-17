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
import io.nuls.core.rpc.model.TypeDescriptor;

import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2018/8/15
 */
@ApiModel
public class ContractInfoDto {
    @ApiModelProperty(description = "Transactions for publishing contractshash")
    private String createTxHash;
    @ApiModelProperty(description = "Contract address")
    private String address;
    @ApiModelProperty(description = "Contract Creator Address")
    private String creater;
    @ApiModelProperty(description = "Contract alias")
    private String alias;
    @ApiModelProperty(description = "Contract creation time（unit：second）")
    private long createTime;
    @ApiModelProperty(description = "Block height during contract creation")
    private long blockHeight;
    @ApiModelProperty(description = "Do you accept direct transfer")
    private boolean isDirectPayable;
    @ApiModelProperty(description = "Do you accept direct transfers of other assets")
    private boolean directPayableByOtherAsset;
    @ApiModelProperty(description = "tokentype, 0 - wrongtoken, 1 - NRC20, 2 - NRC721")
    private int tokenType;
    @ApiModelProperty(description = "Is itNRC20contract")
    private boolean isNrc20;
    @ApiModelProperty(description = "NRC20-tokenname")
    private String nrc20TokenName;
    @ApiModelProperty(description = "NRC20-tokensymbol")
    private String nrc20TokenSymbol;
    @ApiModelProperty(description = "NRC20-tokenSupported Decimal Places")
    private long decimals;
    @ApiModelProperty(description = "NRC20-tokenTotal issuance amount")
    private String totalSupply;
    @ApiModelProperty(description = "Contract status（not_found, normal, stop）")
    private String status;
    @ApiModelProperty(description = "List of Contract Methods", type = @TypeDescriptor(value = List.class, collectionElement = ProgramMethod.class))
    private List<ProgramMethod> method;

    public ContractInfoDto() {

    }

    public int getTokenType() {
        return tokenType;
    }

    public void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    public String getCreateTxHash() {
        return createTxHash;
    }

    public void setCreateTxHash(String createTxHash) {
        this.createTxHash = createTxHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreater() {
        return creater;
    }

    public void setCreater(String creater) {
        this.creater = creater;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public boolean isDirectPayable() {
        return isDirectPayable;
    }

    public void setDirectPayable(boolean directPayable) {
        isDirectPayable = directPayable;
    }

    public boolean isDirectPayableByOtherAsset() {
        return directPayableByOtherAsset;
    }

    public void setDirectPayableByOtherAsset(boolean directPayableByOtherAsset) {
        this.directPayableByOtherAsset = directPayableByOtherAsset;
    }

    public boolean isNrc20() {
        return isNrc20;
    }

    public void setNrc20(boolean nrc20) {
        isNrc20 = nrc20;
    }

    public String getNrc20TokenName() {
        return nrc20TokenName;
    }

    public void setNrc20TokenName(String nrc20TokenName) {
        this.nrc20TokenName = nrc20TokenName;
    }

    public String getNrc20TokenSymbol() {
        return nrc20TokenSymbol;
    }

    public void setNrc20TokenSymbol(String nrc20TokenSymbol) {
        this.nrc20TokenSymbol = nrc20TokenSymbol;
    }

    public long getDecimals() {
        return decimals;
    }

    public void setDecimals(long decimals) {
        this.decimals = decimals;
    }

    public String getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(String totalSupply) {
        this.totalSupply = totalSupply;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ProgramMethod> getMethod() {
        return method;
    }

    public void setMethod(List<ProgramMethod> method) {
        this.method = method;
    }
}
