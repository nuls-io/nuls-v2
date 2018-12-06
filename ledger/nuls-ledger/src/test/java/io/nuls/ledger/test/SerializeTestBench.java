package io.nuls.ledger.test;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.serializers.FreezeLockTimeStateSerializer;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.thread.TimeService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;

/**
 * Created by wangkun23 on 2018/12/6.
 */
public class SerializeTestBench {
    final Logger logger = LoggerFactory.getLogger(SerializeTestBench.class);

    @Test
    public void test() throws IOException, NulsException {
        ECKey ecKey = new ECKey();
        //String hash = ecKey.getPrivKey().toString();
        String hash = "10056059455279212001966187311318411819374482224877967186032413783466000690485";
        logger.info("hash {}", hash);

        TimeService.getInstance().start();
        long time = TimeService.currentTimeMillis();

        FreezeLockTimeState state = new FreezeLockTimeState();
        state.setTxHash(hash);
        state.setAmount(BigInteger.valueOf(1000000000000L));
        state.setLockTime(time);
        state.setCreateTime(time);

        FreezeLockTimeStateSerializer stateSerializer = new FreezeLockTimeStateSerializer();
        byte[] bytes1 = stateSerializer.serialize(state);

        byte[] bytes2 = state.serialize();
        logger.info("bytes1 size {} , bytes2 size {}", bytes1.length, bytes2.length);

        FreezeLockTimeState state2 = stateSerializer.deserialize(bytes1);
        FreezeLockTimeState state3 = new FreezeLockTimeState();
        state3.parse(new NulsByteBuffer(bytes2));


        logger.info("state2 size {}", state2);
        logger.info("state3 size {}", state3);

    }

}
