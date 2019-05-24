package io.nuls.crosschain.nuls.servive;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * 主网跨链模块特有方法
 * @author tag
 * @date 2019/4/23
 */
public interface MainNetService {
    /**
     * 注册新的跨链交易
     * */
    Result registerCrossChain(Map<String,Object> params);

    /**
     * 注销跨链交易
     */
    Result cancelCrossChain(Map<String,Object> params);

    /**
     * 跨链注册消息变更
     */
    Result crossChainRegisterChange(Map<String, Object> params);

    /**
     * 友链向主网查询所有跨链注册信息
     * Friend Chain inquires all cross-chain registration information from the main network
     * */
    Result getCrossChainList(Map<String,Object> params);

    /**
     * 主网链管理模块向跨链模块获取友链资产信息
     * Access to Friendship Chain Asset Information
     * */
    Result getFriendChainCirculat(Map<String,Object> params);
}
