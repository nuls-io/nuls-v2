package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 17:50
 * @Description:
 * 删除指定账户
 * remove account by address
 */
@Data
@AllArgsConstructor
public class RemoveAccountReq extends BaseReq {

    private String password;

    private String address;

}
