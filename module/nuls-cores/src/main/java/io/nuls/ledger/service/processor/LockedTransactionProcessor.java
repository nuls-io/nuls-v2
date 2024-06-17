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
package io.nuls.ledger.service.processor;

import io.nuls.base.data.Coin;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.sub.FreezeHeightState;
import io.nuls.ledger.model.po.sub.FreezeLockTimeState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LoggerUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Unlock transaction processing
 * Created by lanjinsheng on 2018/12/29.
 *
 * @author lanjinsheng
 */
@Component
public class LockedTransactionProcessor implements TxLockedProcessor {
    @Autowired
    AccountStateService accountStateService;
    @Autowired
    Repository repository;

    /**
     * During transactions, press Unlocking operation for time or height
     *
     * @param coin
     * @param txHash
     * @param accountState
     * @return
     */
    private boolean processFromCoinData(CoinFrom coin, String txHash, AccountState accountState, String address) {

        if (coin.getLocked() == LedgerConstant.UNLOCKED_TIME) {
            //Remove lock by time
            List<FreezeLockTimeState> list = accountState.getFreezeLockTimeStates();
            for (FreezeLockTimeState freezeLockTimeState : list) {
                LoggerUtil.COMMON_LOG.debug("processFromCoinData remove TimeUnlocked address={},amount={}={},nonce={}={},hash={} ", address, coin.getAmount(), freezeLockTimeState.getAmount(), LedgerUtil.getNonceEncode(coin.getNonce()), LedgerUtil.getNonceEncode(freezeLockTimeState.getNonce()), txHash);
                if (LedgerUtil.equalsNonces(freezeLockTimeState.getNonce(), coin.getNonce())) {
                    if (0 == freezeLockTimeState.getAmount().compareTo(coin.getAmount())) {
                        //Consistent amount, removed
                        list.remove(freezeLockTimeState);
                        LoggerUtil.COMMON_LOG.debug("TimeUnlocked remove ok,hash={} ", txHash);
                        return true;
                    }
                }
            }

        } else {
            //Remove lock by height
            List<FreezeHeightState> list = accountState.getFreezeHeightStates();
            for (FreezeHeightState freezeHeightState : list) {
                Log.debug("processFromCoinData remove HeightUnlocked address={},amount={}={},nonce={}={},hash={} ", address, coin.getAmount(), freezeHeightState.getAmount(), LedgerUtil.getNonceEncode(coin.getNonce()), LedgerUtil.getNonceEncode(freezeHeightState.getNonce()), txHash);
                if (LedgerUtil.equalsNonces(freezeHeightState.getNonce(), coin.getNonce())) {
                    if (0 == freezeHeightState.getAmount().compareTo(coin.getAmount())) {
                        //Consistent amount, removed
                        list.remove(freezeHeightState);
                        LoggerUtil.COMMON_LOG.debug("HeightUnlocked remove ok,hash={} ", txHash);
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * During transactions, press Time or height locking operation
     *
     * @param coin
     * @param nonce
     * @param hash
     */
    private boolean processToCoinData(CoinTo coin, byte[] nonce, String hash, AccountState accountState, long txTime, String address) {
        if (coin.getLockTime() < LedgerConstant.MAX_HEIGHT_VALUE && !LedgerUtil.isPermanentLock(coin.getLockTime())) {
            //Lock by height
            FreezeHeightState freezeHeightState = new FreezeHeightState();
            freezeHeightState.setAmount(coin.getAmount());
            freezeHeightState.setCreateTime(txTime);
            freezeHeightState.setHeight(coin.getLockTime());
            freezeHeightState.setNonce(nonce);
            freezeHeightState.setTxHash(hash);
            LoggerUtil.COMMON_LOG.debug("processToCoinData add HeightLocked address={},amount={},height={},hash={} ", address, freezeHeightState.getAmount(), freezeHeightState.getHeight(), hash);
            accountState.getFreezeHeightStates().add(freezeHeightState);
        } else {
            //Lock by time
            FreezeLockTimeState freezeLockTimeState = new FreezeLockTimeState();
            freezeLockTimeState.setAmount(coin.getAmount());
            freezeLockTimeState.setCreateTime(txTime);
            freezeLockTimeState.setLockTime(coin.getLockTime());
            freezeLockTimeState.setNonce(nonce);
            freezeLockTimeState.setTxHash(hash);
            LoggerUtil.COMMON_LOG.debug("processToCoinData add TimeLocked address={},amount={},time={},hash={} ", address, coin.getAmount(), freezeLockTimeState.getLockTime(), hash);
            accountState.getFreezeLockTimeStates().add(freezeLockTimeState);
        }
        return true;
    }

    /**
     * Perform cache locking and unlocking processing on blocks
     *
     * @param coin
     * @param nonce
     * @param txHash
     * @param timeStateList
     * @param heightStateList
     * @param address
     * @param isFromCoin
     * @return
     */
    @Override
    public boolean processCoinData(Coin coin, byte[] nonce, String txHash, List<FreezeLockTimeState> timeStateList,
                                   List<FreezeHeightState> heightStateList, String address, boolean isFromCoin) {

        if (isFromCoin) {
            CoinFrom coinFrom = (CoinFrom) coin;
            if (coinFrom.getLocked() == LedgerConstant.UNLOCKED_TIME) {
                //Remove lock by time
                for (FreezeLockTimeState freezeLockTimeState : timeStateList) {
                    LoggerUtil.COMMON_LOG.debug("processFromCoinData remove TimeUnlocked address={},amount={}={},nonce={}={},hash={} ", address, coin.getAmount(), freezeLockTimeState.getAmount(), LedgerUtil.getNonceEncode(coinFrom.getNonce()), LedgerUtil.getNonceEncode(freezeLockTimeState.getNonce()), txHash);
                    if (LedgerUtil.equalsNonces(freezeLockTimeState.getNonce(), coinFrom.getNonce())) {
                        if (0 == freezeLockTimeState.getAmount().compareTo(coin.getAmount())) {
                            //Consistent amount, removed
                            timeStateList.remove(freezeLockTimeState);
                            LoggerUtil.COMMON_LOG.debug("TimeUnlocked remove ok,hash={} ", txHash);
                            return true;
                        }
                    }
                }

            } else {
                //Remove lock by height
                for (FreezeHeightState freezeHeightState : heightStateList) {
                    Log.debug("processFromCoinData remove HeightUnlocked address={},amount={}={},nonce={}={},hash={} ", address, coin.getAmount(), freezeHeightState.getAmount(), LedgerUtil.getNonceEncode(coinFrom.getNonce()), LedgerUtil.getNonceEncode(freezeHeightState.getNonce()), txHash);
                    if (LedgerUtil.equalsNonces(freezeHeightState.getNonce(), coinFrom.getNonce())) {
                        if (0 == freezeHeightState.getAmount().compareTo(coin.getAmount())) {
                            //Consistent amount, removed
                            heightStateList.remove(freezeHeightState);
                            LoggerUtil.COMMON_LOG.debug("HeightUnlocked remove ok,hash={} ", txHash);
                            return true;
                        }
                    }
                }
            }
        } else {
            CoinTo coinTo = (CoinTo) coin;
            if (coinTo.getLockTime() < LedgerConstant.MAX_HEIGHT_VALUE && !LedgerUtil.isPermanentLock(coinTo.getLockTime())) {
                //Lock by height
                FreezeHeightState freezeHeightState = new FreezeHeightState();
                freezeHeightState.setAmount(coin.getAmount());
                freezeHeightState.setHeight(coinTo.getLockTime());
                freezeHeightState.setNonce(nonce);
                freezeHeightState.setTxHash(txHash);
                LoggerUtil.COMMON_LOG.debug("processToCoinData add HeightLocked address={},amount={},height={},hash={} ", address, freezeHeightState.getAmount(), freezeHeightState.getHeight(), txHash);
                heightStateList.add(freezeHeightState);
            } else {
                //Lock by time
                FreezeLockTimeState freezeLockTimeState = new FreezeLockTimeState();
                freezeLockTimeState.setAmount(coin.getAmount());
                freezeLockTimeState.setLockTime(coinTo.getLockTime());
                freezeLockTimeState.setNonce(nonce);
                freezeLockTimeState.setTxHash(txHash);
                LoggerUtil.COMMON_LOG.debug("processToCoinData add TimeLocked address={},amount={},time={},hash={} ", address, coin.getAmount(), freezeLockTimeState.getLockTime(), txHash);
                timeStateList.add(freezeLockTimeState);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean processCoinData(Coin coin, byte[] nonce, String txHash, AccountState accountState, long txTime, String address, boolean isFromCoin) {
        if (isFromCoin) {
            return processFromCoinData((CoinFrom) coin, txHash, accountState, address);
        } else {
            return processToCoinData((CoinTo) coin, nonce, txHash, accountState, txTime, address);
        }
    }

}
