package io.nuls.ledger.db;

import io.nuls.ledger.model.AccountState;
import io.nuls.tools.core.annotation.Service;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Service
public class RepositoryImpl implements Repository {

    @Override
    public AccountState createAccount(byte[] addr) {
        return null;
    }

    @Override
    public boolean isExist(byte[] addr) {
        return false;
    }

    @Override
    public AccountState getAccountState(byte[] addr) {
        return null;
    }

    @Override
    public void delete(byte[] addr) {

    }

    @Override
    public BigInteger increaseNonce(byte[] addr) {
        return null;
    }

    @Override
    public BigInteger setNonce(byte[] addr, BigInteger nonce) {
        return null;
    }

    @Override
    public BigInteger getNonce(byte[] addr) {
        return null;
    }

    @Override
    public BigInteger getBalance(byte[] addr) {
        return null;
    }

    @Override
    public BigInteger addBalance(byte[] addr, BigInteger value) {
        return null;
    }
}
