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

package io.nuls.transaction.utils;

import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.transaction.model.po.TransactionNetPO;

import java.util.Arrays;
import java.util.Comparator;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2018-12-11
 */
@Component
public class TransactionComparator implements Comparator<TransactionNetPO> {

    @Override
    public int compare(TransactionNetPO txNeto1, TransactionNetPO txNeto2) {

        if (null == txNeto1 && null == txNeto2) {
            return 0;
        }
        if(null == txNeto1){
            return 1;
        }
        if(null == txNeto2){
            return -1;
        }

        Transaction o1 = txNeto1.getTx();
        Transaction o2 = txNeto2.getTx();
        if (null == o1 && null == o2) {
            return 0;
        }
        if(null == o1){
            return 1;
        }
        if(null == o2){
            return -1;
        }
        if (o1.equals(o2)) {
            return 0;
        }
        if (o1.getTime() < o2.getTime()) {
            return -1;
        } else if (o1.getTime() > o2.getTime()) {
            return 1;
        } else {
            //比较交易hash和nonce的关系
            try {
                if (null == o1.getCoinData() && null == o2.getCoinData()) {
                    return 0;
                }
                if (null == o1.getCoinData()) {
                    return 1;
                }
                if (null == o2.getCoinData()) {
                    return -1;
                }

                CoinData o1CoinData = o1.getCoinDataInstance();
                CoinData o2CoinData = o2.getCoinDataInstance();
                if (null == o1CoinData && null == o2CoinData) {
                    return 0;
                }
                if (null == o1CoinData) {
                    return 1;
                }
                if (null == o2CoinData) {
                    return -1;
                }
                if (null == o1CoinData.getFrom() && null == o2CoinData.getFrom()) {
                    return 0;
                }
                if (null == o1CoinData.getFrom()) {
                    return 1;
                }
                if (null == o2CoinData.getFrom()) {
                    return -1;
                }
                byte[] o2HashPrefix = TxUtil.getNonce(o2.getHash());
                for (CoinFrom o1CoinFrom : o1CoinData.getFrom()) {
                    if (Arrays.equals(o2HashPrefix, o1CoinFrom.getNonce())) {
                        //o1其中一个账户的nonce等于o2的hash，则需要交换位置(说明o2是o1的前一笔交易)
                        //命中一个from直接返回
                        return 1;
                    }
                }

                byte[] o1HashPrefix = TxUtil.getNonce(o1.getHash());
                for (CoinFrom o2CoinFrom : o2CoinData.getFrom()) {
                    if (Arrays.equals(o1HashPrefix, o2CoinFrom.getNonce())) {
                        //o2其中一个账户的nonce等于o1的hash，则不需要交换位置(说明o1是o2的前一笔交易)
                        //命中一个from直接返回
                        return -1;
                    }
                }
            } catch (NulsException e) {
                LOG.error(e);
            }finally {
                return 0;
            }
        }
    }

}
