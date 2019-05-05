package io.nuls.poc.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * 智能合约与共识交互接口定义类
 * @author tag
 * 2019/5/5
 * */
public interface ContractService {
    /**
     * 创建节点
     * */
    Result createAgent(Map<String,Object> params);

    /**
     * 注销节点
     * @param params
     * return Result
     * */
    Result stopAgent(Map<String,Object> params);

    /**
     * 委托共识
     * @param params
     * @return Result
     * */
    Result depositToAgent(Map<String,Object> params);

    /**
     * 退出共识
     * @param params
     * @return Result
     * */
    Result withdraw(Map<String,Object> params);
}
