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

package io.nuls.account.model.po;


import io.nuls.account.model.bo.Account;
import io.nuls.tools.log.Log;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.EncryptedData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: EdwardChan
 *
 * Dec.19th 2018
 *
 */
public class MultiSigAccountPo extends BaseNulsData {

    private int chainId;

    private transient Address address;

    private List<byte[]> pubKeyList;

    private byte m;


    private String alias;

    public MultiSigAccountPo() {
    }

    public MultiSigAccountPo(MultiSigAccount multiSigAccount) {
        this.chainId = multiSigAccount.getChainId();
        this.address = multiSigAccount.getAddress();
        this.pubKeyList = multiSigAccount.getPubKeyList();
        this.m = multiSigAccount.getM();
        this.alias = multiSigAccount.getAlias();
    }

    public MultiSigAccount toAccount() {
        MultiSigAccount account = new MultiSigAccount();
        account.setChainId(chainId);
        account.setAddress(address);
        account.setPubKeyList(pubKeyList);
        account.setM(m);
        account.setAlias(alias);
        return account;
    }

    @Override
    public int size() {
        int size = 4;//chainId
        size += Address.ADDRESS_LENGTH;
        size += 4;
        for (byte[] bytes:pubKeyList) {
            size += SerializeUtils.sizeOfBytes(bytes);
        }
        size += 1;
        size += SerializeUtils.sizeOfString(alias);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(chainId);
        stream.write(address.getAddressBytes());
        stream.writeUint32(pubKeyList.size());
        for (int i = 0; i < pubKeyList.size(); i++) {
            stream.writeBytesWithLength(pubKeyList.get(i));
        }
        stream.write(m);
        stream.writeString(alias);

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readInt32();
        byte[] bytes = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.address = Address.fromHashs(bytes);
        this.pubKeyList = new ArrayList<>();
        long count = byteBuffer.readUint32();
        for (int i = 0; i < count; i++) {
            pubKeyList.add(byteBuffer.readByLengthByte());
        }
        this.m = byteBuffer.readByte();
        this.alias = byteBuffer.readString();
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
