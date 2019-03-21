package io.nuls.api.provider.transaction.facade;

import lombok.Data;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 16:40
 * @Description: 功能描述
 */
@Data
public class TransactionCoinData {

    /**
     * byte[24] 账户地址
     */
    protected String address;

    /**
     * uint16 资产发行链的id
     */
    protected int assetsChainId;

    /**
     * uint16 资产id
     */
    protected int assetsId;

    /**
     * uint128 数量
     */
    protected BigInteger amount;

    /**
     * byte[8]
     */
    private String nonce;

}
