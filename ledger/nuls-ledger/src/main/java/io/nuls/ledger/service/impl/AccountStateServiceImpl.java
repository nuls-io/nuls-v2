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
import java.util.List;

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

    /**
     * TODO..暂时这样写,正确做法是根据区块来解析所有的高度冻结
     *
     * @param address
     * @param assetId
     * @param latestHeight
     * @return
     */
    @Override
    public long unfreezeByHeight(String address, int assetId, long latestHeight) {
        AccountState accountState = getAccountState(address, assetId);
        // 判断高度是否大于区块的最新高度
        List<FreezeHeightState> freezeStates = accountState.getFreezeState().getFreezeHeightStates();
        for (FreezeHeightState state : freezeStates) {
            if (state.getHeight() < latestHeight) {
                // 增加用户可用余额
                accountState = accountState.withBalanceIncrement(state.getAmount());
                byte[] key = this.getKey(address, assetId);
                // 然后删除该条锁定记录
                freezeStates.remove(state);
                repository.putAccountState(key, accountState);
            }
        }
        return accountState.getBalance();
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
    public long unfreezeLockTime(String address, int assetId, long latestBlockTime) {
        AccountState accountState = getAccountState(address, assetId);
        // 判断冻结时间是否大于最新区块时间
        List<FreezeLockTimeState> freezeStates = accountState.getFreezeState().getFreezeLockTimeStates();
        for (FreezeLockTimeState state : freezeStates) {
            if (state.getLockTime() < latestBlockTime) {
                // 增加用户可用余额
                accountState = accountState.withBalanceIncrement(state.getAmount());
                byte[] key = this.getKey(address, assetId);
                // 然后删除该条锁定记录
                freezeStates.remove(state);
                repository.putAccountState(key, accountState);
            }
        }
        return accountState.getBalance();
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
