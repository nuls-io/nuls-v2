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
 */
@ApiModel
public class ContractMultyAssetMergedTransferDto {
    @ApiModelProperty(description = "Contract generation transaction：Contract transfer transactionhash")
    private String txHash;
    @ApiModelProperty(description = "Transferred contract address")
    private String from;
    @ApiModelProperty(description = "Transfer amount")
    private String value;
    @ApiModelProperty(description = "Transfer amount asset chainID")
    private int assetChainId;
    @ApiModelProperty(description = "Transfer amount assetsID")
    private int assetId;
    @ApiModelProperty(description = "Transferred address list", type = @TypeDescriptor(value = List.class, collectionElement = MultyAssetOutputDto.class))
    private List<MultyAssetOutputDto> outputs;
    @ApiModelProperty(description = "Call contract transactionshash（Source transactionhashContract trading is derived from calling contract trading）")
    private String orginTxHash;

    public ContractMultyAssetMergedTransferDto() {
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

    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public List<MultyAssetOutputDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<MultyAssetOutputDto> outputs) {
        this.outputs = outputs;
    }

    public String getOrginTxHash() {
        return orginTxHash;
    }

    public void setOrginTxHash(String orginTxHash) {
        this.orginTxHash = orginTxHash;
    }
}
