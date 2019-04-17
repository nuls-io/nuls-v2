/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.ledger.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.ledger.utils.TimeUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author lan
 * @description
 * @date 2019/01/10
 **/
public class UnconfirmedAmount extends BaseNulsData {
    private long time;
    private String txHash;
    /**
     * locked == 0 时的 fromCoin花费金额
     */
    private BigInteger spendAmount = BigInteger.ZERO;
    /**
     * lockedTime == 0 时的 toCoin收入金额
     */
    private BigInteger earnAmount = BigInteger.ZERO;
    /**
     * locked != 0 时的 fromCoin 解锁花费金额
     */
    private BigInteger fromUnLockedAmount = BigInteger.ZERO;

    /**
     * lockedTime != 0 时的 toCoin 锁定收入金额
     */
    private BigInteger toLockedAmount = BigInteger.ZERO;

    public UnconfirmedAmount() {
        super();
    }

    public UnconfirmedAmount(BigInteger earn, BigInteger spend, BigInteger unLockedAmount, BigInteger lockedAmount) {
        spendAmount = spendAmount.add(spend);
        earnAmount = earnAmount.add(earn);
        fromUnLockedAmount = fromUnLockedAmount.add(unLockedAmount);
        toLockedAmount = toLockedAmount.add(lockedAmount);
        this.time = TimeUtil.getCurrentTime();
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint48(time);
        stream.writeString(txHash);
        stream.writeBigInteger(spendAmount);
        stream.writeBigInteger(earnAmount);
        stream.writeBigInteger(fromUnLockedAmount);
        stream.writeBigInteger(toLockedAmount);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.time = byteBuffer.readUint48();
        this.txHash = byteBuffer.readString();
        this.spendAmount = byteBuffer.readBigInteger();
        this.earnAmount = byteBuffer.readBigInteger();
        if (!byteBuffer.isFinished()) {
            //兼容下老数据，过阵子可以删除该判断20190306
            this.fromUnLockedAmount = byteBuffer.readBigInteger();
            this.toLockedAmount = byteBuffer.readBigInteger();
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfString(txHash);
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfBigInteger();
        return size;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public BigInteger getSpendAmount() {
        return spendAmount;
    }

    public void setSpendAmount(BigInteger spendAmount) {
        this.spendAmount = spendAmount;
    }

    public BigInteger getEarnAmount() {
        return earnAmount;
    }

    public void setEarnAmount(BigInteger earnAmount) {
        this.earnAmount = earnAmount;
    }

    public BigInteger getFromUnLockedAmount() {
        return fromUnLockedAmount;
    }

    public void setFromUnLockedAmount(BigInteger fromUnLockedAmount) {
        this.fromUnLockedAmount = fromUnLockedAmount;
    }

    public BigInteger getToLockedAmount() {
        return toLockedAmount;
    }

    public void setToLockedAmount(BigInteger toLockedAmount) {
        this.toLockedAmount = toLockedAmount;
    }
}
