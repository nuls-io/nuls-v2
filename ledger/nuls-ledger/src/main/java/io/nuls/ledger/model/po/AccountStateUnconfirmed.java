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
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.utils.TimeUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */

public class AccountStateUnconfirmed extends BaseNulsData {

    private String address;

    private int addressChainId;

    private int assetChainId;

    private int assetId;

    private String dbNonce = LedgerConstant.INIT_NONCE;
    private String dbHash = "";

    /**
     * 未确认交易的nonce列表，有花费的余额的交易会更改这部分Nonce数据
     */

    private List<UnconfirmedNonce> unconfirmedNonces = new ArrayList<>();
    /**
     * 未确认交易，存储交易金额数据
     */
    private List<UnconfirmedAmount> unconfirmedAmounts = new ArrayList<>();


    public AccountStateUnconfirmed() {
        super();
    }

    public AccountStateUnconfirmed(String address, int addressChainId, int assetChainId, int assetId, String dbNonce) {
        this.address = address;
        this.addressChainId = addressChainId;
        this.assetChainId = assetChainId;
        this.assetId = assetId;
        this.dbNonce = dbNonce;
    }

    /**
     * 获取最近的未提交交易nonce
     *
     * @return
     */
    public String getLatestUnconfirmedNonce() {
        if (unconfirmedNonces.size() == 0) {
            return dbNonce;
        }
        return unconfirmedNonces.get(unconfirmedNonces.size() - 1).getNonce();
    }

    /**
     * 计算未确认交易的可用余额(不含已确认的账户余额)
     *
     * @return
     */
    public BigInteger getUnconfirmedAmount() {
        BigInteger calUnconfirmedAmount = BigInteger.ZERO;
        for (UnconfirmedAmount unconfirmedAmount : unconfirmedAmounts) {
            calUnconfirmedAmount = calUnconfirmedAmount.add(unconfirmedAmount.getEarnAmount()).subtract(unconfirmedAmount.getSpendAmount());

        }
        return calUnconfirmedAmount;
    }


    public void addUnconfirmedNonce(UnconfirmedNonce unconfirmedNonce) {
        unconfirmedNonces.add(unconfirmedNonce);
    }

    public String getUnconfirmedNoncesStrs() {
        StringBuilder s = new StringBuilder();
        for (UnconfirmedNonce unconfirmedNonce : unconfirmedNonces) {
            s.append(unconfirmedNonce.getNonce() + ",");
        }
        return s.toString();
    }

    public void addUnconfirmedAmount(UnconfirmedAmount unconfirmedAmount) {
        unconfirmedAmounts.add(unconfirmedAmount);
    }


//    public boolean updateConfirmedAmount(String hash) {
//        if (unconfirmedAmounts.size() > 0) {
//            UnconfirmedAmount unconfirmedAmount = unconfirmedAmounts.get(0);
//            if (unconfirmedAmount.getTxHash().equalsIgnoreCase(hash)) {
//                //未确认的转为确认的，移除集合中的数据
//                unconfirmedAmounts.remove(0);
//            } else {
//                //分叉了，清空之前的未提交金额数据
//                unconfirmedAmounts.clear();
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }

    /**
     * 计算未确认交易的冻结部分
     *
     * @return
     */
    public BigInteger getUnconfirmedFreezeAmount() {
        BigInteger calUnconfirmedFreeAmount = BigInteger.ZERO;
        for (UnconfirmedAmount unconfirmedAmount : unconfirmedAmounts) {
            //add 冻结 subtract 解锁的
            calUnconfirmedFreeAmount = calUnconfirmedFreeAmount.add(unconfirmedAmount.getToLockedAmount()).subtract(unconfirmedAmount.getFromUnLockedAmount());
        }
        return calUnconfirmedFreeAmount;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        stream.writeUint16(addressChainId);
        stream.writeUint16(assetChainId);
        stream.writeUint16(assetId);
        stream.writeString(dbNonce);

        stream.writeUint16(unconfirmedNonces.size());
        for (UnconfirmedNonce unconfirmedNonce : unconfirmedNonces) {
            stream.writeNulsData(unconfirmedNonce);
        }

        stream.writeUint16(unconfirmedAmounts.size());
        for (UnconfirmedAmount unconfirmedAmount : unconfirmedAmounts) {
            stream.writeNulsData(unconfirmedAmount);
        }

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readString();
        this.addressChainId = byteBuffer.readUint16();
        this.assetChainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.dbNonce = byteBuffer.readString();
        int unconfirmNonceCount = byteBuffer.readUint16();
        for (int i = 0; i < unconfirmNonceCount; i++) {
            try {
                UnconfirmedNonce unconfirmedNonce = new UnconfirmedNonce();
                byteBuffer.readNulsData(unconfirmedNonce);
                this.unconfirmedNonces.add(unconfirmedNonce);
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
        int unconfirmAmountCount = byteBuffer.readUint16();
        for (int i = 0; i < unconfirmAmountCount; i++) {
            try {
                UnconfirmedAmount unconfirmedAmount = new UnconfirmedAmount();
                byteBuffer.readNulsData(unconfirmedAmount);
                this.unconfirmedAmounts.add(unconfirmedAmount);
            } catch (Exception e) {
                throw new NulsException(e);
            }
        }
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
        size += SerializeUtils.sizeOfString(dbNonce);
        size += SerializeUtils.sizeOfUint16();
        for (UnconfirmedNonce unconfirmedNonce : unconfirmedNonces) {
            size += SerializeUtils.sizeOfNulsData(unconfirmedNonce);
        }
        size += SerializeUtils.sizeOfUint16();
        for (UnconfirmedAmount unconfirmedAmount : unconfirmedAmounts) {
            size += SerializeUtils.sizeOfNulsData(unconfirmedAmount);
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


    public List<UnconfirmedNonce> getUnconfirmedNonces() {
        return unconfirmedNonces;
    }

    public void updateUnconfirmeAmounts() {
        int index = 0;
        for (UnconfirmedAmount unconfirmedAmount : unconfirmedAmounts) {
            index++;
            if (TimeUtil.getCurrentTime() - unconfirmedAmount.getTime() < LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME) {
                break;
            }
        }
        if (index > 0) {
            List<UnconfirmedAmount> list = new ArrayList<UnconfirmedAmount>();
            list.addAll(unconfirmedAmounts.subList(index, unconfirmedAmounts.size()));
            unconfirmedAmounts = list;
        }
    }

    public void setUnconfirmedNonces(List<UnconfirmedNonce> unconfirmedNonces) {
        this.unconfirmedNonces = unconfirmedNonces;
    }

    public List<UnconfirmedAmount> getUnconfirmedAmounts() {
        return unconfirmedAmounts;
    }

    public void setUnconfirmedAmounts(List<UnconfirmedAmount> unconfirmedAmounts) {
        this.unconfirmedAmounts = unconfirmedAmounts;
    }

    public String getDbNonce() {
        return dbNonce;
    }

    public void setDbNonce(String dbNonce) {
        this.dbNonce = dbNonce;
    }

    public String getDbHash() {
        return dbHash;
    }

    public void setDbHash(String dbHash) {
        this.dbHash = dbHash;
    }
}
