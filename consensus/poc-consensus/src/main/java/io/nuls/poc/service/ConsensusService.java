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
     * 委托共识
     * @param params
     * @return Result
     * */
    Result depositToAgent(Map<String,Object> params);

    /**
     * 委托共识交易验证
     * @param params
     * @return Result
     * */
    Result depositValid(Map<String,Object> params);


    /**
     * 退出共识
     * @param params
     * @return Result
     * */
    Result withdraw(Map<String,Object> params);

    /**
     * 退出共识交易验证
     * @param params
     * @return Result
     * */
    Result withdrawValid(Map<String,Object> params);

    /**
     * 共识模块交易提交
     * @param params
     * @return Result
     * */
    Result commitCmd(Map<String,Object> params);

    /**
     * 共识模块交易回滚
     * @param params
     * @return Result
     * */
    Result rollbackCmd(Map<String,Object> params);

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
     * 查询黄牌列表
     * @param params
     * @return Result
     * */
    Result getPublishList(Map<String,Object> params);

    /**
     * 查询委托信息列表
     * @param params
     * @return Result
     * */
    Result getDepositList(Map<String,Object> params);

    /**
     * 查询全网共识信息
     * @param params
     * @return Result
     * */
    Result getWholeInfo(Map<String,Object> params);

    /**
     * 查询指定账户的共识信息
     * @param params
     * @return Result
     * */
    Result getInfo(Map<String,Object> params);

    /**
     * 验证区块正确性
     * @param params
     * @return Result
     * */
    Result validBlock(Map<String,Object> params);

    /**
     * 批量验证共识模块交易
     * @param params
     * @return Result
     * */
    Result batchValid(Map<String,Object> params);

    /**
     * 获取当前轮次信息
     * @param params
     * @return Result
     * */
    Result getCurrentRoundInfo(Map<String,Object> params);

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
     * 停止一条子链
     * @param params
     * @return Result
     * */
    Result stopChain(Map<String,Object> params);

    /**
     * 运行一条子链
     * @param params
     * @return Result
     * */
    Result runChain(Map<String,Object> params);

    /**
     * 运行主链
     * @param params
     * @return Result
     * */
    Result runMainChain(Map<String,Object> params);

    /**
     * 缓存最新区块
     * @param params
     * @return Result
     * */
    Result addBlock(Map<String,Object> params);

    /**
     * 连分叉区块回滚
     * @param params
     * @return Result
     * */
    Result chainRollBack(Map<String,Object> params);

    /**
     * 缓存最新区块
     * @param params
     * @return Result
     * */
    Result addEvidenceRecord(Map<String,Object> params);

    /**
     * 双花交易记录
     * @param params
     * @return Result
     * */
    Result doubleSpendRecord(Map<String,Object> params);

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
}
