package io.nuls.api.provider.transaction.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 10:47
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class GetConfirmedTxByHashReq extends BaseReq {

    private String txHash;

}
