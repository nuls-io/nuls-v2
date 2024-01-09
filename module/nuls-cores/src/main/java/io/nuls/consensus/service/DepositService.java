package io.nuls.consensus.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2019/04/01
 * */
public interface DepositService {
    /**
     * Commission consensus
     * @param params
     * @return Result
     * */
    Result depositToAgent(Map<String,Object> params);

    /**
     * Entrusted consensus transaction verification
     * @param params
     * @return Result
     * */
    Result depositValid(Map<String,Object> params);


    /**
     * Exit consensus
     * @param params
     * @return Result
     * */
    Result withdraw(Map<String,Object> params);

    /**
     * Exit consensus transaction verification
     * @param params
     * @return Result
     * */
    Result withdrawValid(Map<String,Object> params);

    /**
     * Query delegation information list
     * @param params
     * @return Result
     * */
    Result getDepositList(Map<String,Object> params);

}
