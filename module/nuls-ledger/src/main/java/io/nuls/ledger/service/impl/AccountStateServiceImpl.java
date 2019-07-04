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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.model.po.*;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.ledger.utils.CoinDataUtil;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.ArrayList;
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
    public void rollAccountState(int chainId, List<AccountStateSnapshot> preAccountStates) throws Exception {
        //获取当前数据库值
        Map<byte[], byte[]> accountStates = new HashMap<>(1024);
        for (AccountStateSnapshot accountStateSnapshot : preAccountStates) {
            String assetKey = LedgerUtil.getKeyStr(accountStateSnapshot.getBakAccountState().getAddress(),
                    accountStateSnapshot.getBakAccountState().getAssetChainId(), accountStateSnapshot.getBakAccountState().getAssetId());
            accountStates.put(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountStateSnapshot.getBakAccountState().getAccountState().serialize());
            //获取当前数据库值
            Map<String, TxUnconfirmed> unconfirmedNonces = new HashMap<>(64);
            BakAccountState bakAccountState = accountStateSnapshot.getBakAccountState();
            AccountStateUnconfirmed accountStateUnconfirmed = new AccountStateUnconfirmed();
            List<AmountNonce> list = accountStateSnapshot.getNonces();
            BigInteger amount = BigInteger.ZERO;
            for (AmountNonce amountNonce : list) {
                TxUnconfirmed txUnconfirmed = new TxUnconfirmed(bakAccountState.getAddress(), bakAccountState.getAssetChainId(), bakAccountState.getAssetId(),
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
                unconfirmedStateService.mergeUnconfirmedNonce(chainId, accountStateSnapshot.getBakAccountState().getAccountState(), assetKey, unconfirmedNonces, accountStateUnconfirmed);
            }
        }
        if (accountStates.size() > 0) {
            repository.batchUpdateAccountState(chainId, accountStates);
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
        //账户处理锁
        byte[] key = LedgerUtil.getKey(address, assetChainId, assetId);
        AccountState accountState = repository.getAccountState(addressChainId, key);
        if (null == accountState) {
            accountState = new AccountState(LedgerConstant.getInitNonceByte());
        } else {
            //解冻时间高度锁
            if (accountState.timeAllow()) {
                freezeStateService.recalculateFreeze(addressChainId, accountState);
                accountState.setLatestUnFreezeTime(NulsDateUtils.getCurrentTimeSeconds());
            }
        }
        return accountState;
    }

    private void parseCoins(Coin coin, List<byte[]> accountKeys, Map<String, Integer> existAccounts) {
        String address = AddressTool.getStringAddressByBytes(coin.getAddress());
        String keyStr = LedgerUtil.getKeyStr(address, coin.getAssetsChainId(), coin.getAssetsId());
        if (null != existAccounts.get(keyStr)) {
            return;
        }
        byte[] key = LedgerUtil.getKey(address, coin.getAssetsChainId(), coin.getAssetsId());
        accountKeys.add(key);
        existAccounts.put(keyStr, 1);
    }

    @Override
    public AccountState getAccountStateReCalByMap(int addressChainId, String key, Map<String, AccountState> accounts) {
        AccountState accountState = accounts.get(key);
        if (null == accountState) {
            accountState = new AccountState(LedgerConstant.getInitNonceByte());
            accounts.put(key, accountState);
        } else {
            //解冻时间高度锁
            if (accountState.timeAllow()) {
                freezeStateService.recalculateFreeze(addressChainId, accountState);
                accountState.setLatestUnFreezeTime(NulsDateUtils.getCurrentTimeSeconds());
            }
        }
        return accountState;
    }


    @Override
    public void buildAccountStateMap(int addressChainId, List<Transaction> txs, Map<String, AccountState> accounts, Map<String, CoinData> coinDatas) throws NulsException {
        List<byte[]> accountKeys = new ArrayList<>();
        Map<String, Integer> existAccounts = new HashMap<>();
        for (Transaction tx : txs) {
            String txHash = tx.getHash().toHex();
            CoinData coinData = CoinDataUtil.parseCoinData(tx.getCoinData());
            if (null != coinData) {
                coinDatas.put(txHash, coinData);
            }else{
                continue;
            }
            List<CoinFrom> coinFroms = coinData.getFrom();
            List<CoinTo> coinTos = coinData.getTo();
            for (Coin coin : coinFroms) {
                parseCoins(coin, accountKeys, existAccounts);
            }
            for (Coin coin : coinTos) {
                parseCoins(coin, accountKeys, existAccounts);
            }
        }
        try {
            if (accountKeys.size() > 0) {
                //批量查库
                Map<byte[], byte[]> bytesAccounts = repository.getAccountStates(addressChainId, accountKeys);
                for (Map.Entry<byte[], byte[]> entry : bytesAccounts.entrySet()) {
                    //缓存数据
                    AccountState accountState = new AccountState();
                    accountState.parse(new NulsByteBuffer(entry.getValue()));
                    accounts.put(ByteUtils.asString(entry.getKey()), accountState);
                }
            }
        } catch (Exception e) {
            LoggerUtil.logger(addressChainId).error(e);
            throw new NulsException(e);
        }
    }

}
