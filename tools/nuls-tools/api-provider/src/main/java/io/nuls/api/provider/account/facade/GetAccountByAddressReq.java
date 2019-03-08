package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 13:42
 * @Description:
 * 根据地址获取账户信息
 * get account info by address
 */
@Data
@AllArgsConstructor
public class GetAccountByAddressReq extends BaseReq {

    private String address;

}
