package io.nuls.ledger.model;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
public class AccountState {

    private final Integer chaiId;

    private final BigInteger nonce;

    private final BigInteger balance;

    public AccountState(Integer chaiId, BigInteger nonce, BigInteger balance) {
        this.chaiId = chaiId;
        this.nonce = nonce;
        this.balance = balance;
    }


}
