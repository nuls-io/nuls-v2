package io.nuls.api.provider.consensus;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.consensus.facade.CreateAgentReq;
import io.nuls.api.provider.consensus.facade.DepositToAgentReq;
import io.nuls.api.provider.consensus.facade.StopAgentReq;
import io.nuls.api.provider.consensus.facade.WithdrawReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:43
 * @Description:
 * consensus provider
 */
public interface ConsensusProvider {

    /**
     * create consensus node
     * @param req
     * @return
     */
    Result<String> createAgent(CreateAgentReq req);

    /**
     * stop consensus node
     * @param req
     * @return
     */
    Result<String> stopAgent(StopAgentReq req);


    /**
     * 委托共识
     * @param req
     * @return
     */
    Result<String> depositToAgent(DepositToAgentReq req);


    /**
     * 退出委托
     * @param req
     * @return
     */
    Result<String> withdraw(WithdrawReq req);




}
