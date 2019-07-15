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

import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.sub.FreezeHeightState;
import io.nuls.ledger.model.po.sub.FreezeLockTimeState;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.ledger.storage.Repository;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangkun23 on 2018/12/4.
 * @author lanjinsheng
 */
@Service
public class FreezeStateServiceImpl implements FreezeStateService {
    @Autowired
    Repository repository;

    private BigInteger unFreezeLockTimeState(List<FreezeLockTimeState> timeList, AccountState accountState) {
        long nowTime = NulsDateUtils.getCurrentTimeSeconds();
        //可移除的时间锁列表
        List<FreezeLockTimeState> timeRemove = new ArrayList<>();
        timeList.sort((x, y) -> Long.compare(x.getLockTime(), y.getLockTime()));
        for (FreezeLockTimeState freezeLockTimeState : timeList) {
            if (freezeLockTimeState.getLockTime() <= nowTime) {
                //永久锁定的,继续处理
                if (freezeLockTimeState.getLockTime() == LedgerConstant.PERMANENT_LOCK) {
                    continue;
                }
                timeRemove.add(freezeLockTimeState);
            } else {
                //因为正序排列，所以可以跳出
                break;
            }
        }
        BigInteger addToAmount = BigInteger.ZERO;
        for (FreezeLockTimeState freezeLockTimeState : timeRemove) {
            timeList.remove(freezeLockTimeState);
            addToAmount = addToAmount.add(freezeLockTimeState.getAmount());
        }
        return addToAmount;
    }

    private BigInteger unFreezeLockHeightState(int addressChainId, List<FreezeHeightState> heightList, AccountState accountState) {
        //此处高度可以做个时间缓存
        long nowHeight = repository.getBlockHeight(addressChainId);
        //可移除的高度锁列表
        List<FreezeHeightState> heightRemove = new ArrayList<>();
        heightList.sort((x, y) -> Long.compare(x.getHeight(), y.getHeight()));
        for (FreezeHeightState freezeHeightState : heightList) {
            if (freezeHeightState.getHeight() <= nowHeight) {
                //时间到期，进行解锁
                heightRemove.add(freezeHeightState);
            } else {
                //因为正序排列，所以可以跳出
                break;
            }
        }
        BigInteger addToAmount = BigInteger.ZERO;
        for (FreezeHeightState freezeHeightState : heightRemove) {
            heightList.remove(freezeHeightState);
            addToAmount = addToAmount.add(freezeHeightState.getAmount());
        }
        return addToAmount;
    }

    /**
     * 释放账户的锁定记录
     *
     * @param addressChainId
     * @param accountState
     * @return
     */
    @Override
    public boolean recalculateFreeze(int addressChainId,AccountState accountState) {
        List<FreezeLockTimeState> timeList = accountState.getFreezeLockTimeStates();
        List<FreezeHeightState> heightList = accountState.getFreezeHeightStates();
        if (timeList.size() == 0 && heightList.size() == 0) {
            return true;
        }
        BigInteger addTimeAmount = unFreezeLockTimeState(timeList, accountState);
        BigInteger addHeightAmount = unFreezeLockHeightState(addressChainId,heightList, accountState);
        accountState.addTotalToAmount(addTimeAmount);
        accountState.addTotalToAmount(addHeightAmount);
        return true;
    }
}


