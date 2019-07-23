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

import java.util.List;

/**
 * 解锁交易处理
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
     * 交易中按 时间或高度的解锁操作
     *
     * @param coin
     * @param nonce
     * @param txHash
     * @param accountState
     * @return
     */
    @Override
    public boolean processFromCoinData(CoinFrom coin, byte[] nonce, String txHash, AccountState accountState, String address) {

        if (coin.getLocked() == -1) {
            //按时间移除锁定
            List<FreezeLockTimeState> list = accountState.getFreezeLockTimeStates();
            for (FreezeLockTimeState freezeLockTimeState : list) {
                LoggerUtil.COMMON_LOG.debug("processFromCoinData remove TimeUnlocked address={},amount={}={},nonce={}={},hash={} ", address, coin.getAmount(), freezeLockTimeState.getAmount(), LedgerUtil.getNonceEncode(coin.getNonce()), freezeLockTimeState.getNonce(), txHash);
                if (LedgerUtil.equalsNonces(freezeLockTimeState.getNonce(), coin.getNonce())) {
                    if (0 == freezeLockTimeState.getAmount().compareTo(coin.getAmount())) {
                        //金额一致，移除
                        list.remove(freezeLockTimeState);
                        LoggerUtil.COMMON_LOG.debug("TimeUnlocked remove ok,hash={} ", txHash);
                        return true;
                    }
                }
            }

        } else {
            //按高度移除锁定
            List<FreezeHeightState> list = accountState.getFreezeHeightStates();
            for (FreezeHeightState freezeHeightState : list) {
                Log.debug("processFromCoinData remove HeightUnlocked address={},amount={}={},nonce={}={},hash={} ", address, coin.getAmount(), freezeHeightState.getAmount(), LedgerUtil.getNonceEncode(coin.getNonce()), freezeHeightState.getNonce(), txHash);
                if (LedgerUtil.equalsNonces(freezeHeightState.getNonce(), coin.getNonce())) {
                    if (0 == freezeHeightState.getAmount().compareTo(coin.getAmount())) {
                        //金额一致，移除
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
     * 交易中按 时间或者高度的锁定操作
     *
     * @param coin
     * @param nonce
     * @param hash
     */
    @Override
    public boolean processToCoinData(CoinTo coin, byte[] nonce, String hash, AccountState accountState, long txTime, String address) {
        if (coin.getLockTime() < LedgerConstant.MAX_HEIGHT_VALUE && coin.getLockTime() != -1) {
            //按高度锁定
            FreezeHeightState freezeHeightState = new FreezeHeightState();
            freezeHeightState.setAmount(coin.getAmount());
            freezeHeightState.setCreateTime(txTime);
            freezeHeightState.setHeight(coin.getLockTime());
            freezeHeightState.setNonce(nonce);
            freezeHeightState.setTxHash(hash);
            LoggerUtil.COMMON_LOG.debug("processToCoinData add HeightLocked address={},amount={},height={},hash={} ", address, freezeHeightState.getAmount(), freezeHeightState.getHeight(), hash);
            accountState.getFreezeHeightStates().add(freezeHeightState);
        } else {
            //按时间锁定
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
    public static void main(String []args){
        System.out.println((int)(System.currentTimeMillis()/1000));
    }
}
