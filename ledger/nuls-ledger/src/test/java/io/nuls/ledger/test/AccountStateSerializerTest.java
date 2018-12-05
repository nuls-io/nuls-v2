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
package io.nuls.ledger.test;

import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.model.FreezeHeightState;
import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.model.FreezeState;
import io.nuls.ledger.serializers.AccountStateSerializer;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/30.
 */

public class AccountStateSerializerTest extends BaseTest {

    final Logger logger = LoggerFactory.getLogger(AccountStateSerializerTest.class);

    @Test
    public void test() {
        AccountStateSerializer accountStateSerializer = SpringLiteContext.getBean(AccountStateSerializer.class);

        Integer chainId = 1;
        String address = "NsdzTe4czMVA5Ccc1p9tgiGrKWx7WLNV";
        Integer assetId = 1;
        FreezeState freezeState = new FreezeState();
        freezeState.setAmount(BigInteger.valueOf(100));

        FreezeLockTimeState state = new FreezeLockTimeState();
        state.setTxHash("dfdf");
        state.setLockTime(System.currentTimeMillis());
        state.setAmount(BigInteger.valueOf(100));
        state.setCreateTime(System.currentTimeMillis());
        freezeState.getFreezeLockTimeStates().add(state);


        FreezeLockTimeState state2 = new FreezeLockTimeState();
        state2.setTxHash("dfdf22222");
        state2.setLockTime(System.currentTimeMillis());
        state2.setAmount(BigInteger.valueOf(100));
        state2.setCreateTime(System.currentTimeMillis());

        freezeState.getFreezeLockTimeStates().add(state2);

        logger.info("rlp {}", freezeState);


        FreezeHeightState heightState = new FreezeHeightState();
        heightState.setTxHash("dfdf");
        heightState.setHeight(100L);
        heightState.setAmount(BigInteger.valueOf(900));
        heightState.setCreateTime(System.currentTimeMillis());
        freezeState.getFreezeHeightStates().add(heightState);


        AccountState accountState = new AccountState(chainId, assetId, 50, BigInteger.valueOf(100));
        accountState.setFreezeState(freezeState);


        byte[] source = accountStateSerializer.serialize(accountState);
        logger.info("accountState {}", accountState);
        AccountState accountState2 = accountStateSerializer.deserialize(source);
        logger.info("accountState2 {}", accountState2);
    }
}
