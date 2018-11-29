package io.nuls.ledger.db;

import io.nuls.ledger.model.AccountState;

import java.math.BigInteger;

public interface Repository {


    /**
     * put accountState to rocksdb
     *
     * @param key
     * @param accountState
     */
    void putAccountState(byte[] key, AccountState accountState);

    /**
     * get accountState from rocksdb
     *
     * @param key
     * @return
     */
    AccountState getAccountState(byte[] key);
}
