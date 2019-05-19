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

import io.nuls.core.model.BigIntegerUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.utils.TimeUtil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */

public class AccountStateUnconfirmed {
    private byte[] fromNonce = LedgerConstant.getInitNonceByte();
    private byte[] nonce = LedgerConstant.getInitNonceByte();

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

    public AccountStateUnconfirmed(byte[] pFromNonce, byte[] pNonce, BigInteger amount) {
        System.arraycopy(pFromNonce, 0, fromNonce, 0, LedgerConstant.NONCE_LENGHT);
        System.arraycopy(pNonce, 0, nonce, 0, LedgerConstant.NONCE_LENGHT);
        this.unconfirmedAmount = amount;
        this.createTime = TimeUtil.getCurrentTime();
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
