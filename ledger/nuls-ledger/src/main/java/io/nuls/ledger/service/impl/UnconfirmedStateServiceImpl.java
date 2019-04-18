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
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.AccountStateUncfd2Cfd;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LockerUtil;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.ledger.utils.TimeUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.model.BigIntegerUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/12/4.
 * 未确认账本状态实现类
 *
 * @author lanjinsheng
 */
@Service
public class UnconfirmedStateServiceImpl implements UnconfirmedStateService {
    @Autowired
    private Repository repository;
    @Autowired
    private AccountStateService accountStateService;
    @Autowired
    private UnconfirmedRepository unconfirmedRepository;


    /**
     * 计算未确认账本信息并返回
     *
     * @param accountState
     * @return
     */
    @Override
    public AccountStateUnconfirmed getUnconfirmedInfo(AccountState accountState) {
        byte[] key = LedgerUtil.getKey(accountState.getAddress(), accountState.getAssetChainId(), accountState.getAssetId());
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getAccountStateUnconfirmed(accountState.getAddressChainId(), key);
        if (null != accountStateUnconfirmed && !accountStateUnconfirmed.isOverTime()) {
            try {
                AccountStateUncfd2Cfd accountStateUncfd2Cfd = unconfirmedRepository.getAccountStateUncfd2Cfd(accountState.getAddressChainId(), key);
                if (null != accountStateUncfd2Cfd) {
                    BigInteger amount = accountStateUnconfirmed.getAmount().subtract(accountStateUncfd2Cfd.getAmount());
                    if (BigIntegerUtils.isGreaterThan(amount, BigInteger.ZERO)) {
                        accountStateUnconfirmed.setAmount(amount);
                    } else {
                        accountStateUnconfirmed.setAmount(BigInteger.ZERO);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }
        return accountStateUnconfirmed;
    }


    @Override
    public void mergeUnconfirmedNonce(AccountState accountState, String assetKey, Map<byte[], byte[]> txsUnconfirmed, AccountStateUnconfirmed accountStateUnconfirmed) {
        //获取未确认的列表
        synchronized (LockerUtil.getUnconfirmedAccountLocker(assetKey)) {
            try {
                AccountStateUnconfirmed accountStateUnconfirmedDB = unconfirmedRepository.getAccountStateUnconfirmed(accountState.getAddressChainId(), assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                if (null != accountStateUnconfirmedDB && !accountStateUnconfirmedDB.isOverTime()) {
                    accountStateUnconfirmedDB.setAmount(accountStateUnconfirmedDB.getAmount().add(accountStateUnconfirmed.getAmount()));
                } else {
                    accountStateUnconfirmedDB = accountStateUnconfirmed;
                }
                unconfirmedRepository.updateAccountStateUnconfirmed(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountStateUnconfirmedDB);
                unconfirmedRepository.batchSaveTxsUnconfirmed(accountState.getAddressChainId(), txsUnconfirmed);
            } catch (Exception e) {
                e.printStackTrace();
                LoggerUtil.logger(accountState.getAddressChainId()).error("@@@@mergeUnconfirmedNonce exception");
            }
        }
    }

    @Override
    public boolean rollUnconfirmedTx(int addressChainId, String assetKey, String txHash) {
        //账户处理锁
        synchronized (LockerUtil.getUnconfirmedAccountLocker(assetKey)) {
            try {
                AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getAccountStateUnconfirmed(addressChainId, assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                if (null == accountStateUnconfirmed) {
                    //清除数据
                    unconfirmedRepository.deleteAccountStateUncfd2Cfd(addressChainId, assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                    return true;
                }
                byte[] preUnconfirmedNonceKey = LedgerUtil.getAccountNoncesByteKey(assetKey, LedgerUtil.getNonceEncode(accountStateUnconfirmed.getFromNonce()));
                TxUnconfirmed txUnconfirmed = unconfirmedRepository.getTxUnconfirmed(addressChainId, preUnconfirmedNonceKey);
                if (null != txUnconfirmed) {
                    accountStateUnconfirmed.setNonce(txUnconfirmed.getNonce());
                    accountStateUnconfirmed.setAmount(accountStateUnconfirmed.getAmount().subtract(txUnconfirmed.getAmount()));
                    accountStateUnconfirmed.setCreateTime(TimeUtil.getCurrentTime());
                    unconfirmedRepository.updateAccountStateUnconfirmed(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountStateUnconfirmed);
                } else {
                    //不存在上一个未确认交易，刷新数据
                    unconfirmedRepository.deleteAccountStateUnconfirmed(addressChainId, assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                    unconfirmedRepository.deleteAccountStateUncfd2Cfd(addressChainId, assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                }
            } catch (Exception e) {
                e.printStackTrace();
                LoggerUtil.logger(addressChainId).error("@@@@rollUnconfirmTx exception assetKey={},txHash={}", assetKey, txHash);
            }
            return false;
        }
    }

    @Override
    public boolean existTxUnconfirmedTx(int addressChainId, byte[] assetNonceKey) throws Exception {
        TxUnconfirmed txUnconfirmed = unconfirmedRepository.getTxUnconfirmed(addressChainId, assetNonceKey);
        return txUnconfirmed != null;
    }

    @Override
    public void clearAccountUnconfirmed(int addressChainId, String accountKey) throws Exception {
        byte[] key = accountKey.getBytes(LedgerConstant.DEFAULT_ENCODING);
        synchronized (LockerUtil.getUnconfirmedAccountLocker(accountKey)) {
            unconfirmedRepository.deleteAccountStateUnconfirmed(addressChainId, key);
            unconfirmedRepository.deleteAccountStateUncfd2Cfd(addressChainId, key);
        }
    }

    @Override
    public void updateAccountAmountUncfd2Cfd(int addressChainId, byte[] assetKey, BigInteger amount) throws Exception {
        AccountStateUncfd2Cfd accountStateUncfd2Cfd = unconfirmedRepository.getAccountStateUncfd2Cfd(addressChainId, assetKey);
        if (null == accountStateUncfd2Cfd) {
            accountStateUncfd2Cfd = new AccountStateUncfd2Cfd(amount);
        } else {
            accountStateUncfd2Cfd.setAmount(accountStateUncfd2Cfd.getAmount().add(amount));
        }
        unconfirmedRepository.saveAccountStateUncfd2Cfd(addressChainId, assetKey, accountStateUncfd2Cfd);
    }

    @Override
    public void batchDeleteUnconfirmedTx(int addressChainId, List<byte[]> keys) throws Exception {
        if (keys.size() > 0) {
            unconfirmedRepository.batchDeleteTxsUnconfirmed(addressChainId, keys);
        }
    }

    @Override
    public ValidateResult updateUnconfirmedTx(int addressChainId, byte[] txNonce, TxUnconfirmed txUnconfirmed) {
        //账户同步锁
        byte[] key = LedgerUtil.getKey(txUnconfirmed.getAddress(), txUnconfirmed.getAssetChainId(), txUnconfirmed.getAssetId());
        synchronized (LockerUtil.getUnconfirmedAccountLocker(txUnconfirmed.getAddress(), txUnconfirmed.getAssetChainId(), txUnconfirmed.getAssetId())) {
            AccountState accountState = accountStateService.getAccountState(txUnconfirmed.getAddress(), addressChainId, txUnconfirmed.getAssetChainId(), txUnconfirmed.getAssetId());
            AccountStateUnconfirmed accountStateUnconfirmed = getUnconfirmedInfo(accountState);
            byte[] preNonce = null;
            BigInteger amount = BigInteger.ZERO;
            AccountStateUncfd2Cfd accountStateUncfd2Cfd = null;
            if (BigIntegerUtils.isGreaterThan(txUnconfirmed.getAmount(), BigInteger.ZERO)) {
                if (null == accountStateUnconfirmed) {
                    //新建
                    preNonce = accountState.getNonce();
                    accountStateUnconfirmed = new AccountStateUnconfirmed(txUnconfirmed.getAddress(), addressChainId, txUnconfirmed.getAssetChainId(), txUnconfirmed.getAssetId(),
                            txUnconfirmed.getFromNonce(), txUnconfirmed.getNonce(), txUnconfirmed.getAmount());
                    amount = accountState.getTotalAmount();
                    accountStateUncfd2Cfd = new AccountStateUncfd2Cfd(BigInteger.ZERO);
                } else {
                    preNonce = accountStateUnconfirmed.getNonce();
                    accountStateUnconfirmed.setFromNonce(txUnconfirmed.getFromNonce());
                    accountStateUnconfirmed.setNonce(txUnconfirmed.getNonce());
                    accountStateUnconfirmed.setAmount(accountStateUnconfirmed.getAmount().add(txUnconfirmed.getAmount()));
                }
                if (!LedgerUtil.equalsNonces(txUnconfirmed.getFromNonce(), preNonce)) {
                    return ValidateResult.getResult(ValidateEnum.FAIL_CODE, new String[]{txUnconfirmed.getAddress(), LedgerUtil.getNonceEncode(txUnconfirmed.getFromNonce()), "account lastNonce=" + LedgerUtil.getNonceEncode(preNonce)});
                }
                try {
                    unconfirmedRepository.updateAccountStateUnconfirmed(key, accountStateUnconfirmed);
                    unconfirmedRepository.saveTxUnconfirmed(addressChainId, LedgerUtil.getAccountNoncesByteKey(txUnconfirmed.getAddress(), txUnconfirmed.getAssetChainId(), txUnconfirmed.getAssetId(), LedgerUtil.getNonceEncode(txNonce)), txUnconfirmed);
                    if (null != accountStateUncfd2Cfd) {
                        unconfirmedRepository.saveAccountStateUncfd2Cfd(addressChainId, key, accountStateUncfd2Cfd);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ValidateResult.getResult(ValidateEnum.FAIL_CODE, new String[]{txUnconfirmed.getAddress(), LedgerUtil.getNonceEncode(txUnconfirmed.getFromNonce()), "updateUnconfirmTx exception"});
                }
            }
            return ValidateResult.getSuccess();
        }
    }

}


