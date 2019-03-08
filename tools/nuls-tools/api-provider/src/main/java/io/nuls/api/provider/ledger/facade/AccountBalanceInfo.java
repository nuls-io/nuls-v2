package io.nuls.api.provider.ledger.facade;

import lombok.Data;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 15:39
 * @Description: 功能描述
 */
@Data
public class AccountBalanceInfo {


    private String freeze;

    private BigInteger total;

    private String available;


}
