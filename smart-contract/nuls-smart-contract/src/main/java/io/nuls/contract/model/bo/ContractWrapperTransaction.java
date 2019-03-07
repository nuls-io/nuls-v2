/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.model.bo;

import io.nuls.base.constant.TxStatusEnum;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.tools.exception.NulsException;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Collection;

/**
 * @author: PierreLuo
 * @date: 2019-02-27
 */
@Getter
@Setter
public class ContractWrapperTransaction extends Transaction{

    private Transaction tx;

    private ContractResult contractResult;

    private ContractData contractData;

    private int order;

    private transient BlockHeader blockHeader;

    public ContractWrapperTransaction(Transaction tx, ContractData contractData) {
        this.tx = tx;
        this.contractData = contractData;
    }

    public byte[] getTxData() {
        return tx.getTxData();
    }

    public long getTime() {
        return tx.getTime();
    }

    public int getType() {
        return tx.getType();
    }

    public byte[] getRemark() {
        return tx.getRemark();
    }

    public NulsDigestData getHash() {
        return tx.getHash();
    }

    public byte[] getTransactionSignature() {
        return tx.getTransactionSignature();
    }

    public long getBlockHeight() {
        return tx.getBlockHeight();
    }

    public TxStatusEnum getStatus() {
        return tx.getStatus();
    }

    public byte[] getCoinData() {
        return tx.getCoinData();
    }

    public int getInBlockIndex() {
        return tx.getInBlockIndex();
    }

    public CoinData getCoinDataInstance() throws NulsException {
        return tx.getCoinDataInstance();
    }

    public int getSize() {
        return tx.getSize();
    }

    public String hex() throws Exception {
        return tx.hex();
    }

    public BigInteger getFee() throws NulsException {
        return tx.getFee();
    }

    public boolean isMultiSignTx()throws NulsException {
        return tx.isMultiSignTx();
    }

    @Override
    public boolean equals(Object obj) {
        return tx.equals(obj);
    }
}
