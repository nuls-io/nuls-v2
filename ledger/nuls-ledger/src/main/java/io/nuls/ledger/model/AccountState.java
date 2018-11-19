package io.nuls.ledger.model;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
public class AccountState {


    private final BigInteger nonce;

    private final BigInteger balance;


    public AccountState(BigInteger nonce, BigInteger balance) {
        this.nonce = nonce;
        this.balance = balance;
    }


}
