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
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;
import io.nuls.ledger.model.po.sub.AccountStateSnapshot;
import io.nuls.ledger.model.po.sub.AmountNonce;
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
@Component
public class AccountStateServiceImpl implements AccountStateService {

    @Autowired
    private Repository repository;
    @Autowired
    private UnconfirmedRepository unconfirmedRepository;
    @Autowired
    private FreezeStateService freezeStateService;
    @Autowired
    private UnconfirmedStateService unconfirmedStateService;
    @Autowired
    LedgerChainManager ledgerChainManager;

    @Override
    public void rollAccountState(int chainId, List<AccountStateSnapshot> preAccountStates) throws Exception {
        //获取当前数据库值
        Map<byte[], byte[]> accountStates = new HashMap<>(preAccountStates.size());
        Map<String, AccountState> accountStatesMem = new HashMap<>(preAccountStates.size());
        for (AccountStateSnapshot accountStateSnapshot : preAccountStates) {
            String assetKey = LedgerUtil.getKeyStr(accountStateSnapshot.getAddress(),
                    accountStateSnapshot.getAssetChainId(), accountStateSnapshot.getAssetId());
            accountStates.put(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountStateSnapshot.getAccountState().serialize());
            accountStatesMem.put(assetKey, accountStateSnapshot.getAccountState());
            //获取当前数据库值
            Map<String, TxUnconfirmed> unconfirmedNonces = new HashMap<>(64);
            AccountStateUnconfirmed accountStateUnconfirmed = new AccountStateUnconfirmed();
            List<AmountNonce> list = accountStateSnapshot.getNonces();
            BigInteger amount = BigInteger.ZERO;
            for (AmountNonce amountNonce : list) {
                TxUnconfirmed txUnconfirmed = new TxUnconfirmed(accountStateSnapshot.getAddress(), accountStateSnapshot.getAssetChainId(), accountStateSnapshot.getAssetId(),
                        amountNonce.getFromNonce(), amountNonce.getNonce(), amountNonce.getAmount());
                unconfirmedNonces.put(LedgerUtil.getNonceEncode(amountNonce.getNonce()), txUnconfirmed);
                amount.add(amountNonce.getAmount());
            }
            //进行nonce的回退合并处理
            if (unconfirmedNonces.size() > 0) {
                accountStateUnconfirmed.setNonce(list.get(list.size() - 1).getNonce());
                accountStateUnconfirmed.setFromNonce(list.get(list.size() - 1).getFromNonce());
                accountStateUnconfirmed.setUnconfirmedAmount(amount);
                accountStateUnconfirmed.setCreateTime(NulsDateUtils.getCurrentTimeSeconds());
                unconfirmedStateService.mergeUnconfirmedNonce(chainId, accountStateSnapshot.getAccountState(), assetKey, unconfirmedNonces, accountStateUnconfirmed);
            }
        }
        if (accountStates.size() > 0) {
            repository.batchUpdateAccountState(chainId, accountStates, accountStatesMem);
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
        //尝试缓存获取
        AccountState accountState = repository.getAccountStateByMemory(addressChainId, LedgerUtil.getKeyStr(address, assetChainId, assetId));
        if (null != accountState) {
            return accountState;
        }
        byte[] key = LedgerUtil.getKey(address, assetChainId, assetId);
        accountState = repository.getAccountState(addressChainId, key);
        if (null == accountState) {
            accountState = new AccountState(LedgerConstant.getInitNonceByte());
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
        //尝试缓存获取
        AccountState accountState = repository.getAccountStateByMemory(addressChainId, LedgerUtil.getKeyStr(address, assetChainId, assetId));
        if (null == accountState) {
            //账户处理锁
            byte[] key = LedgerUtil.getKey(address, assetChainId, assetId);
            accountState = repository.getAccountState(addressChainId, key);
            if (null == accountState) {
                accountState = new AccountState(LedgerConstant.getInitNonceByte());
                return accountState;
            }
        }
        //解冻时间高度锁
        if (accountState.timeAllow()) {
            freezeStateService.recalculateFreeze(addressChainId, accountState);
            accountState.setLatestUnFreezeTime(NulsDateUtils.getCurrentTimeSeconds());
        }
        return accountState;
    }


}
