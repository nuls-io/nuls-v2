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
     * 委托共识
     * @param req
     * @return
     */
    Result<String> depositToAgent(DepositToAgentReq req);


    /**
     * 委托共识
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> depositToAgentForMultiSignAccount(MultiSignAccountDepositToAgentReq req);


    /**
     * 退出委托
     * @param req
     * @return
     */
    Result<String> withdraw(WithdrawReq req);


    /**
     * 退出委托
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> withdrawForMultiSignAccount(MultiSignAccountWithdrawReq req);


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

    /**
     * 查询委托列表
     * @param req
     * @return
     */
    Result<DepositInfo> getDepositList(GetDepositListReq req);
}
