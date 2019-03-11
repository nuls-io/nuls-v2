package io.nuls.api.provider.transaction.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:25
 * @Description:
 * 通过别名转账
 * transfer by account alias
 */
@Data
@AllArgsConstructor
public class TransferByAliasReq extends BaseReq {

    /**
     * 接收地址
     */
    private String address;

    /**
     * 账户密码
     *
     */
    private String password;

    /**
     * 转账金额
     */
    private BigInteger amount;

    /**
     * 发出资产账户别名
     */
    private String alias;

    /**
     * 备注
     */
    private String remark;

}
