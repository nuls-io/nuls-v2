package io.nuls.provider.model.form.contract;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.provider.model.form.Base;

import java.math.BigInteger;

@ApiModel(description = "token转账交易")
public class ContractTokenTransferOffline extends Base {

    @ApiModelProperty(description = "转出者账户地址", required = true)
    private String fromAddress;
    @ApiModelProperty(description = "转出者账户余额")
    private BigInteger senderBalance;
    @ApiModelProperty(description = "转出者账户nonce值")
    private String nonce;
    @ApiModelProperty(description = "转入者账户地址", required = true)
    private String toAddress;
    @ApiModelProperty(description = "合约地址", required = true)
    private String contractAddress;
    @ApiModelProperty(description = "GAS限制")
    private long gasLimit;
    @ApiModelProperty(description = "转出的token资产金额", required = true)
    private BigInteger amount;
    @ApiModelProperty(description = "备注", required = false)
    private String remark;

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public BigInteger getSenderBalance() {
        return senderBalance;
    }

    public void setSenderBalance(BigInteger senderBalance) {
        this.senderBalance = senderBalance;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
