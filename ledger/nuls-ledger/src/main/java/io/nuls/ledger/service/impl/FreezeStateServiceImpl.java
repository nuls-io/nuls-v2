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
import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.FreezeHeightState;
import io.nuls.ledger.model.po.FreezeLockTimeState;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.ledger.utils.TimeUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangkun23 on 2018/12/4.
 */
@Service
public class FreezeStateServiceImpl implements FreezeStateService {
    @Autowired
    Repository repository;
    private boolean timeAllow(long latestUnfreezeTime){
        //是否改为网络时间？
        long nowTime = TimeUtils.getCurrentTime();
        if(nowTime - latestUnfreezeTime > LedgerConstant.TIME_RECALCULATE_FREEZE){
            //解锁时间超时了,进行重新计算
            return true;
        }
        return false;
    }
    private BigInteger unFreezeLockTimeState(List<FreezeLockTimeState> timeList,AccountState accountState){
        long nowTime = TimeUtils.getCurrentTime();
        //可移除的时间锁列表
        List<FreezeLockTimeState>  timeRemove =  new ArrayList<>();
        timeList.sort((x, y) -> Long.compare(x.getLockTime(),y.getLockTime()));
        for(FreezeLockTimeState freezeLockTimeState : timeList){
            if(freezeLockTimeState.getLockTime() >= nowTime){
                //永久锁定的,继续处理
                if(freezeLockTimeState.getLockTime() == LedgerConstant.PERMANENT_LOCK){
                    continue;
                }
                timeRemove.add(freezeLockTimeState);
            }else{
                //因为正序排列，所以可以跳出
                break;
            }
        }
        BigInteger addFromAmount = BigInteger.ZERO;
        for(FreezeLockTimeState freezeLockTimeState : timeRemove){
            timeList.remove(freezeLockTimeState);
            addFromAmount.add(freezeLockTimeState.getAmount());
        }
        return addFromAmount;
    }
    private BigInteger unFreezeLockHeightState(List<FreezeHeightState> heightList,AccountState accountState){
        long nowHeight = repository.getBlockHeight(accountState.getAddressChainId());
        //可移除的高度锁列表
        List<FreezeHeightState>  heightRemove =  new ArrayList<>();
        heightList.sort((x, y) -> Long.compare(x.getHeight(),y.getHeight()));
        for(FreezeHeightState freezeHeightState : heightList){
            if(freezeHeightState.getHeight()  <= nowHeight){
                //时间到期，进行解锁
                heightRemove.add(freezeHeightState);
            }else{
                //因为正序排列，所以可以跳出
                break;
            }
        }
        BigInteger addFromAmount = BigInteger.ZERO;
        for(FreezeHeightState freezeHeightState : heightRemove){
            heightList.remove(freezeHeightState);
            addFromAmount.add(freezeHeightState.getAmount());
        }
        return addFromAmount;
    }

    /**
     * 释放账户的锁定记录
     * @param accountState
     * @return
     */
    @Override
    public boolean recalculateFreeze(AccountState accountState) {
        if (timeAllow(accountState.getLatestUnFreezeTime())) {
            List<FreezeLockTimeState> timeList = accountState.getFreezeLockTimeStates();
            List<FreezeHeightState> heightList = accountState.getFreezeHeightStates();
            if (timeList.size() == 0 && heightList.size() == 0) {
                accountState.setLatestUnFreezeTime(TimeUtils.getCurrentTime());
                return true;
            }
            BigInteger addTimeAmount = unFreezeLockTimeState(timeList, accountState);
            BigInteger addHeightAmount = unFreezeLockHeightState(heightList, accountState);
            accountState.addTotalFromAmount(addTimeAmount);
            accountState.addTotalFromAmount(addHeightAmount);
            accountState.setLatestUnFreezeTime(TimeUtils.getCurrentTime());
        }else{
            return false;
        }
        return true;
    }
}


