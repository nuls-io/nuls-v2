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
        freezeState.setAmount(100L);

        FreezeLockTimeState state = new FreezeLockTimeState();
        state.setTxHash("dfdf");
        state.setLockTime(System.currentTimeMillis());
        state.setAmount(100L);
        state.setCreateTime(System.currentTimeMillis());
        freezeState.getFreezeLockTimeStates().add(state);


        FreezeLockTimeState state2 = new FreezeLockTimeState();
        state2.setTxHash("dfdf22222");
        state2.setLockTime(System.currentTimeMillis());
        state2.setAmount(200L);
        state2.setCreateTime(System.currentTimeMillis());

        freezeState.getFreezeLockTimeStates().add(state2);

        logger.info("rlp {}", freezeState);


        FreezeHeightState heightState = new FreezeHeightState();
        heightState.setTxHash("dfdf");
        heightState.setHeight(100L);
        heightState.setAmount(900L);
        heightState.setCreateTime(System.currentTimeMillis());
        freezeState.getFreezeHeightStates().add(heightState);


        AccountState accountState = new AccountState(chainId, assetId, 50, 70);
        accountState.setFreezeState(freezeState);


        byte[] source = accountStateSerializer.serialize(accountState);
        logger.info("accountState {}", accountState);
        AccountState accountState2 = accountStateSerializer.deserialize(source);
        logger.info("accountState2 {}", accountState2);
    }
}
