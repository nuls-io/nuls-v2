package io.nuls.poc.service;
import io.nuls.tools.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2018/11/12
 * */
public interface ConsensusService {
    /**
     * 创建节点
     * */
    Result createAgent(Map<String,Object> params);

    /**
     * 创建节点交易验证
     * */
    Result createAgentValid(Map<String,Object> params);

    /**
     * 创建节点交易提交
     * */
    Result createAgentCommit(Map<String,Object> params);

    /**
     * 创建节点交易回滚
     * */
    Result createAgentRollBack(Map<String,Object> params);

    /**
     * 注销节点
     * */
    Result stopAgent(Map<String,Object> params);

    /**
     * 注销节点交易验证
     * */
    Result stopAgentValid(Map<String,Object> params);

    /**
     * 注销节点交易提交
     * */
    Result stopAgentCommit(Map<String,Object> params);

    /**
     * 注销节点交易回滚
     * */
    Result stopAgentRollBack(Map<String,Object> params);

    /**
     * 委托共识
     * */
    Result depositToAgent(Map<String,Object> params);

    /**
     * 委托共识交易验证
     * */
    Result depositValid(Map<String,Object> params);

    /**
     * 委托共识交易提交
     * */
    Result depositCommit(Map<String,Object> params);

    /**
     * 委托共识交易回滚
     * */
    Result depositRollBack(Map<String,Object> params);

    /**
     * 退出共识
     * */
    Result withdraw(Map<String,Object> params);

    /**
     * 退出共识交易验证
     * */
    Result withdrawValid(Map<String,Object> params);

    /**
     * 退出共识交易提交
     * */
    Result withdrawCommit(Map<String,Object> params);

    /**
     * 退出共识交易回滚
     * */
    Result withdrawRollBack(Map<String,Object> params);

    /**
     * 获取节点列表
     * */
    Result getAgentList(Map<String,Object> params);

    /**
     * 获取指定节点信息·
     * */
    Result getAgentInfo(Map<String,Object> params);

    /**
     * 查询黄牌列表
     * */
    Result getPublishList(Map<String,Object> params);

    /**
     * 查询委托信息列表
     * */
    Result getDepositList(Map<String,Object> params);

    /**
     * 查询全网共识信息
     * */
    Result getWholeInfo(Map<String,Object> params);

    /**
     * 查询指定账户的共识信息
     * */
    Result getInfo(Map<String,Object> params);

    /**
     * 验证区块正确性
     * */
    Result validBlock(Map<String,Object> params);

    /**
     * 批量验证共识模块交易
     * */
    Result batchValid(Map<String,Object> params);

    /**
     * 获取当前轮次信息
     * */
    Result getCurrentRoundInfo(Map<String,Object> params);

    /**
     * 查询指定共识节点状态
     * */
    Result getAgentStatus(Map<String,Object> params);

    /**
     * 修改节点共识状态
     */
    Result updateAgentConsensusStatus(Map<String, Object> params);

    /**
     * 修改节点打包状态
     * */
    Result updateAgentStatus(Map<String,Object> params);

    /**
     * 停止一条子链
     * */
    Result stopChain(Map<String,Object> params);

    /**
     * 运行一条子链
     * */
    Result runChain(Map<String,Object> params);

    /**
     * 运行主链
     * */
    Result runMainChain(Map<String,Object> params);
}
