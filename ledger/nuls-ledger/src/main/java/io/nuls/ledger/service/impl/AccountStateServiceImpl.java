/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.service.impl;

import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by wangkun23 on 2018/11/29.
 * update by lanjinsheng 2018/12/29.
 */
@Service
public class AccountStateServiceImpl implements AccountStateService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Repository repository;

    @Override
    public AccountState createAccount(String address, int chainId, int assetId) {
        AccountState accountState = getAccountState(address, chainId, assetId);
        if (null != accountState) {
            return getAccountState(address, chainId, assetId);
        }
        String initialNonce = BigInteger.ZERO.toString();
        accountState = new AccountState(chainId, assetId, initialNonce);
        byte[] key = this.getKey(address, chainId, assetId);
        repository.putAccountState(key, accountState);
        return accountState;
    }
    @Override
    public void updateAccountState(String address, int chainId, int assetId,AccountState accountState){
        byte[] key = this.getKey(address, chainId, assetId);
        repository.putAccountState(key, accountState);
    }
    @Override
    public boolean isExist(String address, int chainId, int assetId) {
        return getAccountState(address, chainId, assetId) != null;
    }

    @Override
    public  AccountState getAccountState(String address, int chainId, int assetId) {
        //TODO:促发进行冻结部分的解冻处理,如果没有账号，将创建账号
        byte[] key = this.getKey(address, chainId, assetId);
        AccountState accountState = repository.getAccountState(key);
        if(null == accountState){
            accountState = new AccountState(chainId,assetId,"0");
            repository.putAccountState(key,accountState);
        }else{
            //TODO:账号锁未设置
            //解冻时间锁
            this.unfreezeLockTime(accountState,System.currentTimeMillis());
            repository.putAccountState(key,accountState);
        }
        return accountState;
    }

    /**
     * getOrCreateAccountState
     *
     * @param address
     * @param chainId
     * @param assetId
     * @return
     */
    synchronized AccountState getOrCreateAccountState(String address, int chainId, int assetId) {
        byte[] key = this.getKey(address, chainId, assetId);
        AccountState accountState = repository.getAccountState(key);
        if (accountState == null) {
            accountState = createAccount(address, chainId, assetId);
        }
        return accountState;
    }

    @Override
    public synchronized String setNonce(String address, int chainId, int assetId, String nonce) {
        AccountState accountState = getAccountState(address, chainId, assetId);
        accountState.setNonce(nonce);
        byte[] key = this.getKey(address, chainId, assetId);
        repository.putAccountState(key, accountState);
        return accountState.getNonce();
    }

    @Override
    public  String setUnconfirmNonce(String address, int chainId, int assetId, String nonce) {
        AccountState accountState = getAccountState(address, chainId, assetId);
        accountState.setUnconfirmedNonce(nonce);
        byte[] key = this.getKey(address, chainId, assetId);
        repository.putAccountState(key, accountState);
        return accountState.getUnconfirmedNonce();
    }

    @Override
    public synchronized BigInteger addBalance(String address, int chainId, int assetId, BigInteger value) {
        AccountState accountState = getOrCreateAccountState(address, chainId, assetId);
        accountState.addTotalFromAmount(value);
        byte[] key = this.getKey(address, chainId, assetId);
        repository.putAccountState(key, accountState);
        return accountState.getTotalAmount();
    }


    /**
     * Lan:此方法有问题，无用！
     * 从from转账到to
     *
     * @param fromAddress
     * @param toAddress
     * @param chainId
     * @param assetId
     * @param value
     */
    @Override
    public void transfer(String fromAddress,
                         String toAddress,
                         int chainId,
                         int assetId,
                         BigInteger value) {
        this.addBalance(fromAddress, chainId, assetId, value.negate());
        this.addBalance(toAddress, chainId, assetId, value);
    }

    @Override
    public BigInteger freezeByHeight(String address, int chainId, int assetId, String txHash, BigInteger amount, BigInteger height) {
        AccountState accountState = getAccountState(address, chainId, assetId);
        //TODO:
//        FreezeHeightState state = new FreezeHeightState();
//
//        state.setTxHash(txHash);
//        state.setAmount(amount);
//        state.setHeight(height);
//        state.setCreateTime(TimeService.currentTimeMillis());
//        accountState.getFreezeState().getFreezeHeightStates().add(state);
//        //减去锁定金额
//        BigInteger balance = accountState.getBalance().subtract(amount);
//        accountState.setBalance(balance);
//        byte[] key = this.getKey(address, chainId, assetId);
//        repository.putAccountState(key, accountState);
        return accountState.getAvailableAmount();
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
    public BigInteger unfreezeByHeight(String address, int chainId, int assetId, BigInteger latestHeight) {
        AccountState accountState = getAccountState(address, chainId, assetId);
        // TODO:判断高度是否大于区块的最新高度
//        List<FreezeHeightState> freezeStates = accountState.getFreezeState().getFreezeHeightStates();
//        for (FreezeHeightState state : freezeStates) {
//            if (state.getHeight().compareTo(latestHeight) < -1) {
//                // 增加用户可用余额
//                accountState = accountState.withBalanceIncrement(state.getAmount());
//                byte[] key = this.getKey(address, chainId, assetId);
//                // 然后删除该条锁定记录
//                freezeStates.remove(state);
//                repository.putAccountState(key, accountState);
//            }
//        }
        return accountState.getAvailableAmount();
    }

    @Override
    public BigInteger freezeByLockTime(String address, int chainId, int assetId, String txHash, BigInteger amount, long lockTime) {
        AccountState accountState = getAccountState(address, chainId, assetId);
        //TODO:
//        FreezeLockTimeState state = new FreezeLockTimeState();
//
//        state.setTxHash(txHash);
//        state.setAmount(amount);
//        state.setLockTime(lockTime);
//        state.setCreateTime(TimeService.currentTimeMillis());
//        accountState.getFreezeState().getFreezeLockTimeStates().add(state);
//        //减去锁定金额
//        BigInteger balance = accountState.getBalance().subtract(amount);
//        accountState.setBalance(balance);
//
//        byte[] key = this.getKey(address, chainId, assetId);
//        repository.putAccountState(key, accountState);
        return accountState.getAvailableAmount();
    }

    @Override
    public void unfreezeLockTime(AccountState accountState, long latestBlockTime) {
        // TODO:判断冻结时间是否大于最新区块时间
        List<FreezeLockTimeState> freezeStates = accountState.getFreezeState().getFreezeLockTimeStates();
        for (FreezeLockTimeState state : freezeStates) {
            if (state.getLockTime() < latestBlockTime && state.getLockTime() != -1) {
                // 增加用户可用余额
                 accountState.addTotalToAmount(state.getAmount());
                // 然后删除该条锁定记录
                freezeStates.remove(state);

            }
        }
    }

    /**
     * rockdb key
     *
     * @param address
     * @param assetId
     * @return
     */
    private byte[] getKey(String address, int chainId, int assetId) {
        String key = address + "-" + chainId + "-" + assetId;
        return HexUtil.decode(key);
    }
}
