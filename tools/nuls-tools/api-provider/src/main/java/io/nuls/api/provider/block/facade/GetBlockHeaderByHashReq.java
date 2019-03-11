package io.nuls.api.provider.block.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:33
 * @Description:
 * 通过hash获取区块头
 * get block header by hash
 */
@Data
@AllArgsConstructor
public class GetBlockHeaderByHashReq extends BaseReq {

    private String hash;

}
