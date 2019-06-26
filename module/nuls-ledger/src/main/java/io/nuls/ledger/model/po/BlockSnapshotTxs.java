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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cody on 2019/01/09.
 * 用于缓存区块中的账户信息
 *
 * @author lanjinsheng
 */
public class BlockSnapshotTxs extends BaseNulsData {

    private List<String> txHashList = new ArrayList<String>();
    private List<String> addressNonceList = new ArrayList<String>();
    private String blockHash = "";

    public List<String> getTxHashList() {
        return txHashList;
    }

    public void setTxHashList(List<String> txHashList) {
        this.txHashList = txHashList;
    }

    public List<String> getAddressNonceList() {
        return addressNonceList;
    }

    public void setAddressNonceList(List<String> addressNonceList) {
        this.addressNonceList = addressNonceList;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public void addHash(String hash) {
        txHashList.add(hash);
    }

    public void addNonce(String addressNonce) {
        addressNonceList.add(addressNonce);
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(blockHash);
        stream.writeUint16(txHashList.size());
        for (String hash : txHashList) {
            stream.writeString(hash);
        }
        stream.writeUint16(addressNonceList.size());
        for (String addressNonce : addressNonceList) {
            stream.writeString(addressNonce);
        }

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        blockHash = byteBuffer.readString();
        int hashCount = byteBuffer.readUint16();
        for (int i = 0; i < hashCount; i++) {
            try {
                txHashList.add(byteBuffer.readString());
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }

        int nonceCount = byteBuffer.readUint16();
        for (int i = 0; i < nonceCount; i++) {
            try {
                addressNonceList.add(byteBuffer.readString());
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }

    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(blockHash);
        size += SerializeUtils.sizeOfUint16();
        for (String hash : txHashList) {
            size += SerializeUtils.sizeOfString(hash);
        }
        size += SerializeUtils.sizeOfUint16();
        for (String addressNonce : addressNonceList) {
            size += SerializeUtils.sizeOfString(addressNonce);
        }

        return size;
    }
}
