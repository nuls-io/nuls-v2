package io.nuls.api.provider.consensus.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:44
 * @Description:
 *  创建共识节点
 *  create consensus agent node
 */
@Data
@AllArgsConstructor
public class CreateAgentReq extends BaseReq {

    private String agentAddress;

    private String packingAddress;

    private String rewardAddress;

    private Integer commissionRate;

    private BigInteger deposit;

    private String password;

}
