package io.nuls.api.provider.contract.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 16:26
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class TransferToContractReq  extends BaseReq {

    private String address;

    private String toAddress;

    private BigInteger amount;

    private String password;

    private String remark;

}
