package io.nuls.ledger.serializers;

import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.utils.ByteUtil;
import io.nuls.ledger.utils.RLP;
import io.nuls.ledger.utils.RLPList;
import io.nuls.tools.core.annotation.Component;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/30.
 */
@Component
public class FreezeLockTimeStateSerializer implements Serializer<FreezeLockTimeState, byte[]> {
    @Override
    public byte[] serialize(FreezeLockTimeState accountState) {
        byte[] txHash = RLP.encodeString(accountState.getTxHash());
        byte[] amount = RLP.encodeBigInteger(BigInteger.valueOf(accountState.getAmount()));
        byte[] lockTime = RLP.encodeBigInteger(BigInteger.valueOf(accountState.getLockTime()));
        byte[] createTime = RLP.encodeBigInteger(BigInteger.valueOf(accountState.getCreateTime()));
        return RLP.encodeList(txHash, amount, lockTime, createTime);
    }

    @Override
    public FreezeLockTimeState deserialize(byte[] stream) {
        if (stream == null || stream.length == 0) {
            return null;
        }
        FreezeLockTimeState state = new FreezeLockTimeState();
        try {
            RLPList decodedList = RLP.decode2(stream);
            RLPList items = (RLPList) decodedList.get(0);

            byte[] txHashBytes = items.get(0).getRLPData();
            byte[] amountBytes = items.get(1).getRLPData();
            byte[] lockTimeBytes = items.get(2).getRLPData();
            byte[] createTimeBytes = items.get(3).getRLPData();

            state.setTxHash(new String(txHashBytes));
            state.setAmount(ByteUtil.byteArrayToLong(amountBytes));
            state.setLockTime(ByteUtil.byteArrayToLong(lockTimeBytes));
            state.setCreateTime(ByteUtil.byteArrayToLong(createTimeBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
        return state;
    }
}
