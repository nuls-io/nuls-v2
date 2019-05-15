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
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.utils.TimeUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */

public class AccountStateUnconfirmed extends BaseNulsData {

    private String address;

    private int addressChainId;

    private int assetChainId;

    private int assetId;
    private byte[] fromNonce = LedgerConstant.getInitNonceByte();
    private byte[] nonce = LedgerConstant.getInitNonceByte();
    private BigInteger amount = BigInteger.ZERO;
    private BigInteger toConfirmedAmount = BigInteger.ZERO;
    private BigInteger unconfirmedAmount = BigInteger.ZERO;

    Map<String, TxUnconfirmed> txUnconfirmedMap = new HashMap<>();

    private long createTime = 0;

    public AccountStateUnconfirmed() {
        super();
    }

    public Map<String, TxUnconfirmed> getTxUnconfirmedMap() {
        return txUnconfirmedMap;
    }

    public void setTxUnconfirmedMap(Map<String, TxUnconfirmed> txUnconfirmedMap) {
        this.txUnconfirmedMap = txUnconfirmedMap;
    }

    public void addTxUnconfirmed(String nonce, TxUnconfirmed txUnconfirmed) {
        txUnconfirmedMap.put(nonce, txUnconfirmed);
    }

    public void addTxUnconfirmeds(Map<String, TxUnconfirmed> txUnconfirmeds) {
        txUnconfirmedMap.putAll(txUnconfirmeds);
    }

    public TxUnconfirmed getTxUnconfirmed(String nonce) {
        return txUnconfirmedMap.get(nonce);
    }

    public void delTxUnconfirmed(String nonce) {
        txUnconfirmedMap.remove(nonce);
    }

    public void clearTxUnconfirmeds() {
        txUnconfirmedMap.clear();
    }

    public AccountStateUnconfirmed(String address, int addressChainId, int assetChainId, int assetId, byte[] pFromNonce, byte[] pNonce, BigInteger amount) {
        this.address = address;
        this.addressChainId = addressChainId;
        this.assetChainId = assetChainId;
        this.assetId = assetId;
        System.arraycopy(pFromNonce, 0, fromNonce, 0, LedgerConstant.NONCE_LENGHT);
        System.arraycopy(pNonce, 0, nonce, 0, LedgerConstant.NONCE_LENGHT);
        this.unconfirmedAmount = amount;
        this.createTime = TimeUtil.getCurrentTime();
    }


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        stream.writeUint16(addressChainId);
        stream.writeUint16(assetChainId);
        stream.writeUint16(assetId);
        stream.write(fromNonce);
        stream.write(nonce);
        stream.writeBigInteger(amount);
        stream.writeUint48(createTime);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readString();
        this.addressChainId = byteBuffer.readUint16();
        this.assetChainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.fromNonce = byteBuffer.readBytes(LedgerConstant.NONCE_LENGHT);
        this.nonce = byteBuffer.readBytes(LedgerConstant.NONCE_LENGHT);
        this.amount = byteBuffer.readBigInteger();
        this.createTime = byteBuffer.readUint48();
    }

    @Override
    public int size() {
        int size = 0;
        //address
        size += SerializeUtils.sizeOfString(address);
        //chainId
        size += SerializeUtils.sizeOfInt16();
        //asset chainId
        size += SerializeUtils.sizeOfInt16();
        //assetId
        size += SerializeUtils.sizeOfInt16();
        size += fromNonce.length;
        size += nonce.length;
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfUint48();
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

    public byte[] getFromNonce() {
        return fromNonce;
    }

    public void setFromNonce(byte[] fromNonce) {
        this.fromNonce = fromNonce;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    public BigInteger getAmount() {
        BigInteger unconfirmed = unconfirmedAmount.subtract(toConfirmedAmount);
        if (BigIntegerUtils.isLessThan(unconfirmed, BigInteger.ZERO)) {
            return BigInteger.ZERO;
        } else {
            return unconfirmed;
        }
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isOverTime() {
        return (TimeUtil.getCurrentTime() - createTime) > LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME;
    }

    public BigInteger getToConfirmedAmount() {
        return toConfirmedAmount;
    }

    public void setToConfirmedAmount(BigInteger toConfirmedAmount) {
        this.toConfirmedAmount = toConfirmedAmount;
    }

    public BigInteger getUnconfirmedAmount() {
        return unconfirmedAmount;
    }

    public void setUnconfirmedAmount(BigInteger unconfirmedAmount) {
        this.unconfirmedAmount = unconfirmedAmount;
    }

}
