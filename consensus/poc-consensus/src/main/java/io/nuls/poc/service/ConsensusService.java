package io.nuls.poc.service;

import io.nuls.rpc.model.CmdResponse;

import java.util.List;
import java.util.Map;

public interface ConsensusService {

    /**
     * 创建节点
     * */
    public CmdResponse createAgent(Map<String,Object> params);

    /**
     * 创建节点交易验证
     * */
    public CmdResponse createAgentValid(Map<String,Object> params);

    /**
     * 创建节点交易提交
     * */
    public CmdResponse createAgentCommit(Map<String,Object> params);

    /**
     * 创建节点交易回滚
     * */
    public CmdResponse createAgentRollBack(Map<String,Object> params);

    /**
     * 注销节点
     * */
    public CmdResponse stopAgent(Map<String,Object> params);

    /**
     * 注销节点交易验证
     * */
    public CmdResponse stopAgentValid(Map<String,Object> params);

    /**
     * 注销节点交易提交
     * */
    public CmdResponse stopAgentCommit(Map<String,Object> params);

    /**
     * 注销节点交易回滚
     * */
    public CmdResponse stopAgentRollBack(Map<String,Object> params);

    /**
     * 委托共识
     * */
    public CmdResponse depositToAgent(Map<String,Object> params);

    /**
     * 委托共识交易验证
     * */
    public CmdResponse depositValid(Map<String,Object> params);

    /**
     * 委托共识交易提交
     * */
    public CmdResponse depositCommit(Map<String,Object> params);

    /**
     * 委托共识交易回滚
     * */
    public CmdResponse depositRollBack(Map<String,Object> params);

    /**
     * 退出共识
     * */
    public CmdResponse withdraw(Map<String,Object> params);

    /**
     * 退出共识交易验证
     * */
    public CmdResponse withdrawValid(Map<String,Object> params);

    /**
     * 退出共识交易提交
     * */
    public CmdResponse withdrawCommit(Map<String,Object> params);

    /**
     * 退出共识交易回滚
     * */
    public CmdResponse withdrawRollBack(Map<String,Object> params);

    /**
     * 获取节点列表
     * */
    public CmdResponse getAgentList(Map<String,Object> params);

    /**
     * 获取指定节点信息·
     * */
    public CmdResponse getAgentInfo(Map<String,Object> params);

    /**
     * 查询黄牌列表
     * */
    public CmdResponse getPublishList(Map<String,Object> params);

    /**
     * 查询委托信息列表
     * */
    public CmdResponse getDepositList(Map<String,Object> params);

    /**
     * 查询全网共识信息
     * */
    public CmdResponse getWholeInfo(Map<String,Object> params);

    /**
     * 查询指定账户的共识信息
     * */
    public CmdResponse getInfo(Map<String,Object> params);

    /**
     * 验证区块正确性
     * */
    public CmdResponse validBlock(Map<String,Object> params);

    /**
     * 批量验证共识模块交易
     * */
    public CmdResponse batchValid(List<Object> params);

    /**
     * 获取当前轮次信息
     * */
    public CmdResponse getRoundInfo(List<Object> params);

    /**
     * 查询指定共识节点状态
     * */
    public CmdResponse getAgentStatus(List<Object> params);

    /**
     * 修改节点打包状态
     * */
    public CmdResponse updateAgentStatus(List<Object> params);

    /**
     * 停止一条子链
     * */
    public CmdResponse stopChain(List<Object> params);

    /**
     * 运行一条子链
     * */
    public CmdResponse runChain(List<Object> params);
}
