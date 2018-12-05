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
package io.nuls.ledger.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.*;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountState extends BaseNulsData {

    @Setter
    @Getter
    private int chainId;

    @Setter
    @Getter
    private int assetId;

    @Setter
    @Getter
    private long nonce;

    @Setter
    @Getter
    private BigInteger balance = BigInteger.ZERO;

    /**
     * 账户总金额
     */
    private BigInteger totalAmount = BigInteger.ZERO;

    /**
     * 账户冻结的资产
     */
    @Setter
    @Getter
    private FreezeState freezeState;

    public AccountState(int chainId, int assetId, long nonce, BigInteger balance) {
        this.chainId = chainId;
        this.assetId = assetId;
        this.nonce = nonce;
        this.balance = balance;
        this.freezeState = new FreezeState();
    }

    /**
     * 获取账户总金额
     *
     * @return
     */
    public BigInteger getTotalAmount() {
        return balance.add(freezeState.getTotal());
    }

    public AccountState withNonce(long nonce) {
        return new AccountState(chainId, assetId, nonce, balance);
    }

    public AccountState withIncrementedNonce() {
        return new AccountState(chainId, assetId, nonce + 1, balance);
    }

    public AccountState withBalanceIncrement(BigInteger value) {
        return new AccountState(chainId, assetId, nonce, balance.add(value));
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeUint32(nonce);
        stream.writeBigInteger(balance);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.nonce = byteBuffer.readUint32();
        this.balance = byteBuffer.readBigInteger();
    }

    @Override
    public int size() {
        int size = 0;
        //chainId
        size += SerializeUtils.sizeOfInt16();
        //assetId
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfInt32();
        return size;
    }
}
