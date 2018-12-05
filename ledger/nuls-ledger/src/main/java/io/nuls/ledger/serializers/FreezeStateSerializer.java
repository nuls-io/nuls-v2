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
