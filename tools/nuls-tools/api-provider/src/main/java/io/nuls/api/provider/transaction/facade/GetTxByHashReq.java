package io.nuls.api.provider.transaction.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:01
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class GetTxByHashReq extends BaseReq {

    private String txHash;

}
