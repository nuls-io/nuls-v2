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

package io.nuls.transaction.model;

import io.nuls.base.data.Transaction;

/**
 * 交易模块打包区块交易时对交易临时封装
 * @author: Charlie
 * @date: 2019/3/26
 */
public class TxWrapper{

    private Transaction tx;

    private int index;

    private String txHex;

    public TxWrapper(Transaction tx, int index) {
        this.tx = tx;
        this.index = index;
    }

    public TxWrapper(Transaction tx, int index, String txHex) {
        this.tx = tx;
        this.index = index;
        this.txHex = txHex;
    }

    public int compareTo(int index) {
        if (this.index > index) {
            return -1;
        } else if (this.index < index) {
            return 1;
        }
        return 0;
    }

    public Transaction getTx() {
        return tx;
    }

    public void setTx(Transaction tx) {
        this.tx = tx;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTxHex() {
        return txHex;
    }

    public void setTxHex(String txHex) {
        this.txHex = txHex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TxWrapper)) {
            return false;
        }
        return this.tx.equals(((TxWrapper) obj).getTx());
    }

    @Override
    public int hashCode() {
        return this.tx.getHash().hashCode();
    }
}
