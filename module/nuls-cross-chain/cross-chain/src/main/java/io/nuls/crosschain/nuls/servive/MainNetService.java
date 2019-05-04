package io.nuls.crosschain.nuls.servive;

import io.nuls.tools.basic.Result;

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
}
