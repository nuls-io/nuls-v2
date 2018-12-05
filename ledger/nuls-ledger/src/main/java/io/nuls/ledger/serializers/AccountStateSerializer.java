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

import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.utils.ByteUtil;
import io.nuls.ledger.utils.RLP;
import io.nuls.ledger.utils.RLPList;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.math.BigInteger;

/**
 * account state serializer
 * Created by wangkun23 on 2018/11/30.
 */
@Component
public class AccountStateSerializer implements Serializer<AccountState, byte[]> {

    @Autowired
    FreezeStateSerializer freezeStateSerializer;

    @Override
    public byte[] serialize(AccountState accountState) {
        byte[] chainId = RLP.encodeInt(accountState.getChainId());
        byte[] assetId = RLP.encodeInt(accountState.getAssetId());
        byte[] nonce = RLP.encodeBigInteger(BigInteger.valueOf(accountState.getNonce()));
        byte[] balance = RLP.encodeBigInteger(accountState.getBalance());
        byte[] freezeState = freezeStateSerializer.serialize(accountState.getFreezeState());
        return RLP.encodeList(chainId, assetId, nonce, balance, freezeState);
    }

    @Override
    public AccountState deserialize(byte[] stream) {
        if (stream == null || stream.length == 0) {
            return null;
        }

        AccountState accountState = new AccountState();
        RLPList items = (RLPList) RLP.decode2(stream).get(0);

        accountState.setChainId(ByteUtil.byteArrayToInt(items.get(0).getRLPData()));
        accountState.setAssetId(ByteUtil.byteArrayToInt(items.get(1).getRLPData()));
        accountState.setNonce(ByteUtil.byteArrayToLong(items.get(2).getRLPData()));
        accountState.setBalance(ByteUtil.bytesToBigInteger(items.get(3).getRLPData()));
        accountState.setFreezeState(freezeStateSerializer.deserialize(items.get(4).getRLPData()));

        return accountState;
    }
}
