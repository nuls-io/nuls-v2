package io.nuls.ledger.service;

import io.nuls.ledger.model.AccountState;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
public interface LedgerService {

    /**
     * create
     *
     * @param address
     * @return
     */
    AccountState createAccount(short chainId, String address);

    /**
     * get user account balance
     *
     * @param address
     * @return
     */
    BigInteger getBalance(String address);
}
