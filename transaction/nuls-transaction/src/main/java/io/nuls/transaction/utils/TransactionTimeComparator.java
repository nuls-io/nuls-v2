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
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.TxWrapper;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author: Charlie
 * @date: 2018-12-11
 */
public class TransactionTimeComparator implements Comparator<TxWrapper> {

    private static TransactionTimeComparator instance = new TransactionTimeComparator();

    public static TransactionTimeComparator getInstance() {
        return instance;
    }

    private TransactionTimeComparator() {
    }

    @Override
    public int compare(TxWrapper t1, TxWrapper t2) {
        Transaction o1 = t1.getTx();
        Transaction o2 = t2.getTx();
        if(o1.getHash().equals(o2.getHash())){
            return 0;
        }
        if(o1.getTime() < o2.getTime()){
            return -1;
        }else if(o1.getTime() > o2.getTime()){
            return 1;
        }else{
            //比较交易hash和nonce的关系
            try {
                if(null == o1.getCoinData() || null == o2.getCoinData()) {
                    return 0;
                }
                CoinData o1CoinData = o1.getCoinDataInstance();
                if(null == o1CoinData.getFrom()){
                    return 0;
                }
                for(CoinFrom coinFrom : o1CoinData.getFrom()){
                    byte[] o2Hash = o2.getHash().getDigestBytes();
                    byte[] o2HashPrefix = Arrays.copyOfRange(o2Hash, 0 ,7);
                    if(Arrays.equals(o2HashPrefix, coinFrom.getNonce())){
                        //o1其中一个账户的nonce等于o2的hash，则需要交换位置(说明o2是o1的前一笔交易)
                        //命中一个from直接返回
                        return 1;
                    }
                }

                CoinData o2CoinData = o2.getCoinDataInstance();
                if(null == o2CoinData.getFrom()){
                    return 0;
                }
                for(CoinFrom coinFrom : o2CoinData.getFrom()){
                    byte[] o1Hash = o1.getHash().getDigestBytes();
                    byte[] o1HashPrefix = Arrays.copyOfRange(o1Hash, 0 ,7);
                    if(Arrays.equals(o1HashPrefix, coinFrom.getNonce())){
                        //o2其中一个账户的nonce等于o1的hash，则不需要交换位置(说明o1是o2的前一笔交易)
                        //命中一个from直接返回
                        return -1;
                    }
                }
            } catch (NulsException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }
}
