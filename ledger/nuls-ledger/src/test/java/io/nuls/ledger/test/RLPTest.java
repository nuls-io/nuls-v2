package io.nuls.ledger.test;

import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.model.FreezeHeightState;
import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.model.FreezeState;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/29.
 */
public class RLPTest {

    final Logger logger = LoggerFactory.getLogger(RLPTest.class);

    @Test
    public void rlp() {
        FreezeHeightState state = new FreezeHeightState();
        state.setTxHash("dfdf");
        state.setHeight(100L);
        state.setAmount(100L);
        state.setCreateTime(System.currentTimeMillis());

        logger.info("state rlp {}", state);

        FreezeHeightState state2 = new FreezeHeightState(state.getEncoded());

        logger.info("state rlp {}", state2);
    }

    @Test
    public void rlp2() {
        Integer chainId = 1;
        String address = "NsdzTe4czMVA5Ccc1p9tgiGrKWx7WLNV";
        Integer assetId = 1;
        AccountState accountState = new AccountState(chainId, assetId, 0, 0);

//        accountState.getFreezeState().getFreezeHeightStates().add(state);
        logger.info("rlp {}", accountState.getEncoded().length);
    }

    @Test
    public void rlp3() {
        FreezeLockTimeState state = new FreezeLockTimeState();
        state.setTxHash("dfdf");
        state.setLockTime(System.currentTimeMillis());
        state.setAmount(100L);
        state.setCreateTime(System.currentTimeMillis());

        logger.info("state rlp {}", state);

        FreezeLockTimeState state2 = new FreezeLockTimeState(state.getEncoded());

        logger.info("state rlp {}", state2);
    }

    @Test
    public void rlp4() {
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
        heightState.setAmount(100L);
        heightState.setCreateTime(System.currentTimeMillis());
        freezeState.getFreezeHeightStates().add(heightState);

        FreezeState freezeState2 = new FreezeState(freezeState.getEncoded());
        freezeState2.rlpParse();
        logger.info("freezeState2 {}", freezeState2);

    }
}
