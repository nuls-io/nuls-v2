/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.base.data.po;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.signture.BlockSignature;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.HashUtil;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 区块头存储对象
 *
 * @author captain
 * @version 1.0
 * @date 18-12-10 下午3:50
 */
public class BlockHeaderPo extends BaseNulsData {

    private byte[] hash;
    private boolean complete;
    private byte[] preHash;
    private byte[] merkleHash;
    private long time;
    private long height;
    private int txCount;
    private BlockSignature blockSignature;
    private byte[] extend;
    private int blockSize;
    private List<byte[]> txHashList;
    private transient byte[] packingAddress;

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public byte[] getPreHash() {
        return preHash;
    }

    public void setPreHash(byte[] preHash) {
        this.preHash = preHash;
    }

    public byte[] getMerkleHash() {
        return merkleHash;
    }

    public void setMerkleHash(byte[] merkleHash) {
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

    public void setBlockSignature(BlockSignature blockSignature) {
        this.blockSignature = blockSignature;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public List<byte[]> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<byte[]> txHashList) {
        this.txHashList = txHashList;
    }

    public byte[] getPackingAddress(int chainID) {
        if (this.blockSignature != null && this.packingAddress == null) {
            this.packingAddress = AddressTool.getAddress(blockSignature.getPublicKey(), chainID);
        }
        return packingAddress;
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfBoolean();
        size += HashUtil.HASH_LENGTH;               //hash
        size += HashUtil.HASH_LENGTH;               //preHash
        size += HashUtil.HASH_LENGTH;               //merkleHash
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfBytes(extend);
        size += txHashList.size() * HashUtil.HASH_LENGTH;
        size += SerializeUtils.sizeOfNulsData(blockSignature);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBoolean(complete);
        stream.write(hash);
        stream.write(preHash);
        stream.write(merkleHash);
        stream.writeUint32(time);
        stream.writeUint32(height);
        stream.writeUint32(txCount);
        stream.writeUint32(blockSize);
        stream.writeBytesWithLength(extend);
        stream.writeNulsData(blockSignature);
        for (byte[] hash : txHashList) {
            stream.write(hash);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.complete = byteBuffer.readBoolean();
        this.hash = byteBuffer.readHash();
        this.preHash = byteBuffer.readHash();
        this.merkleHash = byteBuffer.readHash();
        this.time = byteBuffer.readUint32();
        this.height = byteBuffer.readUint32();
        this.txCount = byteBuffer.readInt32();
        this.blockSize = byteBuffer.readInt32();
        this.extend = byteBuffer.readByLengthByte();
        this.txHashList = new ArrayList<>();
        this.blockSignature = byteBuffer.readNulsData(new BlockSignature());
        for (int i = 0; i < txCount; i++) {
            this.txHashList.add(byteBuffer.readHash());
        }
    }

}
