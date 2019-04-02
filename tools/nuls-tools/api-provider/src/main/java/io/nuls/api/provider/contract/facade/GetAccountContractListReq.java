package io.nuls.api.provider.contract.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 16:55
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class GetAccountContractListReq extends BaseReq {

    private String address;


}
