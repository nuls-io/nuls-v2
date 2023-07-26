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
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.constant.LedgerConstant;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 1.未确认的账户最终账本对象，存储在内存中。
 * 2.key值:address-assetChainId-assetId
 * 3.txUnconfirmedMap 将包含所有的未确认交易值
 *
 * @author lanjinsheng
 * @date 2018/11/19
 */

public class AccountStateUnconfirmed {
    /**
     * 当前状态的上一笔交易nonce值
     */
    private byte[] fromNonce = LedgerConstant.getInitNonceByte();
    /**
     * 当前状态的nonce值
     */
    private byte[] nonce = LedgerConstant.getInitNonceByte();
    /**
     * 从未确认跃迁到已确认的资产金额（累加值）
     */
    private BigInteger toConfirmedAmount = BigInteger.ZERO;
    /**
     * 未确认的资产金额（累加值）
     */
    private BigInteger unconfirmedAmount = BigInteger.ZERO;
    /**
     * 账户对应的未确认集合，key值是当前交易 的 nonce值
     */
    Map<String, TxUnconfirmed> txUnconfirmedMap = new ConcurrentHashMap<>();

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
        this.createTime = NulsDateUtils.getCurrentTimeSeconds();
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
        return (NulsDateUtils.getCurrentTimeSeconds() - createTime) > LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME;
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
