package io.nuls.ledger.serializers;

import io.nuls.ledger.model.FreezeHeightState;
import io.nuls.ledger.utils.ByteUtil;
import io.nuls.ledger.utils.RLP;
import io.nuls.ledger.utils.RLPList;
import io.nuls.tools.core.annotation.Component;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/30.
 */
@Component
public class FreezeHeightStateSerializer implements Serializer<FreezeHeightState, byte[]>{

    @Override
    public byte[] serialize(FreezeHeightState state) {
        byte[] txHash = RLP.encodeString(state.getTxHash());
        byte[] amount = RLP.encodeBigInteger(state.getAmount());
        byte[] height = RLP.encodeBigInteger(BigInteger.valueOf(state.getHeight()));
        byte[] createTime = RLP.encodeBigInteger(BigInteger.valueOf(state.getCreateTime()));
        return RLP.encodeList(txHash, amount, height, createTime);
    }

    @Override
    public FreezeHeightState deserialize(byte[] stream) {
        if (stream == null || stream.length == 0) {
            return null;
        }
        FreezeHeightState state = new FreezeHeightState();
        try {
            RLPList decodedList = RLP.decode2(stream);
            RLPList items = (RLPList) decodedList.get(0);

            byte[] txHashBytes = items.get(0).getRLPData();
            byte[] amountBytes = items.get(1).getRLPData();
            byte[] heightBytes = items.get(2).getRLPData();
            byte[] createTimeBytes = items.get(3).getRLPData();

            state.setTxHash(new String(txHashBytes));
            state.setAmount(ByteUtil.bytesToBigInteger(amountBytes));
            state.setHeight(ByteUtil.byteArrayToLong(heightBytes));
            state.setCreateTime(ByteUtil.byteArrayToLong(createTimeBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
        return state;
    }
}
