package io.nuls.provider.model.dto;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.provider.rpctools.vo.AccountBalance;

/**
 * @author: PierreLuo
 * @date: 2019-06-30
 */
@ApiModel
public class AccountBalanceDto {

    @ApiModelProperty(description = "总余额")
    private String total;
    @ApiModelProperty(description = "锁定金额")
    private String freeze;
    @ApiModelProperty(description = "可用余额")
    private String available;
    @ApiModelProperty(description = "时间锁定金额")
    private String timeLock;
    @ApiModelProperty(description = " 共识锁定金额")
    private String consensusLock;
    @ApiModelProperty(description = "账户资产nonce值")
    private String nonce;
    @ApiModelProperty(description = "1：已确认的nonce值,0：未确认的nonce值")
    private int nonceType;

    public AccountBalanceDto() {

    }

    public AccountBalanceDto(AccountBalance info) {
        this.total = info.getTotalBalance();
        this.freeze = info.getFreeze();
        this.available = info.getBalance();
        this.timeLock = info.getTimeLock();
        this.consensusLock = info.getConsensusLock();
        this.nonce = info.getNonce();
        this.nonceType = info.getNonceType();
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getFreeze() {
        return freeze;
    }

    public void setFreeze(String freeze) {
        this.freeze = freeze;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
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
}
