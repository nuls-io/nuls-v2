package io.nuls.api.provider.contract.facade;

import io.nuls.api.provider.BaseReq;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:33
 * @Description: 功能描述
 */
@Data
public class CreateContractReq extends BaseReq {

    private String sender;

    private long gasLimit;

    private long price;

    private String password;

    private String remark;

    private String contractCode;

    private Object[] args;


}
