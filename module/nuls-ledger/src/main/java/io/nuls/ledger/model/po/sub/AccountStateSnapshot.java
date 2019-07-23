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
package io.nuls.ledger.model.po.sub;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.ledger.model.po.AccountState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 1.用于备份用户某高度的账本信息，key值是高度
 * 2.该部分的nonces 集合是包含了该高度里对应的账户需要回滚的所有nonce值。
 *
 * @author lanjinsheng
 * @date 2018/11/19
 */
public class AccountStateSnapshot extends BaseNulsData {
    /**
     * 需要备份的账户信息，与accountState比较，增加了地址与资产信息，用于回滚使用。
     */
    private String address;

    private int addressChainId;

    private int assetChainId;

    private int assetId;

    AccountState accountState;
    /**
     * 区块中对应账户的所有nonce值集合
     */
    private List<AmountNonce> nonces = new ArrayList<>();

    public AccountStateSnapshot() {
        super();
    }

    public AccountStateSnapshot(int addressChainId, int assetChainId, int assetId, String address, AccountState accountState) {
        this.address = address;
        this.addressChainId = addressChainId;
        this.assetChainId = assetChainId;
        this.assetId = assetId;
        this.accountState = accountState;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        stream.writeUint16(addressChainId);
        stream.writeUint16(assetChainId);
        stream.writeUint16(assetId);
        stream.writeNulsData(accountState);
        stream.writeUint16(nonces.size());
        for (AmountNonce nonce : nonces) {
            stream.writeNulsData(nonce);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readString();
        this.addressChainId = byteBuffer.readUint16();
        this.assetChainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        AccountState accountState = new AccountState();
        byteBuffer.readNulsData(accountState);
        this.accountState = accountState;
        int nonceCount = byteBuffer.readUint16();
        for (int i = 0; i < nonceCount; i++) {
            AmountNonce amountNonce = new AmountNonce();
            byteBuffer.readNulsData(amountNonce);
            this.nonces.add(amountNonce);
        }
    }

    @Override
    public int size() {
        int size = 0;
        //address
        size += SerializeUtils.sizeOfString(address);
        //chainId
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfInt16();
        //assetId
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfNulsData(accountState);
        size += SerializeUtils.sizeOfUint16();
        for (AmountNonce nonce : nonces) {
            size += SerializeUtils.sizeOfNulsData(nonce);
        }
        return size;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAddressChainId() {
        return addressChainId;
    }

    public void setAddressChainId(int addressChainId) {
        this.addressChainId = addressChainId;
    }

    public int getAssetChainId() {
        return assetChainId;
    }

    public void setAssetChainId(int assetChainId) {
        this.assetChainId = assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public AccountState getAccountState() {
        return accountState;
    }

    public void setAccountState(AccountState accountState) {
        this.accountState = accountState;
    }

    public List<AmountNonce> getNonces() {
        return nonces;
    }

    public void setNonces(List<AmountNonce> nonces) {
        this.nonces = nonces;
    }
}
