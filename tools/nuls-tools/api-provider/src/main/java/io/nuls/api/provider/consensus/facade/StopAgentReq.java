package io.nuls.api.provider.consensus.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:51
 * @Description:
 * 停止共识
 * stop  consensus
 */
@Data
@AllArgsConstructor
public class StopAgentReq extends BaseReq {

    /**
     * 共识地址
     */
    String address;

    String password;

}
