package io.nuls.consensus.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * Multi signature account related transaction interface class
 * Multi-Sign Account Related Transaction Interface Class
 *
 * @author tag
 * 2019/07/25
 * */
public interface MultiSignService {
    /**
     * Multiple account creation nodes
     * */
    Result createMultiAgent(Map<String,Object> params);

    /**
     * Stop node for multi account signing
     * */
    Result stopMultiAgent(Map<String,Object> params);

    /**
     * Multiple account delegation nodes
     * */
    Result multiDeposit(Map<String,Object> params);

    /**
     * Sign multiple accounts to exit the commission
     * */
    Result multiWithdraw(Map<String,Object> params);
}
