package io.nuls.consensus.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * Smart Contract and Consensus Interaction Interface Definition Class
 * @author tag
 * 2019/5/5
 * */
public interface ContractService {
    /**
     * Create nodes
     * */
    Result createAgent(Map<String,Object> params);

    /**
     * Unregister node
     * @param params
     * return Result
     * */
    Result stopAgent(Map<String,Object> params);

    /**
     * Commission consensus
     * @param params
     * @return Result
     * */
    Result depositToAgent(Map<String,Object> params);

    /**
     * Exit consensus
     * @param params
     * @return Result
     * */
    Result withdraw(Map<String,Object> params);

    /**
     * Query node information
     * @param params
     * @return Result
     * */
    Result getAgentInfo(Map<String,Object> params);

    /**
     * Query commission information
     * @param params
     * @return Result
     * */
    Result getDepositInfo(Map<String,Object> params);

    /**
     * Transaction module triggeredCoinBaseSmart contracts
     * @param params
     * @return Result
     * */
    Result triggerCoinBaseContract(Map<String,Object> params);
}
