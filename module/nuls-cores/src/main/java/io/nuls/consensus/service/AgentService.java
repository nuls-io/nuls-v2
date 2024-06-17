package io.nuls.consensus.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2019/04/01
 * */
public interface AgentService {
    /**
     * Create nodes
     * */
    Result createAgent(Map<String,Object> params);

    /**
     * Create node transaction verification
     * @param params
     * @return Result
     * */
    Result createAgentValid(Map<String,Object> params);


    /**
     * Unregister node
     * @param params
     * return Result
     * */
    Result stopAgent(Map<String,Object> params);

    /**
     * Cancel node transaction verification
     * @param params
     * @return Result
     * */
    Result stopAgentValid(Map<String,Object> params);

    /**
     * Get node list
     * @param params
     * return Result
     * */
    Result getAgentList(Map<String,Object> params);

    /**
     * Get specified node information
     * @param params
     * @return Result
     * */
    Result getAgentInfo(Map<String,Object> params);

    /**
     * Query the status of specified consensus nodes
     * @param params
     * @return Result
     * */
    Result getAgentStatus(Map<String,Object> params);

    /**
     * Modify node consensus status
     * @param params
     * @return Result
     */
    Result updateAgentConsensusStatus(Map<String, Object> params);

    /**
     * Modify node packaging status
     * @param params
     * @return Result
     * */
    Result updateAgentStatus(Map<String,Object> params);

    /**
     * Get the current node's outbound address
     * @param params
     * @return Result
     * */
    Result getNodePackingAddress(Map<String,Object> params);

    /**
     * Get all node block addresses/specifyNBlock assignment
     * @param params
     * @return Result
     * */
    Result getAgentAddressList(Map<String,Object> params);

    /**
     * Obtain the outbound account information of the current node
     * @param params
     * @return Result
     * */
    Result getPackerInfo(Map<String,Object> params);

    /**
     * Obtain seed node information
     * @param params
     * @return Result
     * */
    Result getSeedNodeInfo(Map<String,Object> params);

    Result getStopAgentCoinData(Map<String, Object> params);
}
