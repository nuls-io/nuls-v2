package io.nuls.consensus.economic.base.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * Economic Model Interface Class
 * @author tag
 * @date 2019/7/22
 */
public interface EconomicService {
    /**
     * Calculate consensus rewards
     * @param params Parameters required for calculating consensus rewards
     *
     * @return processor result
     * */
    Result calcReward(Map<String,Object> params);

    /**
     * Initial parameters for registering consensus module
     * @param params Initial parameters of consensus module
     *
     * @return processor result
     * */
    Result registerConfig(Map<String,Object> params);
}
