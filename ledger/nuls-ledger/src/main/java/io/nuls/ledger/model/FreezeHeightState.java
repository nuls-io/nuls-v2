package io.nuls.ledger.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * account balance lock
 * Created by wangkun23 on 2018/11/21.
 */
@ToString
@NoArgsConstructor
public class FreezeHeightState implements Serializable {

    /**
     * 交易的hash值
     */
    @Setter
    @Getter
    private String txHash;

    /**
     * 锁定金额
     */
    @Setter
    @Getter
    private BigInteger amount;

    /**
     * 锁定时间
     */
    @Setter
    @Getter
    private long height;

    @Setter
    @Getter
    private long createTime;
}
