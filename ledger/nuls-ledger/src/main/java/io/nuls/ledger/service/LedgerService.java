package io.nuls.ledger.service;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
public interface LedgerService {

    /**
     * get user account balance
     *
     * @param address
     * @return
     */
    BigInteger getBalance(String address);
}
