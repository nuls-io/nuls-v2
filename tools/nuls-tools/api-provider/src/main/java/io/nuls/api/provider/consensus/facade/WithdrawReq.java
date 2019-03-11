package io.nuls.api.provider.consensus.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:57
 * @Description:
 * 退出共识
 */
@Data
@AllArgsConstructor
public class WithdrawReq extends BaseReq {

    String address;

    String txHash;

    String password;

}
