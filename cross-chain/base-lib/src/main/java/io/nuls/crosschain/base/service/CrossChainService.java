package io.nuls.crosschain.base.service;

import io.nuls.base.data.Transaction;
import io.nuls.tools.basic.Result;

import java.util.Map;

/**
 * 跨链模块服务接口类
 * @author tag
 * @date 2019/4/8
 */
public interface CrossChainService {

    /**
     * 创建跨链交易
     * @param params 创建跨链交易所需参数
     * */
    Result createCrossTx(Map<String,Object> params);

    /**
     * 跨链交易验证
     * @param params 跨链交易验证所需参数
     * */
    Result validCrossTx(Map<String,Object> params);

    /**
     * 跨链交易提交
     * @param params 跨链交易验证所需参数
     * */
    Result commitCrossTx(Map<String,Object> params);

    /**
     * 跨链交易回滚
     * @param params 跨链交易验证所需参数
     * */
    Result rollbackCrossTx(Map<String,Object> params);

    /**
     * 跨链交易批量验证
     * @param params 跨链交易验证所需参数
     * */
    Result crossTxBatchValid(Map<String,Object> params);

    /**
     * 查询跨链交易在主网的处理结果
     * @param params 跨链交易验证所需参数
     * */
    Result getCrossTxState(Map<String,Object> params);
}
