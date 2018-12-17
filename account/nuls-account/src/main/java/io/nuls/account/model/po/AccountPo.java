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
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.EncryptedData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author: qinyifeng
 */
public class AccountPo extends BaseNulsData {

    private transient Address addressObj;

    private String address;

    private int chainId;

    private Long createTime;

    private String alias;

    private byte[] pubKey;

    private byte[] priKey;

    private byte[] encryptedPriKey;

    private byte[] extend;

    private int status;

    private String remark;

    public AccountPo() {
    }

    public AccountPo(Account account) {
        this.addressObj = account.getAddress();
        this.chainId = account.getChainId();
        this.address = account.getAddress().toString();
        this.alias = account.getAlias();
        this.pubKey = account.getPubKey();
        this.priKey = account.getPriKey();
        this.encryptedPriKey = account.getEncryptedPriKey();
        this.extend = account.getExtend();
        this.remark = account.getRemark();
        this.createTime = account.getCreateTime();
    }

    public Account toAccount() {
        Account account = new Account();
        account.setChainId(this.getChainId());
        try {
            account.setAddress(Address.fromHashs(this.getAddress()));
        } catch (Exception e) {
            Log.error(e);
        }
        account.setAlias(this.getAlias());
        account.setExtend(this.getExtend());
        account.setPriKey(this.getPriKey());
        account.setPubKey(this.getPubKey());
        account.setEncryptedPriKey(this.getEncryptedPriKey());
        if (this.getPriKey() != null && this.getPriKey().length > 1) {
            account.setEcKey(ECKey.fromPrivate(new BigInteger(1, account.getPriKey())));
        } else {
            account.setEcKey(ECKey.fromEncrypted(new EncryptedData(this.getEncryptedPriKey()), this.getPubKey()));
        }
        account.setRemark(this.remark);
        account.setCreateTime(this.getCreateTime());
        return account;
    }

    @Override
    public int size() {
        int size = 0;
        //chainId
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(address);
        size += SerializeUtils.sizeOfString(alias);
        //createTime
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfBytes(pubKey);
        size += SerializeUtils.sizeOfBytes(priKey);
        size += SerializeUtils.sizeOfBytes(encryptedPriKey);
        size += SerializeUtils.sizeOfBytes(extend);
        size += SerializeUtils.sizeOfString(remark);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeString(address);
        stream.writeString(alias);
        stream.writeUint48(createTime);
        stream.writeBytesWithLength(pubKey);
        stream.writeBytesWithLength(priKey);
        stream.writeBytesWithLength(encryptedPriKey);
        stream.writeBytesWithLength(extend);
        stream.writeString(remark);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.address = byteBuffer.readString();
        this.alias = byteBuffer.readString();
        this.createTime = byteBuffer.readUint48();
        this.pubKey = byteBuffer.readByLengthByte();
        this.priKey = byteBuffer.readByLengthByte();
        this.encryptedPriKey = byteBuffer.readByLengthByte();
        this.extend = byteBuffer.readByLengthByte();
        this.remark = byteBuffer.readString();
    }

    public Address getAddressObj() {
        return addressObj;
    }

    public void setAddressObj(Address addressObj) {
        this.addressObj = addressObj;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public void setPubKey(byte[] pubKey) {
        this.pubKey = pubKey;
    }

    public byte[] getPriKey() {
        return priKey;
    }

    public void setPriKey(byte[] priKey) {
        this.priKey = priKey;
    }

    public byte[] getEncryptedPriKey() {
        return encryptedPriKey;
    }

    public void setEncryptedPriKey(byte[] encryptedPriKey) {
        this.encryptedPriKey = encryptedPriKey;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
