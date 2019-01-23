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
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.math.BigInteger;

/**
 * account balance lock
 * Created by wangkun23 on 2018/11/21.
 */
@ToString
@NoArgsConstructor
public class FreezeHeightState extends BaseNulsData {

    /**
     * 交易的hash值
     */
    @Setter
    @Getter
    private String txHash;
    /**
     * 交易的nonce值
     */
    @Setter
    @Getter
    private String nonce;
    /**
     * 锁定金额
     */
    @Setter
    @Getter
    private BigInteger amount;

    /**
     * 锁定高度
     */
    @Setter
    @Getter
    private long height;

    @Setter
    @Getter
    private long createTime;


    public FreezeHeightState(byte[] stream) {
        NulsByteBuffer buffer = new NulsByteBuffer(stream);
        try {
            parse(buffer);
        } catch (NulsException e) {
            Log.error("", e);
        }
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(txHash);
        stream.writeString(nonce);
        stream.writeBigInteger(amount);
        stream.writeUint32(height);
        stream.writeUint48(createTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.txHash = byteBuffer.readString();
        this.nonce = byteBuffer.readString();
        this.amount = byteBuffer.readBigInteger();
        this.height = byteBuffer.readUint32();
        this.createTime = byteBuffer.readUint48();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(txHash);
        size += SerializeUtils.sizeOfString(nonce);
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint48();
        return size;
    }
}
