package io.nuls.poc.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2019/04/01
 * */
public interface DepositService {
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
     * 查询委托信息列表
     * @param params
     * @return Result
     * */
    Result getDepositList(Map<String,Object> params);

}
