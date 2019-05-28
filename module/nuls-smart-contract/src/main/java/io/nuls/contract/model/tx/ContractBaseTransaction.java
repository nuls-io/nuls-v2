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
package io.nuls.contract.model.tx;

import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.core.exception.NulsException;

import java.io.IOException;

/**
 * @author: PierreLuo
 * @date: 2019-03-07
 */
public abstract class ContractBaseTransaction<T extends BaseNulsData> extends Transaction {

    private ContractResult contractResult;

    private CoinData coinDataObj;

    private T txDataObj;

    protected ContractBaseTransaction() {
    }

    protected ContractBaseTransaction(int txType) {
        super(txType);

    }

    public CoinData getCoinDataObj() throws NulsException {
        if (coinDataObj == null) {
            CoinData coinData = new CoinData();
            coinData.parse(this.getCoinData(), 0);
            coinDataObj = coinData;
        }
        return coinDataObj;
    }

    public T getTxDataObj() throws NulsException {
        if (txDataObj == null) {
            txDataObj = newInstance();
            if (txDataObj != null) {
                txDataObj.parse(this.getTxData(), 0);
            }
        }
        return txDataObj;
    }

    protected abstract T newInstance();

    public void serializeData() throws IOException {
        if (this.getCoinData() == null && coinDataObj != null) {
            this.setCoinData(coinDataObj.serialize());
        }
        if (this.getTxData() == null && txDataObj != null) {
            this.setTxData(txDataObj.serialize());
        }
    }

    public void copyTx(Transaction tx) {
        this.setType(tx.getType());
        this.setCoinData(tx.getCoinData());
        this.setTxData(tx.getTxData());
        this.setTime(tx.getTime());
        this.setTransactionSignature(tx.getTransactionSignature());
        this.setRemark(tx.getRemark());
    }

    public ContractResult getContractResult() {
        return contractResult;
    }

    public void setContractResult(ContractResult contractResult) {
        this.contractResult = contractResult;
    }

    public void setCoinDataObj(CoinData coinDataObj) {
        this.coinDataObj = coinDataObj;
    }

    public void setTxDataObj(T txDataObj) {
        this.txDataObj = txDataObj;
    }
}
