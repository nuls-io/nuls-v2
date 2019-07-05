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
import io.nuls.base.signture.BlockSignature;
import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.parse.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author vivi
 */
public class BlockHeader extends BaseNulsData {

    /**
     * 区块头排序器
     */
    public static final Comparator<BlockHeader> BLOCK_HEADER_COMPARATOR = Comparator.comparingLong(BlockHeader::getHeight);

    private transient NulsHash hash;
    private NulsHash preHash;
    private NulsHash merkleHash;
    private long time;
    private long height;
    private int txCount;
    private BlockSignature blockSignature;
    private byte[] extend;
    /**
     * pierre add 智能合约世界状态根
     */
    private transient byte[] stateRoot;

    private transient byte[] packingAddress;

    private synchronized void calcHash() {
        if (null != this.hash) {
            return;
        }
        try {
            hash = NulsHash.calcHash(serializeWithoutSign());
        } catch (Exception e) {
            throw new NulsRuntimeException(e);
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += NulsHash.HASH_LENGTH;               //preHash
        size += NulsHash.HASH_LENGTH;               //merkleHash
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfBytes(extend);
        size += SerializeUtils.sizeOfNulsData(blockSignature);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(preHash.getBytes());
        stream.write(merkleHash.getBytes());
        stream.writeUint32(time);
        stream.writeUint32(height);
        stream.writeUint32(txCount);
        stream.writeBytesWithLength(extend);
        stream.writeNulsData(blockSignature);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.preHash = byteBuffer.readHash();
        this.merkleHash = byteBuffer.readHash();
        this.time = byteBuffer.readUint32();
        this.height = byteBuffer.readUint32();
        this.txCount = byteBuffer.readInt32();
        this.extend = byteBuffer.readByLengthByte();
        this.blockSignature = byteBuffer.readNulsData(new BlockSignature());
    }

    public byte[] serializeWithoutSign() {
        int size = size() - SerializeUtils.sizeOfNulsData(blockSignature);
        try (ByteArrayOutputStream bos = new UnsafeByteArrayOutputStream(size)) {
            NulsOutputStreamBuffer buffer = new NulsOutputStreamBuffer(bos);
            buffer.write(preHash.getBytes());
            buffer.write(merkleHash.getBytes());
            buffer.writeUint32(time);
            buffer.writeUint32(height);
            buffer.writeUint32(txCount);
            buffer.writeBytesWithLength(extend);
            byte[] bytes = bos.toByteArray();
            if (bytes.length != size) {
                throw new RuntimeException();
            }
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public NulsHash getHash() {
        if (null == hash) {
            calcHash();
        }
        return hash;
    }

    public void setHash(NulsHash hash) {
        this.hash = hash;
    }

    public NulsHash getPreHash() {
        return preHash;
    }

    public void setPreHash(NulsHash preHash) {
        this.preHash = preHash;
    }

    public NulsHash getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(NulsHash merkleHash) {
        this.merkleHash = merkleHash;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public BlockSignature getBlockSignature() {
        return blockSignature;
    }

    public void setBlockSignature(BlockSignature scriptSign) {
        this.blockSignature = scriptSign;
    }

    public byte[] getPackingAddress(int chainID) {
        if (this.blockSignature != null && this.packingAddress == null) {
            this.packingAddress = AddressTool.getAddress(blockSignature.getPublicKey(), chainID);
        }
        return packingAddress;
    }


    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }

    @Override
    public String toString() {
        return "BlockHeader{" +
                "hash=" + hash.toHex() +
                ", preHash=" + preHash.toHex() +
                ", merkleHash=" + merkleHash.toHex() +
                ", time=" + time +
                ", height=" + height +
                ", txCount=" + txCount +
                ", blockSignature=" + blockSignature +
                //", extend=" + Arrays.toString(extend) +
                ", size=" + size() +
                ", packingAddress=" + (packingAddress == null ? packingAddress : AddressTool.getStringAddressByBytes(packingAddress)) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BlockHeader header = (BlockHeader) o;

        if (time != header.time) {
            return false;
        }
        if (height != header.height) {
            return false;
        }
        if (txCount != header.txCount) {
            return false;
        }
        if (!preHash.equals(header.preHash)) {
            return false;
        }
        if (!merkleHash.equals(header.merkleHash)) {
            return false;
        }
        if (!blockSignature.equals(header.blockSignature)) {
            return false;
        }
        return Arrays.equals(extend, header.extend);
    }

    @Override
    public int hashCode() {
        int result = preHash.hashCode();
        result = 31 * result + merkleHash.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (height ^ (height >>> 32));
        result = 31 * result + txCount;
        result = 31 * result + blockSignature.hashCode();
        result = 31 * result + Arrays.hashCode(extend);
        return result;
    }
}
