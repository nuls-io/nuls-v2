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

package io.nuls.transaction.model.bo;

import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.utils.LoggerUtil;
import io.nuls.transaction.utils.TxUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 孤儿交易封装处理类
 * 按照交易nonce链，一笔交易可以有多个上笔交易(多个from), 但只有一笔下一个交易
 *
 * @author: Charlie
 * @date: 2019/5/6
 */
public class Orphans {

    /**
     * 当前孤儿交易
     */
    private TransactionNetPO tx;

    /**
     * 能连上当前交易的下一笔交易
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
     * 获取当前交易nonce集合
     * @return
     * @throws NulsException
     */
   /* public List<byte[]> getNonce() {
        List<byte[]> nonceList = new ArrayList<>();
        for (CoinFrom coinFrom : this.coinFromList) {
            nonceList.add(coinFrom.getNonce());
        }
        return nonceList;
    }*/


    /**
     * 判断传入的交易是否是本交易的上一笔交易
     * 本交易from中是否有nonce与传入交易的hash后8位匹配
     * @param txNet
     * @return
     */
    public boolean isNextTx(TransactionNetPO txNet) {
        byte[] hashSuffix = TxUtil.getNonce(txNet.getTx().getHash().getDigestBytes());
        String test = HexUtil.encode(hashSuffix);
        for (CoinFrom coinFrom : this.coinFromList) {
            String test2 = HexUtil.encode(coinFrom.getNonce());
            if(Arrays.equals(hashSuffix, coinFrom.getNonce())){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断传入的交易是否是本交易的后一笔交易
     * 传入交易from中是否有nonce与本交易的hash后8位匹配
     * @param txNet
     * @return
     */
    public boolean isPrevTx(TransactionNetPO txNet) {
        try {
            CoinData coinData = txNet.getTx().getCoinDataInstance();
            byte[] hashSuffix = TxUtil.getNonce(this.tx.getTx().getHash().getDigestBytes());
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
