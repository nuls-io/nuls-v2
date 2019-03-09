package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 14:39
 * @Description:
 * 设置账户别名
 * set account alias
 */
@Data
@AllArgsConstructor
public class SetAccountAliasReq extends BaseReq {

    private String password;

    private String address;

    private String alias;

}
