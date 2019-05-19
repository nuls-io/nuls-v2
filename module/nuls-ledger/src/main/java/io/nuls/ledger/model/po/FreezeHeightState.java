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
package io.nuls.ledger.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * account balance lock
 * Created by wangkun23 on 2018/11/21.
 *
 * @author lanjinsheng
 */
public class FreezeHeightState extends BaseNulsData {

    /**
     * 交易的hash值
     */
    private String txHash;
    /**
     * 交易的nonce值
     */
    private byte[] nonce = LedgerConstant.getInitNonceByte();
    /**
     * 锁定金额
     */
    private BigInteger amount = BigInteger.ZERO;

    /**
     * 锁定高度
     */
    private long height = 0;

    private long createTime = 0;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(txHash);
        stream.write(nonce);
        stream.writeBigInteger(amount);
        stream.writeUint32(height);
        stream.writeUint48(createTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.txHash = byteBuffer.readString();
        this.nonce = byteBuffer.readBytes(8);
        this.amount = byteBuffer.readBigInteger();
        this.height = byteBuffer.readUint32();
        this.createTime = byteBuffer.readUint48();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(txHash);
        size += nonce.length;
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint48();
        return size;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
