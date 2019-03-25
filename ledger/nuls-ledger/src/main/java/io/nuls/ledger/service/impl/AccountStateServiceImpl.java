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

import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.model.UnconfirmedTx;
import io.nuls.ledger.model.po.*;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.*;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangkun23 on 2018/11/29.
 * update by lanjinsheng 2018/12/29.
 */
@Service
public class AccountStateServiceImpl implements AccountStateService {

    @Autowired
    private Repository repository;
    @Autowired
    FreezeStateService freezeStateService;
    @Autowired
    LedgerChainManager ledgerChainManager;


    @Override
    public void updateAccountStateByTx(String assetKey, BlockSnapshotAccounts blockSnapshotAccounts, AccountState accountState) throws Exception {
        //同步下未确认交易账户数据
        synchronized (LockerUtil.getAccountLocker(assetKey)) {
            AccountState dbAccountState = repository.getAccountState(accountState.getAddressChainId(), assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
            if (accountState.getNonce().equalsIgnoreCase(LedgerConstant.INIT_NONCE) && dbAccountState.getNonce().equalsIgnoreCase(LedgerConstant.INIT_NONCE)) {
                accountState.setUnconfirmedNonces(dbAccountState.getUnconfirmedNonces());
            } else {
                List<UnconfirmedNonce> unconfirmedNonces = CoinDataUtil.getUnconfirmedNonces(accountState.getNonce(), dbAccountState.getUnconfirmedNonces());
                accountState.setUnconfirmedNonces(unconfirmedNonces);
            }
            ;
            List<UnconfirmedAmount> unconfirmedAmounts = CoinDataUtil.getUnconfirmedAmounts(accountState.getTxHash(), dbAccountState.getUnconfirmedAmounts());
            accountState.setUnconfirmedAmounts(unconfirmedAmounts);
            LoggerUtil.logger.debug("更新打包的交易信息:addr={},orgNonce={},newNonce={},unConfirmedNonce org={},new={}", assetKey,dbAccountState.getNonce(), accountState.getNonce(), dbAccountState.getUnconfirmedNoncesStrs(), accountState.getUnconfirmedNoncesStrs());
            repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountState);
            LoggerUtil.txAmount.debug("hash={},assetKey={},dbAmount={},dbFreeze={},changeTo={},freeze={},oldHash={}", accountState.getTxHash(), assetKey, dbAccountState.getAvailableAmount(), dbAccountState.getFreezeTotal(), accountState.getAvailableAmount(), accountState.getFreezeTotal(), dbAccountState.getTxHash());
//            blockSnapshotAccounts.addAccountState(dbAccountState);
        }
    }

    @Override
    public void rollAccountState(String assetKey, AccountStateSnapshot accountStateSnapshot) throws Exception {
        //同步下未确认交易账户数据
        synchronized (LockerUtil.getAccountLocker(assetKey)) {
            //获取当前数据库值
            AccountState dbAccountState = repository.getAccountState(accountStateSnapshot.getAccountState().getAddressChainId(), assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
            List<UnconfirmedNonce> unconfirmedNonces = new ArrayList<>();
            accountStateSnapshot.getNonces().forEach(nonce -> {
                UnconfirmedNonce unconfirmedNonce = new UnconfirmedNonce();
                unconfirmedNonce.setNonce(nonce);
                unconfirmedNonce.setTime(TimeUtil.getCurrentTime());
                unconfirmedNonces.add(unconfirmedNonce);
            });
            dbAccountState.getUnconfirmedNonces().forEach(unconfirmedNonce -> {
                unconfirmedNonce.setTime(TimeUtil.getCurrentTime());
                unconfirmedNonces.add(unconfirmedNonce);
            });
            accountStateSnapshot.getAccountState().setUnconfirmedNonces(unconfirmedNonces);
            repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountStateSnapshot.getAccountState());
        }
    }

