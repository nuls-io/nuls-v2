package io.nuls.provider.model.dto;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.provider.rpctools.vo.AccountBalance;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: PierreLuo
 * @date: 2019-06-30
 */
@Data
@NoArgsConstructor
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

    public AccountBalanceDto(AccountBalance info) {
        this.total = info.getTotalBalance();
        this.freeze = info.getFreeze();
        this.available = info.getBalance();
        this.timeLock = info.getTimeLock();
        this.consensusLock = info.getConsensusLock();
        this.nonce = info.getNonce();
        this.nonceType = info.getNonceType();
    }
}
