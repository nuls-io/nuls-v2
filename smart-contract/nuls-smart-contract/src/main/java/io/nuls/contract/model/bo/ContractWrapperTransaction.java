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

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.contract.model.txdata.ContractData;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: PierreLuo
 * @date: 2019-02-27
 */
@Getter
@Setter
public class ContractWrapperTransaction extends Transaction {

    private String txHex;

    private ContractResult contractResult;

    private ContractData contractData;

    /**
     * 合约内部转账交易的时间的偏移量，用于排序，区块逻辑没有用到时间排序，暂时放弃
     */
    //private int order;

    private BlockHeader blockHeader;

    public ContractWrapperTransaction(Transaction tx, String txHex, ContractData contractData) {
        this.txHex = txHex;
        this.contractData = contractData;
        this.copyTx(tx);
    }

    private void copyTx(Transaction tx) {
        this.setType(tx.getType());
        this.setCoinData(tx.getCoinData());
        this.setTxData(tx.getTxData());
        this.setTime(tx.getTime());
        this.setTransactionSignature(tx.getTransactionSignature());
        this.setRemark(tx.getRemark());
    }
}
