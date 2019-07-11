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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.constant.ToolsConstant;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * @author Charlie
 */
public class Transaction extends BaseNulsData implements Cloneable {

    private int type;

    private byte[] coinData;

    private byte[] txData;

    private long time;

    private byte[] transactionSignature;

    private byte[] remark;

    private transient NulsHash hash;

    private transient long blockHeight = -1L;

    private transient TxStatusEnum status = TxStatusEnum.UNCONFIRM;

    private transient int size;

    /**
     * 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序
     */
    private int inBlockIndex;

    public Transaction() {

    }

    public Transaction(int type) {
        this.type = type;
    }

    @Override
    public int size() {
        int size = 0;
        //type
        size += SerializeUtils.sizeOfUint16();
        //time
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfBytes(remark);
        size += SerializeUtils.sizeOfBytes(txData);
        size += SerializeUtils.sizeOfBytes(coinData);
        size += SerializeUtils.sizeOfBytes(transactionSignature);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(type);
        stream.writeUint32(time);
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
            } else {
                buffer.writeUint16(type);
                buffer.writeUint32(time);
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
        time = byteBuffer.readUint32();
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

    public NulsHash getHash() {
        if (hash == null) {
            try {
                hash = NulsHash.calcHash(serializeForHash());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return hash;
    }

    public void setHash(NulsHash hash) {
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

    public int getInBlockIndex() {
        return inBlockIndex;
    }

    public void setInBlockIndex(int inBlockIndex) {
        this.inBlockIndex = inBlockIndex;
    }

    public CoinData getCoinDataInstance() throws NulsException {
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

    public static Transaction getInstance(byte[] txBytes) throws NulsException {
        NulsByteBuffer nulsByteBuffer = new NulsByteBuffer(txBytes);
        Transaction transaction = new Transaction();
        transaction.parse(nulsByteBuffer);
        return transaction;
    }

    /**
     * 获取交易的手续费
     * @return
     * @throws NulsException
     */
    public BigInteger getFee() throws NulsException {
        BigInteger fee = BigInteger.ZERO;
        if (null != coinData && type > 1) {
            CoinData cData = getCoinDataInstance();
            if(cData.getFrom().size() > 0) {
                BigInteger toAmount = BigInteger.ZERO;
                for (CoinTo coinTo : cData.getTo()) {
                    toAmount = toAmount.add(coinTo.getAmount());
                }
                BigInteger fromAmount = BigInteger.ZERO;
                for (CoinFrom coinFrom : cData.getFrom()) {
                    fromAmount = fromAmount.add(coinFrom.getAmount());
                }
                fee =  fromAmount.subtract(toAmount);
            }
        }
        return fee;
    }

    /**
     * 判断交易是否为多签交易
     * Judging whether a transaction is a multi-signature transaction
     */
    public boolean isMultiSignTx() throws NulsException {
        if (null == coinData) {
            return false;
        }
        CoinData cData = getCoinDataInstance();
        List<CoinFrom> from = cData.getFrom();
        if (from == null || from.size() == 0) {
            return false;
        }
        CoinFrom coinFrom = from.get(0);
        if (AddressTool.isMultiSignAddress(coinFrom.getAddress())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Transaction)) {
            return false;
        }
        return this.getHash().equals(((Transaction) obj).getHash());
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + Arrays.hashCode(coinData);
        result = 31 * result + Arrays.hashCode(txData);
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + Arrays.hashCode(transactionSignature);
        result = 31 * result + Arrays.hashCode(remark);
        return result;
    }
}
