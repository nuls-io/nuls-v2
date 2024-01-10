/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.transaction.model.bo;

import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.utils.LoggerUtil;
import io.nuls.transaction.utils.TxUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Orphan transaction encapsulation processing class
 * According to the transactionnonceChain, a transaction can have multiple previous transactions(Multiplefrom), But there's only one next transaction
 *
 * @author: Charlie
 * @date: 2019/5/6
 */
public class Orphans {

    /**
     * Current Orphan Trading
     */
    private TransactionNetPO tx;

    /**
     * The next transaction that can connect to the current transaction
     */
    private Orphans next;

    private List<CoinFrom> coinFromList;

    public Orphans(TransactionNetPO tx) {
        this.tx = tx;
        this.coinFromList = getCoinFromList();
    }

    public Orphans(TransactionNetPO tx, Orphans next) {
        this.tx = tx;
        this.next = next;
        this.coinFromList = getCoinFromList();
    }

    public TransactionNetPO getTx() {
        return tx;
    }

    public void setTx(TransactionNetPO tx) {
        this.tx = tx;
    }

    public Orphans getNext() {
        return next;
    }

    public void setNext(Orphans next) {
        this.next = next;
    }

    private List<CoinFrom> getCoinFromList(){
        try {
            CoinData coinData = this.tx.getTx().getCoinDataInstance();
            return coinData.getFrom();
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
            return new ArrayList<>();
        }
    }


    /**
     * Determine whether the incoming transaction is the previous transaction of this transaction
     * This transactionfromDoes it exist in the middlenonceRelated to incoming transactionshashafter8Bit matching
     * @param txNet
     * @return
     */
    public boolean isNextTx(TransactionNetPO txNet) {
        byte[] hashSuffix = TxUtil.getNonce(txNet.getTx().getHash().getBytes());
        for (CoinFrom coinFrom : this.coinFromList) {
            if(Arrays.equals(hashSuffix, coinFrom.getNonce())){
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the incoming transaction is the next transaction of this transaction
     * Incoming transactionfromDoes it exist in the middlenonceRelated to this transactionhashafter8Bit matching
     * @param txNet
     * @return
     */
    public boolean isPrevTx(TransactionNetPO txNet) {
        try {
            CoinData coinData = txNet.getTx().getCoinDataInstance();
            byte[] hashSuffix = TxUtil.getNonce(this.tx.getTx().getHash().getBytes());
            for (CoinFrom coinFrom : coinData.getFrom()) {
                if(Arrays.equals(hashSuffix, coinFrom.getNonce())){
                    return true;
                }
            }
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
        }
        return false;
    }
}
