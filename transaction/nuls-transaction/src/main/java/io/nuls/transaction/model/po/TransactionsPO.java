/**
 * MIT License
 * <p>
 * Copyright (c) 2018-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.transaction.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.constant.TxStatusEnum;
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.constant.ToolsConstant;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.EncryptedData;
import io.nuls.tools.crypto.UnsafeByteArrayOutputStream;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.constant.TxConstant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * @author: qinyifeng
 * @date: 2019/01/24
 */
public class TransactionsPO extends BaseNulsData implements Cloneable {

    private int type;

    private byte[] coinData;

    private byte[] txData;

    private long time;

    private byte[] transactionSignature;

    private byte[] remark;

    private long createTime;

    private transient NulsDigestData hash;

    private transient int size;

    public TransactionsPO() {
    }

    public TransactionsPO(Transaction tx) {
        this.type=tx.getType();
        this.time=tx.getTime();
        this.remark=tx.getRemark();
        this.txData=tx.getTxData();
        this.coinData=tx.getCoinData();
        this.transactionSignature=tx.getTransactionSignature();

    }

    public Transaction toTransaction() {
        Transaction tx = new Transaction();
        tx.setType(this.type);
        tx.setTime(this.time);
        tx.setRemark(this.remark);
        tx.setTxData(this.txData);
        tx.setCoinData(this.coinData);
        tx.setTransactionSignature(this.transactionSignature);
        return tx;
    }

    @Override
    public int size() {
        int size = 0;
        //type
        size += SerializeUtils.sizeOfUint16();
        //time
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfBytes(remark);
        size += SerializeUtils.sizeOfBytes(txData);
        size += SerializeUtils.sizeOfBytes(coinData);
        size += SerializeUtils.sizeOfBytes(transactionSignature);
        //createTime
        size += SerializeUtils.sizeOfUint48();
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(type);
        stream.writeUint48(time);
        stream.writeBytesWithLength(remark);
        stream.writeBytesWithLength(txData);
        stream.writeBytesWithLength(coinData);
        stream.writeBytesWithLength(transactionSignature);
        stream.writeUint48(createTime);
    }

    public byte[] serializeForHash() throws IOException {
        ByteArrayOutputStream bos = null;
        try {
            int size = size() - SerializeUtils.sizeOfBytes(transactionSignature)-SerializeUtils.sizeOfUint48();
            bos = new UnsafeByteArrayOutputStream(size);
            NulsOutputStreamBuffer buffer = new NulsOutputStreamBuffer(bos);
            if (size == 0) {
                bos.write(ToolsConstant.PLACE_HOLDER);
            } else {
                buffer.writeUint16(type);
                buffer.writeUint48(time);
                buffer.writeBytesWithLength(remark);
                buffer.writeBytesWithLength(txData);
                buffer.writeBytesWithLength(coinData);
            }
            return bos.toByteArray();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.type = byteBuffer.readUint16();
        this.time = byteBuffer.readUint48();
        this.remark = byteBuffer.readByLengthByte();
        this.txData = byteBuffer.readByLengthByte();
        this.coinData = byteBuffer.readByLengthByte();
        this.transactionSignature = byteBuffer.readByLengthByte();
        this.createTime = byteBuffer.readUint48();
    }

    public NulsDigestData getHash() {
        if (hash == null) {
            try {
                hash = NulsDigestData.calcDigestData(serializeForHash());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return hash;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getCoinData() {
        return coinData;
    }

    public void setCoinData(byte[] coinData) {
        this.coinData = coinData;
    }

    public byte[] getTxData() {
        return txData;
    }

    public void setTxData(byte[] txData) {
        this.txData = txData;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public byte[] getTransactionSignature() {
        return transactionSignature;
    }

    public void setTransactionSignature(byte[] transactionSignature) {
        this.transactionSignature = transactionSignature;
    }

    public byte[] getRemark() {
        return remark;
    }

    public void setRemark(byte[] remark) {
        this.remark = remark;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
