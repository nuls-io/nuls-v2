package io.nuls.ledger.service.impl;

import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.model.FreezeHeightState;
import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.LongUtils;
import io.nuls.tools.thread.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/29.
 */
@Service
public class AccountStateServiceImpl implements AccountStateService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Repository repository;

    @Override
    public AccountState createAccount(int chainId, String address, int assetId) {

        if (isExist(address, assetId)) {
            return getAccountState(address, assetId);
        }
        Long initialNonce = BigInteger.ZERO.longValue();
        AccountState state = new AccountState(chainId, assetId, initialNonce, BigInteger.ZERO.longValue());
        byte[] key = this.getKey(address, assetId);
        repository.putAccountState(key, state);
        return state;
    }

    @Override
    public boolean isExist(String address, int assetId) {
        return getAccountState(address, assetId) != null;
    }

    @Override
    public AccountState getAccountState(String address, int assetId) {
        byte[] key = this.getKey(address, assetId);
        return repository.getAccountState(key);
    }

    @Override
    public synchronized long increaseNonce(String address, int assetId) {
        AccountState accountState = getAccountState(address, assetId);
        accountState = accountState.withIncrementedNonce();
        byte[] key = this.getKey(address, assetId);
        repository.putAccountState(key, accountState);
        return accountState.getNonce();
    }

    @Override
    public synchronized long setNonce(String address, int assetId, long nonce) {
        AccountState accountState = getAccountState(address, assetId);
        accountState = accountState.withNonce(nonce);
        byte[] key = this.getKey(address, assetId);
        repository.putAccountState(key, accountState);
        return accountState.getNonce();
    }

    @Override
    public synchronized long getNonce(String address, int assetId) {
        AccountState accountState = getAccountState(address, assetId);
        return accountState.getNonce();
    }

    @Override
    public synchronized long getBalance(String address, int assetId) {
        AccountState accountState = getAccountState(address, assetId);
        return accountState.getBalance();
    }

    @Override
    public synchronized long addBalance(String address, int assetId, long value) {
        AccountState accountState = getAccountState(address, assetId);
        accountState = accountState.withBalanceIncrement(value);
        byte[] key = this.getKey(address, assetId);
        repository.putAccountState(key, accountState);
        return accountState.getBalance();
    }

    @Override
    public long freezeByHeight(String address, int assetId, String txHash, long amount, long height) {
        AccountState accountState = getAccountState(address, assetId);
        FreezeHeightState state = new FreezeHeightState();

        state.setTxHash(txHash);
        state.setAmount(amount);
        state.setHeight(height);
        state.setCreateTime(TimeService.currentTimeMillis());
        accountState.getFreezeState().getFreezeHeightStates().add(state);
        //减去锁定金额
        LongUtils.sub(accountState.getBalance(), amount);
        byte[] key = this.getKey(address, assetId);
        repository.putAccountState(key, accountState);
        return accountState.getBalance();
    }

    @Override
    public long unfreezeByHeight(String address) {
        return 0;
    }

    @Override
    public long freezeByLockTime(String address, int assetId, String txHash, long amount, long lockTime) {
        AccountState accountState = getAccountState(address, assetId);
        FreezeLockTimeState state = new FreezeLockTimeState();

        state.setTxHash(txHash);
        state.setAmount(amount);
        state.setLockTime(lockTime);
        state.setCreateTime(TimeService.currentTimeMillis());
        accountState.getFreezeState().getFreezeLockTimeStates().add(state);
        //减去锁定金额
        LongUtils.sub(accountState.getBalance(), amount);
        byte[] key = this.getKey(address, assetId);
        repository.putAccountState(key, accountState);
        return accountState.getBalance();
    }

    @Override
    public long unfreezeLockTime(String address) {
        return 0;
    }

    /**
     * rockdb key
     *
     * @param address
     * @param assetId
     * @return
     */
    private byte[] getKey(String address, int assetId) {
        String key = address + assetId;
        return key.getBytes();
    }
}
