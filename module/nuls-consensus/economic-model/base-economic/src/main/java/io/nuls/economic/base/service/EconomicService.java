package io.nuls.economic.base.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * 经济模型接口类
 * @author tag
 * @date 2019/7/22
 */
public interface EconomicService {
    /**
     * 计算共识奖励
     * @param params 计算共识奖励所需参数
     *
     * @return processor result
     * */
    Result calcReward(Map<String,Object> params);

    /**
     * 注册共识模块初始参数
     * @param params 共识模块初始参数
     *
     * @return processor result
     * */
    Result registerConfig(Map<String,Object> params);
}
