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

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.Uncfd2CfdKey;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LockerUtil;
import io.nuls.ledger.utils.LoggerUtil;

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
    public AccountStateUnconfirmed getUnconfirmedInfo(String address, int addressChainId, int assetChainId, int assetId, AccountState accountState) {
        String key = LedgerUtil.getKeyStr(address, assetChainId, assetId);
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getMemAccountStateUnconfirmed(addressChainId, key);
        if (null != accountStateUnconfirmed && !accountStateUnconfirmed.isOverTime()) {
            //未确认与已确认状态一样，则未确认是最后的缓存信息
            if (LedgerUtil.equalsNonces(accountState.getNonce(), accountStateUnconfirmed.getNonce())) {
                return null;
            }
        } else {
            return null;
        }
        return accountStateUnconfirmed;
    }

    /**
     * 清理过期数据释放内存，未确认交易提交时候促发
     *
     * @param accountState
     * @return
     */
    @Override
    public AccountStateUnconfirmed getUnconfirmedInfoAndClear(String address, int addressChainId, int assetChainId, int assetId,AccountState accountState) {
        String key = LedgerUtil.getKeyStr(address, assetChainId, assetId);
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getMemAccountStateUnconfirmed(addressChainId, key);
        if (null != accountStateUnconfirmed) {
            if (accountStateUnconfirmed.isOverTime()) {
                try {
                    clearAccountUnconfirmed(addressChainId, key);
                    return null;
                } catch (Exception e) {
                    LoggerUtil.logger(addressChainId).error(e);
                }
            } else {
                //未确认与已确认状态一样，则未确认是最后的缓存信息
                if (LedgerUtil.equalsNonces(accountState.getNonce(), accountStateUnconfirmed.getNonce())) {
                    return null;
                }
            }
        } else {
            return null;
        }
        return accountStateUnconfirmed;
    }

    /**
     * 获取账本nonce信息
     *
     * @param accountState
     * @return
     */
    @Override
    public AccountStateUnconfirmed getUnconfirmedJustNonce(String address, int addressChainId, int assetChainId, int assetId,AccountState accountState) {
        String key = LedgerUtil.getKeyStr(address, assetChainId, assetId);
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getMemAccountStateUnconfirmed(addressChainId, key);
        if (null != accountStateUnconfirmed && !accountStateUnconfirmed.isOverTime()) {
            //未确认与已确认状态一样，则未确认是最后的缓存信息
            if (LedgerUtil.equalsNonces(accountState.getNonce(), accountStateUnconfirmed.getNonce())) {
                return null;
            }
            return accountStateUnconfirmed;
        } else {
            return null;
        }
    }

    @Override
    public void mergeUnconfirmedNonce(int addressChainId,AccountState accountState, String assetKey, Map<String, TxUnconfirmed> txsUnconfirmed, AccountStateUnconfirmed accountStateUnconfirmed) {
        //获取未确认的列表
        try {
            AccountStateUnconfirmed accountStateUnconfirmedDB = unconfirmedRepository.getMemAccountStateUnconfirmed(addressChainId, assetKey);
            //清空过期数据
            if (null != accountStateUnconfirmedDB && accountStateUnconfirmedDB.isOverTime()) {
                unconfirmedRepository.delMemAccountStateUnconfirmed(addressChainId, assetKey);
                unconfirmedRepository.clearMemUnconfirmedTxs(addressChainId, assetKey);
                accountStateUnconfirmedDB = null;
            }
            if (null == accountStateUnconfirmedDB) {
                unconfirmedRepository.saveMemAccountStateUnconfirmed(addressChainId, assetKey, accountStateUnconfirmed);
            } else {
                accountStateUnconfirmedDB.setUnconfirmedAmount(accountStateUnconfirmedDB.getUnconfirmedAmount().add(accountStateUnconfirmed.getUnconfirmedAmount()));
            }
            unconfirmedRepository.saveMemUnconfirmedTxs(addressChainId, assetKey, txsUnconfirmed);
        } catch (Exception e) {
            LoggerUtil.logger(addressChainId).error("@@@@mergeUnconfirmedNonce exception");
        }
    }

    @Override
    public boolean rollUnconfirmedTx(int addressChainId, String assetKey, String txHash) {
        //更新未确认上一个状态
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getMemAccountStateUnconfirmed(addressChainId, assetKey);
        try {
            if (null != accountStateUnconfirmed) {
                if (LedgerUtil.equalsNonces(accountStateUnconfirmed.getNonce(), LedgerUtil.getNonceDecodeByTxHash(txHash))) {
                    TxUnconfirmed preTxUnconfirmed = unconfirmedRepository.getMemUnconfirmedTx(addressChainId, assetKey, LedgerUtil.getNonceEncode(accountStateUnconfirmed.getFromNonce()));
                    TxUnconfirmed nowTxUnconfirmed = unconfirmedRepository.getMemUnconfirmedTx(addressChainId, assetKey, LedgerUtil.getNonceEncode(accountStateUnconfirmed.getNonce()));
                    if (null != preTxUnconfirmed && (null != nowTxUnconfirmed)) {
                        System.arraycopy(preTxUnconfirmed.getNonce(), 0, accountStateUnconfirmed.getNonce(), 0, LedgerConstant.NONCE_LENGHT);
                        System.arraycopy(preTxUnconfirmed.getFromNonce(), 0, accountStateUnconfirmed.getFromNonce(), 0, LedgerConstant.NONCE_LENGHT);
                        accountStateUnconfirmed.setUnconfirmedAmount(accountStateUnconfirmed.getUnconfirmedAmount().subtract(nowTxUnconfirmed.getAmount()));
                        accountStateUnconfirmed.setCreateTime(NulsDateUtils.getCurrentTimeSeconds());
                    } else {
                        //不存在上一个未确认交易，刷新数据
                        unconfirmedRepository.delMemAccountStateUnconfirmed(addressChainId, assetKey);
                    }
                }
            }
            //删除未确认过程缓存-该笔交易之后的未确认链
            TxUnconfirmed txUnconfirmed = unconfirmedRepository.getMemUnconfirmedTx(addressChainId, assetKey, LedgerUtil.getNonceEncodeByTxHash(txHash));
            unconfirmedRepository.clearMemUnconfirmedTxs(addressChainId, assetKey, txUnconfirmed);
        } catch (Exception e) {
            LoggerUtil.logger(addressChainId).error("@@@@rollUnconfirmTx exception assetKey={},txHash={}", assetKey, txHash);
            LoggerUtil.logger(addressChainId).error(e);
            return false;
        }
        return true;

    }

    @Override
    public boolean existTxUnconfirmedTx(int addressChainId, String assetKey, String nonce) throws Exception {
        TxUnconfirmed txUnconfirmed = unconfirmedRepository.getMemUnconfirmedTx(addressChainId, assetKey, nonce);
        if (null != txUnconfirmed) {
            unconfirmedRepository.addUncfd2Cfd(addressChainId, assetKey, txUnconfirmed.getAmount());
        }
        return txUnconfirmed != null;
    }

    /**
     * @param addressChainId
     * @param accountKey     address+chainId+assetId
     * @throws Exception
     */
    @Override
    public void clearAccountUnconfirmed(int addressChainId, String accountKey) throws Exception {
        unconfirmedRepository.delMemAccountStateUnconfirmed(addressChainId, accountKey);
        unconfirmedRepository.clearMemUnconfirmedTxs(addressChainId, accountKey);
    }

    @Override
    public void clearAllAccountUnconfirmed(int addressChainId) throws Exception {
        //账户处理锁
        LockerUtil.UNCONFIRMED_SYNC_LOCKER.lock();
        try {
            unconfirmedRepository.clearAllMemUnconfirmedTxs(addressChainId);
        } finally {
            LockerUtil.UNCONFIRMED_SYNC_LOCKER.unlock();
        }

    }

    @Override
    public void batchDeleteUnconfirmedTx(int addressChainId, List<Uncfd2CfdKey> keys) throws Exception {
        for (Uncfd2CfdKey uncfd2CfdKey : keys) {
            unconfirmedRepository.delMemUnconfirmedTx(addressChainId, uncfd2CfdKey.getAssetKey(), uncfd2CfdKey.getNonceKey());
        }
    }


    @Override
    public ValidateResult updateUnconfirmedTx(String txHash, int addressChainId, byte[] txNonce, TxUnconfirmed txUnconfirmed) {
        //账户同步锁
        String keyStr = LedgerUtil.getKeyStr(txUnconfirmed.getAddress(), txUnconfirmed.getAssetChainId(), txUnconfirmed.getAssetId());
        AccountState accountState = accountStateService.getAccountState(txUnconfirmed.getAddress(), addressChainId, txUnconfirmed.getAssetChainId(), txUnconfirmed.getAssetId());
        AccountStateUnconfirmed accountStateUnconfirmed = getUnconfirmedInfoAndClear(txUnconfirmed.getAddress(),addressChainId,txUnconfirmed.getAssetChainId(),txUnconfirmed.getAssetId(),accountState);
        byte[] preNonce = null;
        if (null == accountStateUnconfirmed) {
            //新建
            preNonce = accountState.getNonce();
        } else {
            preNonce = accountStateUnconfirmed.getNonce();
        }
//        LoggerUtil.logger(addressChainId).debug("####updateUnconfirmedTx txHash={},preNonce={}====fromNonce={},updateToNonce={}", txHash,
//                LedgerUtil.getNonceEncode(preNonce), LedgerUtil.getNonceEncode(txUnconfirmed.getFromNonce()), LedgerUtil.getNonceEncode(txNonce));
        if (!LedgerUtil.equalsNonces(txUnconfirmed.getFromNonce(), preNonce)) {
            return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{txUnconfirmed.getAddress(), LedgerUtil.getNonceEncode(txUnconfirmed.getFromNonce()), "account lastNonce=" + LedgerUtil.getNonceEncode(preNonce)});
        }
        if (null == accountStateUnconfirmed) {
            accountStateUnconfirmed = new AccountStateUnconfirmed(txUnconfirmed.getFromNonce(), txUnconfirmed.getNonce(), txUnconfirmed.getAmount());
            unconfirmedRepository.saveMemAccountStateUnconfirmed(addressChainId, keyStr, accountStateUnconfirmed);
        } else {
            System.arraycopy(txUnconfirmed.getFromNonce(), 0, accountStateUnconfirmed.getFromNonce(), 0, LedgerConstant.NONCE_LENGHT);
            System.arraycopy(txUnconfirmed.getNonce(), 0, accountStateUnconfirmed.getNonce(), 0, LedgerConstant.NONCE_LENGHT);
            accountStateUnconfirmed.setUnconfirmedAmount(accountStateUnconfirmed.getUnconfirmedAmount().add(txUnconfirmed.getAmount()));
            accountStateUnconfirmed.setCreateTime(NulsDateUtils.getCurrentTimeSeconds());
        }
        try {
            TxUnconfirmed preTxUnconfirmed = unconfirmedRepository.getMemUnconfirmedTx(addressChainId, keyStr, LedgerUtil.getNonceEncode(txUnconfirmed.getFromNonce()));
            if (null != preTxUnconfirmed) {
                System.arraycopy(txUnconfirmed.getNonce(), 0, preTxUnconfirmed.getNextNonce(), 0, LedgerConstant.NONCE_LENGHT);
            }
            unconfirmedRepository.saveMemUnconfirmedTx(addressChainId, keyStr, LedgerUtil.getNonceEncode(txNonce), txUnconfirmed);
        } catch (Exception e) {
            LoggerUtil.logger(addressChainId).error(e);
            return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{txUnconfirmed.getAddress(), LedgerUtil.getNonceEncode(txUnconfirmed.getFromNonce()), "updateUnconfirmTx exception"});
        }

        return ValidateResult.getSuccess();
    }
}


