package io.nuls.poc.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2019/04/01
 * */
public interface AgentService {
    /**
     * 创建节点
     * */
    Result createAgent(Map<String,Object> params);

    /**
     * 创建节点交易验证
     * @param params
     * @return Result
     * */
    Result createAgentValid(Map<String,Object> params);


    /**
     * 注销节点
     * @param params
     * return Result
     * */
    Result stopAgent(Map<String,Object> params);

    /**
     * 注销节点交易验证
     * @param params
     * @return Result
     * */
    Result stopAgentValid(Map<String,Object> params);

    /**
     * 获取节点列表
     * @param params
     * return Result
     * */
    Result getAgentList(Map<String,Object> params);

    /**
     * 获取指定节点信息
     * @param params
     * @return Result
     * */
    Result getAgentInfo(Map<String,Object> params);

    /**
     * 查询指定共识节点状态
     * @param params
     * @return Result
     * */
    Result getAgentStatus(Map<String,Object> params);

    /**
     * 修改节点共识状态
     * @param params
     * @return Result
     */
    Result updateAgentConsensusStatus(Map<String, Object> params);

    /**
     * 修改节点打包状态
     * @param params
     * @return Result
     * */
    Result updateAgentStatus(Map<String,Object> params);

    /**
     * 获取当前节点出块地址
     * @param params
     * @return Result
     * */
    Result getNodePackingAddress(Map<String,Object> params);

    /**
     * 获取所有节点出块地址/指定N个区块出块指定
     * @param params
     * @return Result
     * */
    Result getAgentAddressList(Map<String,Object> params);

    /**
     * 获取当前节点的出块账户信息
     * @param params
     * @return Result
     * */
    Result getPackerInfo(Map<String,Object> params);

    /**
     * 获取种子节点信息
     * @param params
     * @return Result
     * */
    Result getSeedNodeInfo(Map<String,Object> params);
}
