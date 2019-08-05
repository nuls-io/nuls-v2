package io.nuls.provider.model.form.contract;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.provider.model.form.Base;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
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

}
