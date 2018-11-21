package io.nuls.ledger.db;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.config.AppConfig;
import io.nuls.ledger.model.AccountState;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
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
        if (isExist(addr)) {
            return getAccountState(addr);
        }
        Long initialNonce = BigInteger.ZERO.longValue();
        AccountState state = new AccountState(chainId, initialNonce, BigInteger.ZERO.longValue());
        try {
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, addr, state.serialize());
        } catch (Exception e) {
            logger.error("createAccount serialize error.", e);
        }
        return state;
    }

    @Override
    public boolean isExist(byte[] addr) {
        return getAccountState(addr) != null;
    }

    @Override
    public AccountState getAccountState(byte[] addr) {
        byte[] value = RocksDBService.get(DataBaseArea.TB_LEDGER_ACCOUNT, addr);
        if (value == null) {
            return null;
        }
        AccountState state = new AccountState();
        try {
            state.parse(value, 0);
            return state;
        } catch (NulsException e) {
            logger.error("getAccountState serialize error.", e);
        }
        return null;
    }

    @Override
    public void delete(byte[] addr) {
        try {
            RocksDBService.delete(DataBaseArea.TB_LEDGER_ACCOUNT, addr);
        } catch (Exception e) {
            logger.error("delete accountState error.", e);
        }
    }

    @Override
    public synchronized long increaseNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        try {
            accountState = accountState.withIncrementedNonce();
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, addr, accountState.serialize());
        } catch (Exception e) {
            logger.error("createAccount serialize error.", e);
        }
        return accountState.getNonce();
    }

    @Override
    public synchronized long setNonce(byte[] addr, long nonce) {
        AccountState accountState = getAccountState(addr);
        try {
            accountState = accountState.withNonce(nonce);
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, addr, accountState.serialize());
        } catch (Exception e) {
            logger.error("createAccount serialize error.", e);
        }
        return accountState.getNonce();
    }

    @Override
    public synchronized long getNonce(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState.getNonce();
    }

    @Override
    public synchronized long getBalance(byte[] addr) {
        AccountState accountState = getAccountState(addr);
        return accountState.getBalance();
    }

    @Override
    public synchronized long addBalance(byte[] addr, long value) {
        AccountState accountState = getAccountState(addr);
        try {
            accountState = accountState.withBalanceIncrement(value);
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, addr, accountState.serialize());
        } catch (Exception e) {
            logger.error("createAccount serialize error.", e);
        }
        return accountState.getBalance();
    }
}
