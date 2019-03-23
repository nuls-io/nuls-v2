package io.nuls.api.provider.contract.facade;

import io.nuls.api.provider.BaseReq;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 16:39
 * @Description: 功能描述
 */
@Data
public class TokenTransferReq extends BaseReq {


    private String address;
    private String toAddress;
    private String contractAddress;
    private long gasLimit;
    private long price;
    private String password;
    private String amount;
    private String remark;


}
