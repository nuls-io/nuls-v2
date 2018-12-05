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
