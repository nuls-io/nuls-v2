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
import io.nuls.core.rpc.util.TimeUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.model.po.*;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.ledger.utils.LedgerUtil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void rollAccountState(String assetKey, AccountStateSnapshot accountStateSnapshot) throws Exception {
        //获取当前数据库值
        Map<String, TxUnconfirmed> unconfirmedNonces = new HashMap<>(64);
        AccountState accountState = accountStateSnapshot.getAccountState();
        AccountStateUnconfirmed accountStateUnconfirmed = new AccountStateUnconfirmed();
        List<AmountNonce> list = accountStateSnapshot.getNonces();
        BigInteger amount = BigInteger.ZERO;
        for (AmountNonce amountNonce : list) {
            TxUnconfirmed txUnconfirmed = new TxUnconfirmed(accountState.getAddress(), accountState.getAssetChainId(), accountState.getAssetId(),
                    amountNonce.getFromNonce(), amountNonce.getNonce(), amountNonce.getAmount());
            unconfirmedNonces.put(LedgerUtil.getNonceEncode(amountNonce.getNonce()), txUnconfirmed);
            amount.add(amountNonce.getAmount());
        }

        repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountState);
        //进行nonce的回退合并处理
        if (unconfirmedNonces.size() > 0) {
            accountStateUnconfirmed.setNonce(list.get(list.size() - 1).getNonce());
            accountStateUnconfirmed.setFromNonce(list.get(list.size() - 1).getFromNonce());
            accountStateUnconfirmed.setUnconfirmedAmount(amount);
            accountStateUnconfirmed.setCreateTime(TimeUtils.getCurrentTimeSeconds());
            unconfirmedStateService.mergeUnconfirmedNonce(accountStateSnapshot.getAccountState(), assetKey, unconfirmedNonces, accountStateUnconfirmed);
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
    public AccountState getAccountState(String address, int addressChainId, int assetChainId, int assetId) {
        byte[] key = LedgerUtil.getKey(address, assetChainId, assetId);
        AccountState accountState = repository.getAccountState(addressChainId, key);
        if (null == accountState) {
            accountState = new AccountState(address, addressChainId, assetChainId, assetId, LedgerConstant.getInitNonceByte());
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
        //账户处理锁
        byte[] key = LedgerUtil.getKey(address, assetChainId, assetId);
        AccountState accountState = repository.getAccountState(addressChainId, key);
        if (null == accountState) {
            accountState = new AccountState(address, addressChainId, assetChainId, assetId, LedgerConstant.getInitNonceByte());
        } else {
            //解冻时间高度锁
            if (accountState.timeAllow()) {
                freezeStateService.recalculateFreeze(accountState);
                accountState.setLatestUnFreezeTime(TimeUtils.getCurrentTimeSeconds());
            }
        }
        return accountState;
    }


}
