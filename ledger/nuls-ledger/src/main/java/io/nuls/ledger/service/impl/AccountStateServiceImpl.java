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
import io.nuls.ledger.constant.ValidateEnum;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.model.UnconfirmedTx;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.*;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.ledger.utils.*;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.model.BigIntegerUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lanjinsheng
 * @date 2018/11/29
 */
@Service
public class AccountStateServiceImpl implements AccountStateService {

    @Autowired
    private Repository repository;
    @Autowired
    private UnconfirmedRepository unconfirmedRepository;
    @Autowired
    FreezeStateService freezeStateService;
    @Autowired
    UnconfirmedStateService unconfirmedStateService;
    @Autowired
    LedgerChainManager ledgerChainManager;


    @Override
    public void updateAccountStateByTx(String assetKey, AccountState accountState) throws Exception {
        //解冻时间高度锁
        freezeStateService.recalculateFreeze(accountState);
        accountState.setLatestUnFreezeTime(TimeUtil.getCurrentTime());
        repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountState);
    }

    @Override
    public void rollAccountState(String assetKey, AccountStateSnapshot accountStateSnapshot) throws Exception {
        //获取当前数据库值
        List<UnconfirmedNonce> unconfirmedNonces = new ArrayList<>();
        accountStateSnapshot.getNonces().forEach(nonce -> {
            UnconfirmedNonce unconfirmedNonce = new UnconfirmedNonce();
            unconfirmedNonce.setNonce(nonce);
            unconfirmedNonce.setTime(TimeUtil.getCurrentTime());
            unconfirmedNonces.add(unconfirmedNonce);
        });
        repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountStateSnapshot.getAccountState());
        //进行nonce的回退合并处理
        if (unconfirmedNonces.size() > 0) {
            unconfirmedStateService.mergeUnconfirmedNonce(accountStateSnapshot.getAccountState().getAddressChainId(), assetKey, unconfirmedNonces, accountStateSnapshot.getTxHashList());
        }
    }


    /**
     * 只返回数据，不同步计算，不进行更新
     *
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
    public AccountState getAccountStateReCal(String address, int addressChainId, int assetChainId, int assetId) {
        try {
            ledgerChainManager.addChain(addressChainId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //账户处理锁
        byte[] key = LedgerUtil.getKey(address, assetChainId, assetId);
        AccountState accountState = repository.getAccountState(addressChainId, key);
        if (null == accountState) {
            accountState = new AccountState(address, addressChainId, assetChainId, assetId, LedgerConstant.INIT_NONCE);
        } else {
            //解冻时间高度锁
            freezeStateService.recalculateFreeze(accountState);
            accountState.setLatestUnFreezeTime(TimeUtil.getCurrentTime());
        }
        return accountState;
    }


    /**
     * @param addressChainId
     * @param newNonce
     * @param unconfirmedTx
     */
    @Override
    public ValidateResult updateUnconfirmTx(int addressChainId, String newNonce, UnconfirmedTx unconfirmedTx) {
        //账户同步锁
        byte[] key = LedgerUtil.getKey(unconfirmedTx.getAddress(), unconfirmedTx.getAssetChainId(), unconfirmedTx.getAssetId());
        synchronized (LockerUtil.getUnconfirmedAccountLocker(unconfirmedTx.getAddress(), unconfirmedTx.getAssetChainId(), unconfirmedTx.getAssetId())) {
            AccountState accountState = getAccountStateUnSyn(unconfirmedTx.getAddress(), addressChainId, unconfirmedTx.getAssetChainId(), unconfirmedTx.getAssetId());
            AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedInfoReCal(accountState);
            if (BigIntegerUtils.isGreaterThan(unconfirmedTx.getSpendAmount(),BigInteger.ZERO)) {
                if (!unconfirmedTx.getFromNonce().equals(accountStateUnconfirmed.getLatestUnconfirmedNonce())) {
                    return ValidateResult.getResult(ValidateEnum.FAIL_CODE, new String[]{unconfirmedTx.getAddress(), unconfirmedTx.getFromNonce(), "account lastNonce=" + accountStateUnconfirmed.getLatestUnconfirmedNonce()});
                }
                UnconfirmedNonce unconfirmedNonce = new UnconfirmedNonce(newNonce);
                accountStateUnconfirmed.addUnconfirmedNonce(unconfirmedNonce);
                LoggerUtil.logger(addressChainId).debug("非确认交易nonce提交后：txHash={},dbNonce={},nonces={}", unconfirmedTx.getTxHash(), accountStateUnconfirmed.getDbNonce(), accountStateUnconfirmed.getUnconfirmedNoncesStrs());
                UnconfirmedAmount unconfirmedAmount = new UnconfirmedAmount(unconfirmedTx.getEarnAmount(), unconfirmedTx.getSpendAmount(),
                        unconfirmedTx.getFromUnLockedAmount(), unconfirmedTx.getToLockedAmount());
                unconfirmedAmount.setTxHash(unconfirmedTx.getTxHash());
                accountStateUnconfirmed.addUnconfirmedAmount(unconfirmedAmount);
                try {
                    unconfirmedRepository.updateAccountStateUnconfirmed(key, accountStateUnconfirmed);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ValidateResult.getResult(ValidateEnum.FAIL_CODE, new String[]{unconfirmedTx.getAddress(), unconfirmedTx.getFromNonce(), "updateUnconfirmTx exception"});
                }
            }

            return ValidateResult.getSuccess();
        }
    }
}
