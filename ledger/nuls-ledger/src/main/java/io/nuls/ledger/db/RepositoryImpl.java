package io.nuls.ledger.db;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.config.AppConfig;
import io.nuls.ledger.model.AccountState;
import io.nuls.tools.core.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Service
public class RepositoryImpl implements Repository {
    final Logger logger = LoggerFactory.getLogger(AppConfig.class);


    @Override
    public AccountState createAccount(short chainId, byte[] addr) {
        Long initialNonce = BigInteger.ZERO.longValue();
        AccountState state = new AccountState(chainId, initialNonce, BigInteger.ZERO.longValue());
        try {
            RocksDBService.put("account", addr, state.serialize());
        } catch (Exception e) {
            logger.error("createAccount serialize error.", e);
        }
        return state;
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
