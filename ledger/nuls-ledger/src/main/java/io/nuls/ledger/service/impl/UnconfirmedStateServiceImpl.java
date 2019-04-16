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
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.UnconfirmedAmount;
import io.nuls.ledger.model.po.UnconfirmedNonce;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.ledger.utils.CoinDataUtil;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LockerUtil;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.model.StringUtils;

import java.util.List;

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
     * 查看是否有解锁的信息，有就返回true
     *
     * @param accountStateUnconfirmed
     * @return
     */
    public boolean hadUpdateUnconfirmedState(AccountStateUnconfirmed accountStateUnconfirmed, String dbNonce, String dbHash) {
        //存在过时的数据
        if (accountStateUnconfirmed.getUnconfirmedNonces().size() > 0) {
            if (LedgerUtil.isExpiredNonce(accountStateUnconfirmed.getUnconfirmedNonces().get(0))) {
                return true;
            }
        }
        if (accountStateUnconfirmed.getUnconfirmedAmounts().size() > 0) {
            if (LedgerUtil.isExpiredAmount(accountStateUnconfirmed.getUnconfirmedAmounts().get(0))) {
                return true;
            }
        }
        if (StringUtils.isNotBlank(dbHash) && accountStateUnconfirmed.getUnconfirmedAmounts().size() > 0) {
            return !dbHash.equalsIgnoreCase(accountStateUnconfirmed.getUnconfirmedAmounts().get(0).getTxHash());
        }
        /*数据库nonce更新过，并且存在未确认的nonce列表*/
        if (!dbNonce.equals(LedgerConstant.INIT_NONCE) && accountStateUnconfirmed.getUnconfirmedNonces().size() > 0) {
            return accountStateUnconfirmed.getUnconfirmedNoncesStrs().contains(dbNonce);
        }
        return false;
    }

    /**
     * 计算未确认账本信息并返回
     *
     * @param accountState
     * @return
     */
    @Override
    public AccountStateUnconfirmed getUnconfirmedInfoReCal(AccountState accountState) {
        byte[] key = LedgerUtil.getKey(accountState.getAddress(), accountState.getAssetChainId(), accountState.getAssetId());
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getAccountStateUnconfirmed(accountState.getAddressChainId(), key);
        if (null == accountStateUnconfirmed) {
            return new AccountStateUnconfirmed(accountState.getAddress(), accountState.getAddressChainId(), accountState.getAssetChainId(), accountState.getAssetId(), accountState.getNonce());
        }
        accountStateUnconfirmed = calNonceUnconfirmed(accountState, accountStateUnconfirmed);
        accountStateUnconfirmed = calAmountUnconfirmed(accountState, accountStateUnconfirmed);
        return accountStateUnconfirmed;
    }

    /**
     * 判断是否有需要更新的信息，有则需要进行锁定并进行处理返回
     *
     * @param accountState
     * @return
     */
    @Override
    public AccountStateUnconfirmed getUnconfirmedNonceReCal(AccountState accountState) {
        byte[] key = LedgerUtil.getKey(accountState.getAddress(), accountState.getAssetChainId(), accountState.getAssetId());
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getAccountStateUnconfirmed(accountState.getAddressChainId(), key);
        if (null == accountStateUnconfirmed) {
            return new AccountStateUnconfirmed(accountState.getAddress(), accountState.getAddressChainId(), accountState.getAssetChainId(), accountState.getAssetId(), accountState.getNonce());
        }
        accountStateUnconfirmed = calNonceUnconfirmed(accountState, accountStateUnconfirmed);
        return accountStateUnconfirmed;
    }

    @Override
    public void mergeUnconfirmedNonce(int addressChainId, String assetKey, List<UnconfirmedNonce> unconfirmedNonces, List<String> txHashList) {
        //获取未确认的列表
        synchronized (LockerUtil.getUnconfirmedAccountLocker(assetKey)) {
            try {
                AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getAccountStateUnconfirmed(addressChainId, assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                unconfirmedNonces.addAll(accountStateUnconfirmed.getUnconfirmedNonces());
                accountStateUnconfirmed.setUnconfirmedNonces(unconfirmedNonces);
                //未确认的交易金额直接做清空处理，逻辑后续可以继续做优化
                accountStateUnconfirmed.getUnconfirmedAmounts().clear();
                unconfirmedRepository.updateAccountStateUnconfirmed(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountStateUnconfirmed);
            } catch (Exception e) {
                e.printStackTrace();
                LoggerUtil.logger(addressChainId).error("@@@@mergeUnconfirmedNonce exception");
            }
        }
    }

    @Override
    public boolean rollUnconfirmTx(int addressChainId, String assetKey, String nonce, String txHash) {
        //账户处理锁
        synchronized (LockerUtil.getUnconfirmedAccountLocker(assetKey)) {
            try {
                AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedRepository.getAccountStateUnconfirmed(addressChainId, assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                List<UnconfirmedNonce> list = accountStateUnconfirmed.getUnconfirmedNonces();
                int i = 0;
                boolean hadRollNonce = rollUnconfirmedNonce(accountStateUnconfirmed, nonce);
                boolean hadRollAmount = rollUnconfirmedAmount(accountStateUnconfirmed, txHash);
                if (hadRollNonce || hadRollAmount) {
                    unconfirmedRepository.updateAccountStateUnconfirmed(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountStateUnconfirmed);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                LoggerUtil.logger(addressChainId).error("@@@@rollUnconfirmTx exception assetKey={},txHash={}", assetKey, txHash);
            }
            return false;
        }
    }

    private boolean rollUnconfirmedNonce(AccountStateUnconfirmed accountStateUnconfirmed, String nonce) {
        List<UnconfirmedNonce> list = accountStateUnconfirmed.getUnconfirmedNonces();
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
                LoggerUtil.logger(accountStateUnconfirmed.getAddressChainId()).debug("roll j={},nonce = {}", j, list.get(j - 1).getNonce());
                list.remove(j - 1);
            }

        }
        return hadRoll;
    }

    private boolean rollUnconfirmedAmount(AccountStateUnconfirmed accountStateUnconfirmed, String txHash) {
        List<UnconfirmedAmount> list = accountStateUnconfirmed.getUnconfirmedAmounts();
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
                LoggerUtil.logger(accountStateUnconfirmed.getAddressChainId()).debug("roll j={},hash = {}", j, list.get(j - 1).getTxHash());
                list.remove(j - 1);
            }

        }
        return hadRoll;
    }

    private AccountStateUnconfirmed calAmountUnconfirmed(AccountState accountState, AccountStateUnconfirmed accountStateUnconfirmed) {

        //更新数据库已确认信息
        List<UnconfirmedAmount> unconfirmedAmounts = CoinDataUtil.getUnconfirmedAmounts(accountState.getTxHash(), accountStateUnconfirmed.getUnconfirmedAmounts());
        accountStateUnconfirmed.setUnconfirmedAmounts(unconfirmedAmounts);
        //做超时的未确认交易清理
        accountStateUnconfirmed.updateUnconfirmeAmounts();
        return accountStateUnconfirmed;

    }

    private AccountStateUnconfirmed calNonceUnconfirmed(AccountState accountState, AccountStateUnconfirmed accountStateUnconfirmed) {
        String dbNonce = accountState.getNonce();
        String orgUnconfirmedNonces = null;
        //更新未确认交易nonce
        if (dbNonce.equalsIgnoreCase(LedgerConstant.INIT_NONCE) || dbNonce.equalsIgnoreCase(accountStateUnconfirmed.getDbNonce())) {
            //初始账户信息不做处理
        } else {
            orgUnconfirmedNonces = accountStateUnconfirmed.getUnconfirmedNoncesStrs();
            List<UnconfirmedNonce> unconfirmedNonces = CoinDataUtil.getUnconfirmedNonces(dbNonce, accountStateUnconfirmed.getUnconfirmedNonces());
            accountStateUnconfirmed.setDbNonce(dbNonce);
            accountStateUnconfirmed.setUnconfirmedNonces(unconfirmedNonces);
        }
        //做超时的未确认交易清理
        if (accountStateUnconfirmed.getUnconfirmedNonces().size() > 0) {
            if (LedgerUtil.isExpiredNonce(accountStateUnconfirmed.getUnconfirmedNonces().get(0))) {
                accountStateUnconfirmed.getUnconfirmedNonces().clear();
            }
        }
        return accountStateUnconfirmed;
    }
}


