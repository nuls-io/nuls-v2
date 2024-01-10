package io.nuls.base.api.provider.consensus;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.consensus.facade.*;
import io.nuls.base.api.provider.transaction.facade.MultiSignTransferRes;

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
     * create consensus node
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> createAgentForMultiSignAccount(CreateMultiSignAgentReq req);

    /**
     * stop consensus node
     * @param req
     * @return
     */
    Result<String> stopAgent(StopAgentReq req);
    Result<String> getStopAgentCoinData(GetStopAgentCoinDataReq req);

    /**
     * stop consensus node
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> stopAgentForMultiSignAccount(StopMultiSignAgentReq req);


    /**
     * Commission consensus
     * @param req
     * @return
     */
    Result<String> depositToAgent(DepositToAgentReq req);


    /**
     * Commission consensus
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> depositToAgentForMultiSignAccount(MultiSignAccountDepositToAgentReq req);


    /**
     * Exit the commission
     * @param req
     * @return
     */
    Result<String> withdraw(WithdrawReq req);


    /**
     * Exit the commission
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> withdrawForMultiSignAccount(MultiSignAccountWithdrawReq req);


    /**
     * Query node information
     * @param req
     * @return
     */
    Result<AgentInfo> getAgentInfo(GetAgentInfoReq req);


    /**
     * Query node list
     * @param req
     * @return
     */
    Result<AgentInfo> getAgentList(GetAgentListReq req);

    /**
     * Query delegation list
     * @param req
     * @return
     */
    Result<DepositInfo> getDepositList(GetDepositListReq req);
}
