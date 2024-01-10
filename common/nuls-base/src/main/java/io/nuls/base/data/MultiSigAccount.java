/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
 */

package io.nuls.base.data;

import com.google.common.base.Objects;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Multiple address accounts
 *
 * @author: qinyifeng
 * @date: 2018/12/12
 */
public class MultiSigAccount extends BaseNulsData {

    private int chainId;
    private Address address;
    private byte m;
    private List<byte[]> pubKeyList;
    private String alias;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(chainId);
        stream.write(address.getAddressBytes());
        stream.write(m);
        stream.writeUint32(pubKeyList.size());
        for (int i = 0; i < pubKeyList.size(); i++) {
            stream.writeBytesWithLength(pubKeyList.get(i));
        }
        stream.writeString(alias);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readInt32();
        byte[] bytes = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.address = Address.fromHashs(bytes);
        this.m = byteBuffer.readByte();
        this.pubKeyList = new ArrayList<>();
        long count = byteBuffer.readUint32();
        for (int i = 0; i < count; i++) {
            pubKeyList.add(byteBuffer.readByLengthByte());
        }
        this.alias = byteBuffer.readString();
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfUint32();
        size += Address.ADDRESS_LENGTH;
        size += 1;
        size += SerializeUtils.sizeOfUint32();
        for (int i = 0; i < pubKeyList.size(); i++) {
            size += SerializeUtils.sizeOfBytes(pubKeyList.get(i));
        }
        size += SerializeUtils.sizeOfString(alias);
        return size;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<byte[]> getPubKeyList() {
        return pubKeyList;
    }

    public void setPubKeyList(List<byte[]> pubKeyList) {
        this.pubKeyList = pubKeyList;
    }

    public byte getM() {
        return m;
    }

    public void setM(byte m) {
        this.m = m;
    }

    public void addPubkeys(List<String> pubkeys) {
        this.pubKeyList = new ArrayList<>();
        for (String pubkeyStr : pubkeys) {
            pubKeyList.add(HexUtil.decode(pubkeyStr));
        }
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultiSigAccount that = (MultiSigAccount) o;
        return chainId == that.chainId &&
                m == that.m &&
                Objects.equal(address, that.address) &&
                Objects.equal(pubKeyList, that.pubKeyList) &&
                Objects.equal(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chainId, address, m, pubKeyList, alias);
    }
}
