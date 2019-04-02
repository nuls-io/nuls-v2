package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 17:50
 * @Description:
 * 查询账户私钥
 * get account private key
 */
@Data
@AllArgsConstructor
public class GetAccountPrivateKeyByAddressReq extends BaseReq {

    private String password;

    private String address;

}
