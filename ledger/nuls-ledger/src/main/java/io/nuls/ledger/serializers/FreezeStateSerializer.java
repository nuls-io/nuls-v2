package io.nuls.ledger.serializers;

import io.nuls.ledger.model.FreezeHeightState;
import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.model.FreezeState;
import io.nuls.ledger.utils.ByteUtil;
import io.nuls.ledger.utils.RLP;
import io.nuls.ledger.utils.RLPElement;
import io.nuls.ledger.utils.RLPList;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by wangkun23 on 2018/11/30.
 */
@Component
public class FreezeStateSerializer implements Serializer<FreezeState, byte[]> {


    @Autowired
    FreezeHeightStateSerializer freezeHeightStateSerializer;

    @Autowired
    FreezeLockTimeStateSerializer freezeLockTimeStateSerializer;

    @Override
    public byte[] serialize(FreezeState freezeState) {
        byte[] amount = RLP.encodeBigInteger(freezeState.getAmount());
        byte[] freezeHeightStates = getFreezeHeightStatesEncoded(freezeState.getFreezeHeightStates());
        byte[] freezeLockTimeStates = getFreezeLockTimeStatesEncoded(freezeState.getFreezeLockTimeStates());
        return RLP.encodeList(amount, freezeHeightStates, freezeLockTimeStates);
    }

    @Override
    public FreezeState deserialize(byte[] stream) {
        FreezeState freezeState = new FreezeState();
        try {
            RLPList decodedList = RLP.decode2(stream);
            RLPList items = (RLPList) decodedList.get(0);

            byte[] amountBytes = items.get(0).getRLPData();
            RLPList freezeHeightStatesList = (RLPList) items.get(1);
            RLPList freezeLockTimeStatesList = (RLPList) items.get(2);

            freezeState.setAmount(ByteUtil.bytesToBigInteger(amountBytes));

            for (RLPElement rawState : freezeHeightStatesList) {
                FreezeHeightState heightState = freezeHeightStateSerializer.deserialize(rawState.getRLPData());
                freezeState.getFreezeHeightStates().add(heightState);
            }

            for (RLPElement rawState : freezeLockTimeStatesList) {
                FreezeLockTimeState lockTimeState = freezeLockTimeStateSerializer.deserialize(rawState.getRLPData());
                freezeState.getFreezeLockTimeStates().add(lockTimeState);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error on parsing RLP", e);
        }
        return freezeState;
    }

    private byte[] getFreezeHeightStatesEncoded(List<FreezeHeightState> freezeHeightStates) {
        byte[][] encoded = new byte[freezeHeightStates.size()][];
        int i = 0;
        for (FreezeHeightState state : freezeHeightStates) {
            encoded[i] = freezeHeightStateSerializer.serialize(state);
            ++i;
        }
        return RLP.encodeList(encoded);
    }

    private byte[] getFreezeLockTimeStatesEncoded(List<FreezeLockTimeState> freezeLockTimeStates) {
        byte[][] encoded = new byte[freezeLockTimeStates.size()][];
        int i = 0;
        for (FreezeLockTimeState state : freezeLockTimeStates) {
            encoded[i] = freezeLockTimeStateSerializer.serialize(state);
            ++i;
        }
        return RLP.encodeList(encoded);
    }
}
