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
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.model.bo.ContractMergedTransfer;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.vm.program.ProgramInvokeRegisterCmd;
import io.nuls.contract.vm.program.ProgramMethodArg;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.LongUtils;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.nuls.contract.util.ContractUtil.bigInteger2String;

/**
 * @author: PierreLuo
 */
@ApiModel
public class ContractResultDto {
    @ApiModelProperty(description = "合约执行是否成功")
    private boolean success;
    @ApiModelProperty(description = "执行失败信息")
    private String errorMessage;
    @ApiModelProperty(description = "合约地址")
    private String contractAddress;
    @ApiModelProperty(description = "合约执行结果")
    private String result;
    @ApiModelProperty(description = "GAS限制")
    private long gasLimit;
    @ApiModelProperty(description = "已使用GAS")
    private long gasUsed;
    @ApiModelProperty(description = "GAS单价")
    private long price;
    @ApiModelProperty(description = "交易总手续费")
    private String totalFee;
    @ApiModelProperty(description = "交易大小手续费")
    private String txSizeFee;
    @ApiModelProperty(description = "实际执行合约手续费")
    private String actualContractFee;
    @ApiModelProperty(description = "合约返回的手续费")
    private String refundFee;
    @ApiModelProperty(description = "调用者向合约地址转入的主网资产金额，没有此业务时则为0")
    private String value;
    @ApiModelProperty(description = "异常堆栈踪迹")
    private String stackTrace;
    @ApiModelProperty(description = "合约转账列表（从合约转出）", type = @TypeDescriptor(value = List.class, collectionElement = ContractMergedTransferDto.class))
    private List<ContractMergedTransferDto> transfers;
    @ApiModelProperty(description = "合约事件列表", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> events;
    @ApiModelProperty(description = "调式合约事件列表", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> debugEvents;
    @ApiModelProperty(description = "合约token转账列表", type = @TypeDescriptor(value = List.class, collectionElement = ContractTokenTransferDto.class))
    private List<ContractTokenTransferDto> tokenTransfers;
    @ApiModelProperty(description = "合约调用外部命令的调用记录列表", type = @TypeDescriptor(value = List.class, collectionElement = ContractInvokeRegisterCmdDto.class))
    private List<ContractInvokeRegisterCmdDto> invokeRegisterCmds;
    @ApiModelProperty(description = "合约生成交易的序列化字符串列表", type = @TypeDescriptor(value = List.class, collectionElement = String.class))
    private List<String> contractTxList;
    @ApiModelProperty(description = "备注")
    private String remark;

    public ContractResultDto() {
    }

    public ContractResultDto(int chainId, ContractResult result, ContractBaseTransaction tx) throws NulsException {
        ContractData contractData = (ContractData) tx.getTxDataObj();
        this.gasLimit = contractData.getGasLimit();
        this.gasUsed = result.getGasUsed();
        this.price = result.getPrice();
        BigInteger totalFee = tx.getFee();
        this.totalFee = bigInteger2String(totalFee);
        BigInteger actualContractFee = BigInteger.valueOf(LongUtils.mul(this.gasUsed, this.price));
        this.actualContractFee = bigInteger2String(actualContractFee);
        BigInteger contractFee = BigInteger.valueOf(LongUtils.mul(gasLimit, price));
        this.refundFee = bigInteger2String(contractFee.subtract(actualContractFee));
        this.txSizeFee = bigInteger2String(totalFee.subtract(contractFee));
        this.value = String.valueOf(result.getValue());
        this.contractAddress = AddressTool.getStringAddressByBytes(result.getContractAddress());
        this.result = result.getResult();
        this.success = result.isSuccess();
        this.errorMessage = result.getErrorMessage();
        this.stackTrace = result.getStackTrace();
        this.setMergedTransfers(result.getMergedTransferList());
        this.events = result.getEvents();
        this.debugEvents = result.getDebugEvents();
        this.remark = result.getRemark();
        this.invokeRegisterCmds = new LinkedList<>();
        this.contractTxList = new ArrayList<>();
        if (result.isSuccess()) {
            this.makeTokenTransfers(chainId, result.getEvents());
            this.makeInvokeRegisterCmds(result.getInvokeRegisterCmds());
        }
        this.contractTxList.addAll(result.getContractTransferTxStringList());
    }

    public ContractResultDto(int chainId, ContractResult result, long gasLimit) throws NulsException {
        this.gasLimit = gasLimit;
        this.gasUsed = result.getGasUsed();
        this.price = result.getPrice();
        BigInteger actualContractFee = BigInteger.valueOf(LongUtils.mul(this.gasUsed, this.price));
        this.actualContractFee = bigInteger2String(actualContractFee);
        BigInteger contractFee = BigInteger.valueOf(LongUtils.mul(gasLimit, price));
        this.refundFee = bigInteger2String(contractFee.subtract(actualContractFee));
        this.value = String.valueOf(result.getValue());
        this.contractAddress = AddressTool.getStringAddressByBytes(result.getContractAddress());
        this.result = result.getResult();
        this.success = result.isSuccess();
        this.errorMessage = result.getErrorMessage();
        this.stackTrace = result.getStackTrace();
        this.setMergedTransfers(result.getMergedTransferList());
        this.events = result.getEvents();
        this.debugEvents = result.getDebugEvents();
        this.remark = result.getRemark();
        this.invokeRegisterCmds = new LinkedList<>();
        this.contractTxList = new ArrayList<>();
        if (result.isSuccess()) {
            this.makeTokenTransfers(chainId, result.getEvents());
            this.makeInvokeRegisterCmds(result.getInvokeRegisterCmds());
        }
        this.contractTxList.addAll(result.getContractTransferTxStringList());
    }

    private void makeInvokeRegisterCmds(List<ProgramInvokeRegisterCmd> invokeRegisterCmds) {
        if(invokeRegisterCmds == null || invokeRegisterCmds.isEmpty()) {
            return;
        }
        for(ProgramInvokeRegisterCmd invokeRegisterCmd : invokeRegisterCmds) {
            if(CmdRegisterMode.NEW_TX.equals(invokeRegisterCmd.getCmdRegisterMode())) {
                contractTxList.add(invokeRegisterCmd.getProgramNewTx().getTxString());
            }
            this.invokeRegisterCmds.add(new ContractInvokeRegisterCmdDto(invokeRegisterCmd));
        }
    }

    public ContractResultDto(int chainId, ContractResult contractExecuteResult, ContractBaseTransaction tx, ContractTokenTransferInfoPo transferInfoPo) throws NulsException {
        this(chainId, contractExecuteResult, tx);
        if (transferInfoPo != null) {
            this.tokenTransfers = new ArrayList<>();
            this.tokenTransfers.add(new ContractTokenTransferDto(transferInfoPo));
        }
    }

    public List<ContractTokenTransferDto> getTokenTransfers() {
        return tokenTransfers == null ? new ArrayList<>() : tokenTransfers;
    }

    public void setTokenTransfers(List<ContractTokenTransferDto> tokenTransfers) {
        this.tokenTransfers = tokenTransfers;
    }

    private void makeTokenTransfers(int chainId, List<String> tokenTransferEvents) {
        List<ContractTokenTransferDto> result = new ArrayList<>();
        if (tokenTransferEvents != null && tokenTransferEvents.size() > 0) {
            ContractTokenTransferInfoPo po;
            for (String event : tokenTransferEvents) {
                po = ContractUtil.convertJsonToTokenTransferInfoPo(chainId, event);
                if (po != null) {
                    result.add(new ContractTokenTransferDto(po));
                }
            }
        }
        this.tokenTransfers = result;
    }

    public List<ContractMergedTransferDto> getTransfers() {
        return transfers == null ? new ArrayList<>() : transfers;
    }

    public void setMergedTransfers(List<ContractMergedTransfer> transfers) {
        List<ContractMergedTransferDto> list = new LinkedList<>();
        this.transfers = list;
        if (transfers == null || transfers.size() == 0) {
            return;
        }
        for (ContractMergedTransfer transfer : transfers) {
            list.add(new ContractMergedTransferDto(transfer));
        }
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
}
