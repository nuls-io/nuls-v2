package io.nuls.api.provider.consensus.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-26 15:46
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class GetAgentInfoReq extends BaseReq {

    String agentHash;

}