    @Override
    public boolean rollUnconfirmTx(int addressChainId, String assetKey, String nonce, String txHash) {
        //账户处理锁
        synchronized (LockerUtil.getAccountLocker(assetKey)) {
            try {
                AccountState accountState = repository.getAccountState(addressChainId, assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                List<UnconfirmedNonce> list = accountState.getUnconfirmedNonces();
                int i = 0;
                boolean hadRollNonce = rollUnconfirmedNonce(accountState, nonce);
                boolean hadRollAmount = rollUnconfirmedAmount(accountState, txHash);
                if (hadRollNonce || hadRollAmount) {
                    repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountState);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private boolean rollUnconfirmedNonce(AccountState accountState, String nonce) {
        List<UnconfirmedNonce> list = accountState.getUnconfirmedNonces();
        int i = 0;
        boolean hadRoll = false;
        for (UnconfirmedNonce unconfirmedNonce : list) {
            i++;
            if (unconfirmedNonce.getNonce().equalsIgnoreCase(nonce)) {
                hadRoll = true;
                break;
            }
        }
        int size = list.size();
        //从第list的index=i-1起进行清空
        if (hadRoll) {
            for (int j = size; j >= i; j--) {
                LoggerUtil.logger.debug("roll j={},nonce = {}", j, list.get(j - 1).getNonce());
                list.remove(j - 1);
            }

        }
        return hadRoll;
    }

    private boolean rollUnconfirmedAmount(AccountState accountState, String txHash) {
        List<UnconfirmedAmount> list = accountState.getUnconfirmedAmounts();
        int i = 0;
        boolean hadRoll = false;
        for (UnconfirmedAmount unconfirmedAmount : list) {
            i++;
            if (unconfirmedAmount.getTxHash().equalsIgnoreCase(txHash)) {
                hadRoll = true;
                break;
            }
        }
        int size = list.size();
        //从第list的index=i-1起进行清空
        if (hadRoll) {
            for (int j = size; j >= i; j--) {
                LoggerUtil.logger.debug("roll j={},hash = {}", j, list.get(j - 1).getTxHash());
                list.remove(j - 1);
            }

        }
        return hadRoll;
    }

    /**
     * 只返回数据，不同步计算，不进行更新
     * @param address
     * @param addressChainId
     * @param assetChainId
     * @param assetId
     * @return
     */
    @Override
    public AccountState getAccountStateUnSyn(String address, int addressChainId, int assetChainId, int assetId) {
        try {
            ledgerChainManager.addChain(addressChainId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] key = LedgerUtil.getKey(address, assetChainId, assetId);
        AccountState accountState = repository.getAccountState(addressChainId, key);
        if (null == accountState) {
            accountState = new AccountState(address, addressChainId, assetChainId, assetId, LedgerConstant.INIT_NONCE);
        }
        return accountState;
    }

    /**
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     */
    @Override
    public AccountState getAccountState(String address, int addressChainId, int assetChainId, int assetId) {
        try {
            ledgerChainManager.addChain(addressChainId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //账户处理锁
        synchronized (LockerUtil.getAccountLocker(address, assetChainId, assetId)) {
            byte[] key = LedgerUtil.getKey(address, assetChainId, assetId);
            AccountState accountState = repository.getAccountState(addressChainId, key);
            if (null == accountState) {
                accountState = new AccountState(address, addressChainId, assetChainId, assetId, LedgerConstant.INIT_NONCE);
                repository.createAccountState(key, accountState);
            } else {
                if (timeAllow(accountState.getLatestUnFreezeTime())) {
                    //清理未确认交易
                    if (accountState.getUnconfirmedNonces().size() > 0) {
                        if (LedgerUtil.isExpiredNonce(accountState.getUnconfirmedNonces().get(0))) {
                            accountState.getUnconfirmedNonces().clear();
                        }
                    }
                    if (accountState.getUnconfirmedAmounts().size() > 0) {
                        if (LedgerUtil.isExpiredAmount(accountState.getUnconfirmedAmounts().get(0))) {
                            accountState.getUnconfirmedAmounts().clear();
                        }
                    }
                    //解冻时间锁
                    freezeStateService.recalculateFreeze(accountState);
                    accountState.setLatestUnFreezeTime(TimeUtil.getCurrentTime());
                    try {
                        repository.updateAccountState(key, accountState);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return accountState;
        }

    }

    private boolean timeAllow(long latestUnfreezeTime) {
        //是否改为网络时间？
        long nowTime = TimeUtil.getCurrentTime();
        if (nowTime - latestUnfreezeTime > LedgerConstant.TIME_RECALCULATE_FREEZE) {
            //解锁时间超时了,进行重新计算
            return true;
        }
        return false;
    }

    /**
     * @param addressChainId
     * @param newNonce
     * @param unconfirmedTx
     */
    @Override
    public void setUnconfirmTx(int addressChainId, String newNonce, UnconfirmedTx unconfirmedTx) {
        //账户同步锁
        synchronized (LockerUtil.getAccountLocker(unconfirmedTx.getAddress(), unconfirmedTx.getAssetChainId(), unconfirmedTx.getAssetId())) {
            AccountState accountState = getAccountState(unconfirmedTx.getAddress(), addressChainId, unconfirmedTx.getAssetChainId(), unconfirmedTx.getAssetId());
            if (unconfirmedTx.getSpendAmount().compareTo(BigInteger.ZERO) != 0) {
                LoggerUtil.logger.debug("非确认交易nonce提交：txHash={},key={},addNonce={}", unconfirmedTx.getTxHash(), unconfirmedTx.getAddress() + "-" + unconfirmedTx.getAssetChainId() + "-" + unconfirmedTx.getAssetId(), newNonce);
                UnconfirmedNonce unconfirmedNonce = new UnconfirmedNonce(newNonce);
                accountState.addUnconfirmedNonce(unconfirmedNonce);
                LoggerUtil.logger.debug("非确认交易nonce提交后：txHash={},dbNonce={},nonces={}", unconfirmedTx.getTxHash(), accountState.getNonce(), accountState.getUnconfirmedNoncesStrs());
            }
            UnconfirmedAmount unconfirmedAmount = new UnconfirmedAmount(unconfirmedTx.getEarnAmount(), unconfirmedTx.getSpendAmount(),
                    unconfirmedTx.getFromUnLockedAmount(), unconfirmedTx.getToLockedAmount());
            unconfirmedAmount.setTxHash(unconfirmedTx.getTxHash());
            accountState.addUnconfirmedAmount(unconfirmedAmount);
            byte[] key = LedgerUtil.getKey(unconfirmedTx.getAddress(), unconfirmedTx.getAssetChainId(), unconfirmedTx.getAssetId());
            //这个改变无需进行账户的snapshot
            try {
                repository.updateAccountState(key, accountState);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
