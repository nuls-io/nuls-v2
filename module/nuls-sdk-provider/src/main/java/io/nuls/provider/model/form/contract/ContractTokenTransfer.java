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
public class ContractTokenTransfer extends Base {

    @ApiModelProperty(description = "转出者账户地址", required = true)
    private String fromAddress;
    @ApiModelProperty(description = "转出者账户地址密码", required = true)
    private String password;
    @ApiModelProperty(description = "转入者账户地址", required = true)
    private String toAddress;
    @ApiModelProperty(description = "合约地址", required = true)
    private String contractAddress;
    @ApiModelProperty(description = "转出的token资产金额", required = true)
    private BigInteger amount;
    @ApiModelProperty(description = "备注", required = false)
    private String remark;

}
