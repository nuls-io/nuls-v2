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
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.*;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountState extends BaseNulsData {

    @Setter
    @Getter
    private int chainId;

    @Setter
    @Getter
    private int assetId;

    @Setter
    @Getter
    private String nonce;

    @Setter
    @Getter
    private String txHash;
    @Setter
    @Getter
    private long height = 0;
    /**
     * 最近一次的解冻处理时间,存储毫秒
     */
    @Setter
    @Getter
    private long latestUnFreezeTime = 0;
    /**
     * 账户冻结的资产(高度冻结)
     */
    @Setter
    @Getter
    private List<String> unconfirmedNonces = new ArrayList<>();
    /**
     * 账户总金额入账
     */
    @Setter
    @Getter
    private BigInteger totalFromAmount = BigInteger.ZERO;

    /**
     * 账户总金额出账
     */
    @Setter
    @Getter
    private BigInteger totalToAmount = BigInteger.ZERO;


    /**
     * 账户冻结的资产
     */
    @Setter
    @Getter
    private FreezeState freezeState;

    public AccountState(int chainId, int assetId, String nonce) {
        this.chainId = chainId;
        this.assetId = assetId;
        this.nonce = nonce;
        this.freezeState = new FreezeState();
    }

    /**
     * 获取最近的未提交交易nonce
     * @return
     */
    public String getUnconfirmedNonce(){
        if(unconfirmedNonces.size() == 0){
            return "";
        }
        return unconfirmedNonces.get(unconfirmedNonces.size()-1);
    }
    public void setUnconfirmedNonce(String nonce){
        unconfirmedNonces.add(nonce);
    }

    public boolean updateConfirmedNonce(String nonce){
        this.nonce = nonce;
        if(unconfirmedNonces.size()>0) {
            String unconfirmedNonce = unconfirmedNonces.get(0);
            if (unconfirmedNonce.equalsIgnoreCase(nonce)) {
                unconfirmedNonces.remove(0);
            }else{
                //分叉了，清空之前的未提交nonce
                unconfirmedNonces.clear();
            }
            return true;
        }else{
           return false;
        }
    }
    /**
     * 获取账户可用金额（不含锁定金额）
     *
     * @return BigInteger
     */
    public BigInteger getAvailableAmount(){
        return totalToAmount.subtract(totalFromAmount);
    }
    public void addTotalFromAmount(BigInteger value){
        totalFromAmount = totalFromAmount.add(value);
    }

    public void addTotalToAmount(BigInteger value){
        totalToAmount = totalToAmount.add(value);
    }
    /**
     * 获取账户总金额（含锁定金额）
     *
     * @return BigInteger
     */
    public BigInteger getTotalAmount() {
        return totalToAmount.subtract(totalFromAmount).add(freezeState.getTotal());
    }


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeString(nonce);
        stream.writeString(txHash);
        stream.writeUint32(height);
        stream.writeUint32(latestUnFreezeTime);
        stream.writeUint16(unconfirmedNonces.size());
        for (String unconfirmedNonce : unconfirmedNonces) {
            stream.writeString(unconfirmedNonce);
        }
        stream.writeNulsData(freezeState);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.nonce = byteBuffer.readString();
        this.txHash = byteBuffer.readString();
        this.height = byteBuffer.readUint32();
        this.latestUnFreezeTime = byteBuffer.readUint32();
        int unconfirmNonceCount = byteBuffer.readUint16();
        for (int i = 0; i < unconfirmNonceCount; i++) {
            try {
                this.unconfirmedNonces.add(byteBuffer.readString());
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
        this.freezeState = new FreezeState();
        byteBuffer.readNulsData(freezeState);
    }

    @Override
    public int size() {
        int size = 0;
        //chainId
        size += SerializeUtils.sizeOfInt16();
        //assetId
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfString(nonce);
        size += SerializeUtils.sizeOfString(txHash);
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfUint16();
        for (String unconfirmedNonce : unconfirmedNonces) {
            size += SerializeUtils.sizeOfString(unconfirmedNonce);
        }
        //totalFromAmount
        size += SerializeUtils.sizeOfBigInteger();
        //totalToAmount
        size += SerializeUtils.sizeOfBigInteger();
        size += SerializeUtils.sizeOfNulsData(freezeState);
        return size;
    }
    public AccountState deepClone()  {
                 // 将对象写到流里
        try {
                 ByteArrayOutputStream bo = new ByteArrayOutputStream();
                ObjectOutputStream oo = null;

                    oo = new ObjectOutputStream(bo);

                oo.writeObject(this);
                        // 从流里读出来
                ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
                ObjectInputStream oi = new ObjectInputStream(bi);
                return ((AccountState)oi.readObject());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
