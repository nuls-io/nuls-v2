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
package io.nuls.base.data;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.constant.TxStatusEnum;
import io.nuls.tools.constant.ToolsConstant;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.crypto.UnsafeByteArrayOutputStream;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.tools.thread.TimeService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

/**
 * @author Charlie
 */
public class Transaction extends BaseNulsData implements Cloneable {

    protected int type;

    protected byte[] coinData;

    protected byte[] txData;

    protected long time;

    private byte[] transactionSignature;

    protected byte[] remark;

    protected transient NulsDigestData hash;

    protected long blockHeight = -1L;

    protected transient TxStatusEnum status = TxStatusEnum.UNCONFIRM;

    protected transient int size;

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
    }

    public byte[] serializeForHash() throws IOException {
        ByteArrayOutputStream bos = null;
        try {
            int size = size() - SerializeUtils.sizeOfBytes(transactionSignature);
            bos = new UnsafeByteArrayOutputStream(size);
            NulsOutputStreamBuffer buffer = new NulsOutputStreamBuffer(bos);
            if (size == 0) {
                bos.write(ToolsConstant.PLACE_HOLDER);
            }else {
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
        type = byteBuffer.readUint16();
        time = byteBuffer.readUint48();
        remark = byteBuffer.readByLengthByte();
        txData = byteBuffer.readByLengthByte();
        this.coinData = byteBuffer.readByLengthByte();
        transactionSignature = byteBuffer.readByLengthByte();
    }

    public byte[] getTxData() {
        return txData;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public byte[] getRemark() {
        return remark;
    }

    public void setRemark(byte[] remark) {
        this.remark = remark;
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

    public void setHash(NulsDigestData hash) {
        this.hash = hash;
    }

    public byte[] getTransactionSignature() {
        return transactionSignature;
    }

    public void setTransactionSignature(byte[] transactionSignature) {
        this.transactionSignature = transactionSignature;
    }

    public void setTxData(byte[] txData) {
        this.txData = txData;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public TxStatusEnum getStatus() {
        return status;
    }

    public void setStatus(TxStatusEnum status) {
        this.status = status;
    }

    public byte[] getCoinData() {
        return coinData;
    }

    public CoinData getCoinDataInstance() throws NulsException{
        CoinData coinData = new CoinData();
        coinData.parse(new NulsByteBuffer(this.coinData));
        return coinData;
    }

    public void setCoinData(byte[] coinData) {
        this.coinData = coinData;
    }

    public int getSize() {
        if (size == 0) {
            size = size();
        }
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static Transaction getInstance(String Hex) throws NulsException{
        return getInstance(HexUtil.decode(Hex));
    }

    public static Transaction getInstance(byte[] txBytes) throws NulsException{
        NulsByteBuffer nulsByteBuffer = new NulsByteBuffer(txBytes);
        Transaction transaction = new Transaction();
        transaction.parse(nulsByteBuffer);
        return transaction;
    }

    public String hex() throws Exception{
        return HexUtil.encode(this.serialize());
    }

    public BigInteger getFee() throws NulsException{
        BigInteger fee = BigInteger.ZERO;
        if (null != coinData) {
            CoinData cData = getCoinDataInstance();
            fee = cData.getFee();
        }
        return fee;
    }

    public Transaction(){

    }

    public Transaction(int type) {
        this.time = TimeService.currentTimeMillis();
        this.type = type;
    }
}
