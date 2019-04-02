package io.nuls.api.provider.consensus;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.consensus.facade.*;

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


    /**
     * 查询节点信息
     * @param req
     * @return
     */
    Result<AgentInfo> getAgentInfo(GetAgentInfoReq req);


    /**
     * 查询节点列表
     * @param req
     * @return
     */
    Result<AgentInfo> getAgentList(GetAgentListReq req);


}
