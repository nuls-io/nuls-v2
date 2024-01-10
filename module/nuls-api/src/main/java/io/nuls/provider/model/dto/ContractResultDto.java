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
public class ContractResultDto {

    @ApiModelProperty(description = "Whether the contract execution was successful")
    private boolean success;
    @ApiModelProperty(description = "Execution failure information")
    private String errorMessage;
    @ApiModelProperty(description = "Contract address")
    private String contractAddress;
    @ApiModelProperty(description = "Contract execution results")
    private String result;
    @ApiModelProperty(description = "GASlimit")
    private long gasLimit;
    @ApiModelProperty(description = "UsedGAS")
    private long gasUsed;
    @ApiModelProperty(description = "GASunit price")
    private long price;
    @ApiModelProperty(description = "Total transaction fees")
    private String totalFee;
    @ApiModelProperty(description = "Transaction size handling fee")
    private String txSizeFee;
    @ApiModelProperty(description = "Actual contract execution fee")
    private String actualContractFee;
    @ApiModelProperty(description = "Contract return handling fee")
    private String refundFee;
    @ApiModelProperty(description = "The amount of main network assets transferred by the caller to the contracted address. If there is no such service, it is:0")
    private String value;
    @ApiModelProperty(description = "Abnormal stack trace")
    private String stackTrace;
    @ApiModelProperty(description = "Contract transfer list（Transfer of main assets from contract）", type = @TypeDescriptor(value = List.class, collectionElement = ContractMergedTransferDto.class))
    private List<ContractMergedTransferDto> transfers;
    @ApiModelProperty(description = "Contract transfer list（Transferring other assets from the contract）", type = @TypeDescriptor(value = List.class, collectionElement = ContractMultyAssetMergedTransferDto.class))
    private List<ContractMultyAssetMergedTransferDto> multyAssetTransfers;
    @ApiModelProperty(description = "Contract Event List", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> events;
    @ApiModelProperty(description = "Modal Contract Event List", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> debugEvents;
    @ApiModelProperty(description = "contracttokenTransfer List", type = @TypeDescriptor(value = List.class, collectionElement = ContractTokenTransferDto.class))
    private List<ContractTokenTransferDto> tokenTransfers;
    @ApiModelProperty(description = "contractNRC721-tokenTransfer List", type = @TypeDescriptor(value = List.class, collectionElement = ContractToken721TransferDto.class))
    private List<ContractToken721TransferDto> token721Transfers;
    @ApiModelProperty(description = "List of call records for contract calls to external commands", type = @TypeDescriptor(value = List.class, collectionElement = ContractInvokeRegisterCmdDto.class))
    private List<ContractInvokeRegisterCmdDto> invokeRegisterCmds;
    @ApiModelProperty(description = "Serialized string list for contract generation transactions", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> contractTxList;
    @ApiModelProperty(description = "Remarks")
    private String remark;
    @ApiModelProperty(description = "List of contracts created internally", type = @TypeDescriptor(value = List.class, collectionElement = ContractInternalCreateDto.class))
    private List<ContractInternalCreateDto> internalCreates;

    public List<ContractMultyAssetMergedTransferDto> getMultyAssetTransfers() {
        return multyAssetTransfers;
    }

    public void setMultyAssetTransfers(List<ContractMultyAssetMergedTransferDto> multyAssetTransfers) {
        this.multyAssetTransfers = multyAssetTransfers;
    }

    public List<ContractToken721TransferDto> getToken721Transfers() {
        return token721Transfers;
    }

    public void setToken721Transfers(List<ContractToken721TransferDto> token721Transfers) {
        this.token721Transfers = token721Transfers;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(String totalFee) {
        this.totalFee = totalFee;
    }

    public String getTxSizeFee() {
        return txSizeFee;
    }

    public void setTxSizeFee(String txSizeFee) {
        this.txSizeFee = txSizeFee;
    }

    public String getActualContractFee() {
        return actualContractFee;
    }

    public void setActualContractFee(String actualContractFee) {
        this.actualContractFee = actualContractFee;
    }

    public String getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(String refundFee) {
        this.refundFee = refundFee;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<ContractMergedTransferDto> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<ContractMergedTransferDto> transfers) {
        this.transfers = transfers;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public List<String> getDebugEvents() {
        return debugEvents;
    }

    public void setDebugEvents(List<String> debugEvents) {
        this.debugEvents = debugEvents;
    }

    public List<ContractTokenTransferDto> getTokenTransfers() {
        return tokenTransfers;
    }

    public void setTokenTransfers(List<ContractTokenTransferDto> tokenTransfers) {
        this.tokenTransfers = tokenTransfers;
    }

    public List<ContractInvokeRegisterCmdDto> getInvokeRegisterCmds() {
        return invokeRegisterCmds;
    }

    public void setInvokeRegisterCmds(List<ContractInvokeRegisterCmdDto> invokeRegisterCmds) {
        this.invokeRegisterCmds = invokeRegisterCmds;
    }

    public List<String> getContractTxList() {
        return contractTxList;
    }

    public void setContractTxList(List<String> contractTxList) {
        this.contractTxList = contractTxList;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<ContractInternalCreateDto> getInternalCreates() {
        return internalCreates;
    }

    public void setInternalCreates(List<ContractInternalCreateDto> internalCreates) {
        this.internalCreates = internalCreates;
    }
}
