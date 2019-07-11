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
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.ledger.constant.LedgerConstant;

import java.io.IOException;
import java.math.BigInteger;

/**
 *
 *  1.AmountNonce 金额与nonce的对象，用户备份账户资产信息的存储。
 *  2.在 AccountStateSnapshot中进行引用。
 *
 * @author lanjinsheng
 * @date 2018/11/19
 */

public class AmountNonce extends BaseNulsData {
    /**
     * 上一笔的nonce值
     */
    private byte[] fromNonce = LedgerConstant.getInitNonceByte();
    /**
     * 当前的nonce值
     */
    private byte[] nonce = LedgerConstant.getInitNonceByte();
    /**
     * 该nonce消费金额
     */
    private BigInteger amount = BigInteger.ZERO;


    public AmountNonce() {
        super();
    }

    public AmountNonce(byte[] pFromNonce,byte[] pNonce, BigInteger amount) {
        System.arraycopy(pFromNonce, 0, fromNonce, 0, LedgerConstant.NONCE_LENGHT);
        System.arraycopy(pNonce, 0, nonce, 0, LedgerConstant.NONCE_LENGHT);
        this.amount = amount;
    }


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(fromNonce);
        stream.write(nonce);
        stream.writeBigInteger(amount);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.fromNonce = byteBuffer.readBytes(LedgerConstant.NONCE_LENGHT);
        this.nonce = byteBuffer.readBytes(LedgerConstant.NONCE_LENGHT);
        this.amount = byteBuffer.readBigInteger();
    }

    @Override
    public int size() {
        int size = 0;
        size += fromNonce.length;
        size += nonce.length;
        size += SerializeUtils.sizeOfBigInteger();
        return size;
    }
    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public byte[] getFromNonce() {
        return fromNonce;
    }

    public void setFromNonce(byte[] fromNonce) {
        this.fromNonce = fromNonce;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }
}
