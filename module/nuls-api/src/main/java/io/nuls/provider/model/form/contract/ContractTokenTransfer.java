package io.nuls.provider.model.form.contract;


import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.provider.model.form.Base;

import java.math.BigInteger;

@ApiModel(description = "tokenTransfer transaction")
public class ContractTokenTransfer extends Base {

    @ApiModelProperty(description = "Transferor's account address", required = true)
    private String fromAddress;
    @ApiModelProperty(description = "Transferor account address password", required = true)
    private String password;
    @ApiModelProperty(description = "Transferee's account address", required = true)
    private String toAddress;
    @ApiModelProperty(description = "Contract address", required = true)
    private String contractAddress;
    @ApiModelProperty(description = "Transferred outtokenAsset amount", required = true)
    private BigInteger amount;
    @ApiModelProperty(description = "Remarks", required = false)
    private String remark;

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
