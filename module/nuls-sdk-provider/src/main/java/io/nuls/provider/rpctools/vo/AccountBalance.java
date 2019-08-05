package io.nuls.provider.rpctools.vo;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:33
 * @Description: 账户余额和nonce
 */
@Data
@NoArgsConstructor
@ApiModel
public class AccountBalance {

    @ApiModelProperty(description = "总余额")
    private String totalBalance;
    @ApiModelProperty(description = "可用余额")
    private String balance;
    @ApiModelProperty(description = "时间锁定金额")
    private String timeLock;
    @ApiModelProperty(description = " 共识锁定金额")
    private String consensusLock;
    @ApiModelProperty(description = "总锁定余额")
    private String freeze;
    @ApiModelProperty(description = "账户资产nonce值")
    private String nonce;
    @ApiModelProperty(description = "1：已确认的nonce值,0：未确认的nonce值")
    private int nonceType;

}
