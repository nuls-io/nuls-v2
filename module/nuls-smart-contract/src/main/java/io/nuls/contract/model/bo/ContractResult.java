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

package io.nuls.contract.model.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.basic.AddressTool;
import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.vm.program.ProgramAccount;
import io.nuls.contract.vm.program.ProgramInternalCreate;
import io.nuls.contract.vm.program.ProgramInvokeRegisterCmd;
import io.nuls.contract.vm.program.ProgramTransfer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContractResult {
    private transient ContractWrapperTransaction tx;
    private String hash;
    private long txTime;
    private int txOrder;
    private long blockHeight;
    /**
     * 交易创建者
     */
    private byte[] sender;
    /**
     * 合约地址
     */
    private byte[] contractAddress;
    /**
     * 合约执行结果
     */
    private String result;
    /**
     * 已使用Gas
     */
    private long gasUsed;
    /**
     * 单价
     */
    private long price;
    /**
     * 调用者向合约转入的资金
     */
    private long value;
    private boolean revert;
    private boolean error;
    private String errorMessage;
    private String stackTrace;
    private boolean acceptDirectTransfer;
    // token类型, 0 - 非token, 1 - NRC20, 2 - NRC721
    private int tokenType;
    private boolean isNrc20;
    private String tokenName;
    private String tokenSymbol;
    private int tokenDecimals;
    private BigInteger tokenTotalSupply;
    /**
     * 合约转账(从合约转出)交易
     */
    private List<ProgramTransfer> transfers = new ArrayList<>();
    private List<ContractMergedTransfer> mergedTransferList = new ArrayList<>();
    private transient List<ContractTransferTransaction> contractTransferList = new ArrayList<>();
    private List<String> contractTransferTxStringList = new ArrayList<>();
    /**
     * 消息事件
     */
    private List<String> events = new ArrayList<>();
    private List<String> debugEvents = new ArrayList<>();
    private List<ProgramInvokeRegisterCmd> invokeRegisterCmds = new ArrayList<>();
    private String remark;
    private boolean isTerminated;
    private Set<String> contractAddressInnerCallSet;
    private List<ContractMultyAssetMergedTransfer> mergerdMultyAssetTransferList = new ArrayList<>();
    // add by pierre at 2022/6/2 p14
    private List<ContractInternalCreate> internalCreates = new ArrayList<>();
    private transient List<ProgramInternalCreate> programInternalCreates = new ArrayList<>();

    private transient Object txTrack;
    private transient Map<String, ProgramAccount> accounts;
    private transient List<Object> orderedInnerTxs = new ArrayList<>();

    public List<ContractInternalCreate> getInternalCreates() {
        return internalCreates;
    }

    public void setInternalCreates(List<ContractInternalCreate> internalCreates) {
        this.internalCreates = internalCreates;
    }

    public List<ProgramInternalCreate> getProgramInternalCreates() {
        return programInternalCreates;
    }

    public void setProgramInternalCreates(List<ProgramInternalCreate> programInternalCreates) {
        this.programInternalCreates = programInternalCreates;
    }

    public Map<String, ProgramAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, ProgramAccount> accounts) {
        this.accounts = accounts;
    }

    public boolean isSuccess() {
        return !error && !revert;
    }

    @JsonIgnore
    public Object getTxTrack() {
        return txTrack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true;}
        if (!(o instanceof ContractResult)) { return false;}

        ContractResult result = (ContractResult) o;

        if (getHash() != null ? !getHash().equals(result.getHash()) : result.getHash() != null) { return false;}

        return true;
    }

    @Override
    public int hashCode() {
        int result = getHash() != null ? getHash().hashCode() : 0;
        return result;
    }

    private static ContractResult genFailed(ContractData contractData) {
        ContractResult contractResult = new ContractResult();
        contractResult.setContractAddress(contractData.getContractAddress());
        contractResult.setGasUsed(contractData.getGasLimit());
        contractResult.setPrice(contractData.getPrice());
        contractResult.setSender(contractData.getSender());
        contractResult.setValue(contractData.getValue().longValue());
        contractResult.setError(true);
        contractResult.setRevert(true);
        return contractResult;
    }

    public static ContractResult genFailed(ContractData contractData, String msg) {
        ContractResult result = genFailed(contractData);
        result.setErrorMessage(msg);
        return result;
    }

    @Override
    public String toString() {
        return "ContractResult{" +
                "success='" + isSuccess() + '\'' +
                ", hash='" + hash + '\'' +
                ", txOrder=" + txOrder +
                ", txTime=" + txTime +
                ", sender=" + AddressTool.getStringAddressByBytes(sender) +
                ", contractAddress=" + AddressTool.getStringAddressByBytes(contractAddress) +
                ", result='" + result + '\'' +
                ", gasUsed=" + gasUsed +
                ", price=" + price +
                ", value=" + value +
                ", revert=" + revert +
                ", error=" + error +
                ", errorMessage='" + errorMessage + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", acceptDirectTransfer=" + acceptDirectTransfer +
                ", isNrc20=" + isNrc20 +
                ", transfersSize=" + (transfers != null ? transfers.size() : 0) +
                ", mergedTransferList=" + (mergedTransferList != null ? mergedTransferList.size() : 0) +
                ", mergerdMultyAssetTransferList=" + (mergerdMultyAssetTransferList != null ? mergerdMultyAssetTransferList.size() : 0) +
                ", contractTransferList=" + (contractTransferList != null ? contractTransferList.size() : 0) +
                ", invokeRegisterCmds=" + (invokeRegisterCmds != null ? invokeRegisterCmds.size() : 0) +
                ", events=" + events +
                ", remark='" + remark + '\'' +
                ", isTerminated=" + isTerminated +
                ", contractAddressInnerCallSet=" + (contractAddressInnerCallSet != null ? contractAddressInnerCallSet.size() : 0) +
                '}';
    }

    public ContractWrapperTransaction getTx() {
        return tx;
    }

    public void setTx(ContractWrapperTransaction tx) {
        this.tx = tx;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getTxTime() {
        return txTime;
    }

    public void setTxTime(long txTime) {
        this.txTime = txTime;
    }

    public int getTxOrder() {
        return txOrder;
    }

    public void setTxOrder(int txOrder) {
        this.txOrder = txOrder;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public byte[] getSender() {
        return sender;
    }

    public void setSender(byte[] sender) {
        this.sender = sender;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public boolean isRevert() {
        return revert;
    }

    public void setRevert(boolean revert) {
        this.revert = revert;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public boolean isAcceptDirectTransfer() {
        return acceptDirectTransfer;
    }

    public void setAcceptDirectTransfer(boolean acceptDirectTransfer) {
        this.acceptDirectTransfer = acceptDirectTransfer;
    }

    public int getTokenType() {
        return tokenType;
    }

    public void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isNrc20() {
        return isNrc20;
    }

    public void setNrc20(boolean nrc20) {
        isNrc20 = nrc20;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public int getTokenDecimals() {
        return tokenDecimals;
    }

    public void setTokenDecimals(int tokenDecimals) {
        this.tokenDecimals = tokenDecimals;
    }

    public BigInteger getTokenTotalSupply() {
        return tokenTotalSupply;
    }

    public void setTokenTotalSupply(BigInteger tokenTotalSupply) {
        this.tokenTotalSupply = tokenTotalSupply;
    }

    public List<ProgramTransfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<ProgramTransfer> transfers) {
        this.transfers = transfers;
    }

    public List<ContractMergedTransfer> getMergedTransferList() {
        return mergedTransferList;
    }

    public void setMergedTransferList(List<ContractMergedTransfer> mergedTransferList) {
        this.mergedTransferList = mergedTransferList;
    }

    public List<ContractMultyAssetMergedTransfer> getMergerdMultyAssetTransferList() {
        return mergerdMultyAssetTransferList;
    }

    public void setMergerdMultyAssetTransferList(List<ContractMultyAssetMergedTransfer> mergerdMultyAssetTransferList) {
        this.mergerdMultyAssetTransferList = mergerdMultyAssetTransferList;
    }

    public List<ContractTransferTransaction> getContractTransferList() {
        return contractTransferList;
    }

    public void setContractTransferList(List<ContractTransferTransaction> contractTransferList) {
        this.contractTransferList = contractTransferList;
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

    public boolean isTerminated() {
        return isTerminated;
    }

    public void setTerminated(boolean terminated) {
        isTerminated = terminated;
    }

    public Set<String> getContractAddressInnerCallSet() {
        return contractAddressInnerCallSet;
    }

    public void setContractAddressInnerCallSet(Set<String> contractAddressInnerCallSet) {
        this.contractAddressInnerCallSet = contractAddressInnerCallSet;
    }

    public void setTxTrack(Object txTrack) {
        this.txTrack = txTrack;
    }

    public List<ProgramInvokeRegisterCmd> getInvokeRegisterCmds() {
        return invokeRegisterCmds;
    }

    public void setInvokeRegisterCmds(List<ProgramInvokeRegisterCmd> invokeRegisterCmds) {
        this.invokeRegisterCmds = invokeRegisterCmds;
    }

    public List<Object> getOrderedInnerTxs() {
        return orderedInnerTxs;
    }

    public void setOrderedInnerTxs(List<Object> orderedInnerTxs) {
        this.orderedInnerTxs = orderedInnerTxs;
    }

    public List<String> getContractTransferTxStringList() {
        return contractTransferTxStringList;
    }

    public void setContractTransferTxStringList(List<String> contractTransferTxStringList) {
        this.contractTransferTxStringList = contractTransferTxStringList;
    }
}
