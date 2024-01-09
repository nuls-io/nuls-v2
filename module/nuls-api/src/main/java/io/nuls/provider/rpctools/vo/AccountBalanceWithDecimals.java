package io.nuls.provider.rpctools.vo;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * @author: PierreLuo
 * @date: 2022/3/11
 */
@ApiModel
public class AccountBalanceWithDecimals {
    @ApiModelProperty(description = "Asset ChainID")
    private int assetChainId;
    @ApiModelProperty(description = "assetID")
    private int assetId;
    @ApiModelProperty(description = "Contract address")
    private String contractAddress;
    @ApiModelProperty(description = "Total balance")
    private String totalBalance;
    @ApiModelProperty(description = "Available balance")
    private String balance;
    @ApiModelProperty(description = "Time lock amount")
    private String timeLock;
    @ApiModelProperty(description = " Consensus locking amount")
    private String consensusLock;
    @ApiModelProperty(description = "Total locked balance")
    private String freeze;
    @ApiModelProperty(description = "Account assetsnoncevalue")
    private String nonce;
    @ApiModelProperty(description = "1：Confirmednoncevalue,0：unacknowledgednoncevalue")
    private int nonceType;
    @ApiModelProperty(description = "Asset accuracy")
    private int decimals;

    public AccountBalance toAccountBalance() {
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAssetChainId(assetChainId);
        accountBalance.setAssetId(assetId);
        accountBalance.setContractAddress(contractAddress);
        accountBalance.setTotalBalance(totalBalance);
        accountBalance.setBalance(balance);
        accountBalance.setTimeLock(timeLock);
        accountBalance.setConsensusLock(consensusLock);
        accountBalance.setFreeze(freeze);
        accountBalance.setNonce(nonce);
        accountBalance.setNonceType(nonceType);
        return accountBalance;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(String totalBalance) {
        this.totalBalance = totalBalance;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getTimeLock() {
        return timeLock;
    }

    public void setTimeLock(String timeLock) {
        this.timeLock = timeLock;
    }

    public String getConsensusLock() {
        return consensusLock;
    }

    public void setConsensusLock(String consensusLock) {
        this.consensusLock = consensusLock;
    }

    public String getFreeze() {
        return freeze;
    }

    public void setFreeze(String freeze) {
        this.freeze = freeze;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public int getNonceType() {
        return nonceType;
    }

    public void setNonceType(int nonceType) {
        this.nonceType = nonceType;
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

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}
