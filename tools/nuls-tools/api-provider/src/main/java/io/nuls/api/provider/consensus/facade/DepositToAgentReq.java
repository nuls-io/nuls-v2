package io.nuls.api.provider.consensus.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:53
 * @Description:
 * 委托共识
 */
@Data
@AllArgsConstructor
public class DepositToAgentReq extends BaseReq {

    String address;

    String agentHash;

    BigInteger deposit;

    String password;

}
