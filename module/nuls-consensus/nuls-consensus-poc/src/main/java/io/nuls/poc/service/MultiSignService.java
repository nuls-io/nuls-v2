package io.nuls.poc.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * 多签账户相关交易接口类
 * Multi-Sign Account Related Transaction Interface Class
 *
 * @author tag
 * 2019/07/25
 * */
public interface MultiSignService {
    /**
     * 多签账户创建节点
     * */
    Result createMultiAgent(Map<String,Object> params);

    /**
     * 多签账户停止节点
     * */
    Result stopMultiAgent(Map<String,Object> params);

    /**
     * 多签账户委托节点
     * */
    Result multiDeposit(Map<String,Object> params);

    /**
     * 多签账户退出委托
     * */
    Result multiWithdraw(Map<String,Object> params);
}
