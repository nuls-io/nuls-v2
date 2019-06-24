package io.nuls.poc.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2019/04/01
 * */
public interface ChainService {
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
     * 查询黄牌列表
     * @param params
     * @return Result
     * */
    Result getPublishList(Map<String,Object> params);

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
     * 获取指定区块轮次
     * @param params
     * @return Result
     * */
    Result getRoundMemberList(Map<String,Object> params);


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
    Result addEvidenceRecord(Map<String,Object> params);

    /**
     * 双花交易记录
     * @param params
     * @return Result
     * */
    Result doubleSpendRecord(Map<String,Object> params);

    /**
     * 获取种子节点列表
     * @param params
     * @return Result
     * */
    Result getSeedNodeList(Map<String,Object> params);

    /**
     * 获取共识两轮次间节点变化信息
     * @param params
     * @return Result
     * */
    Result getAgentChangeInfo(Map<String,Object> params);
}
