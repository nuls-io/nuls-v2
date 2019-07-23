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
import io.nuls.transaction.model.bo.OrphanSortItem;
import io.nuls.transaction.model.bo.OrphanSortResult;
import io.nuls.transaction.model.po.TransactionNetPO;

import java.util.*;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2019/7/23
 */
@Component
public class OrphanSort {

    public void rank(List<TransactionNetPO> txList) {
        //分组：相同时间的一组，同时设置排序字段的值（10000*time），用于最终排序
        Map<Long, List<TransactionNetPO>> groupMap = new HashMap<>();
        for (TransactionNetPO tx : txList) {
            long second = tx.getTx().getTime();
            List<TransactionNetPO> subList = groupMap.get(second);
            if (null == subList) {
                subList = new ArrayList<>();
                groupMap.put(second, subList);
            }
            tx.setOrphanSortSerial(second * 10000);
            subList.add(tx);
        }
        //相同时间的组，进行细致排序，并更新排序字段的值
        for (List<TransactionNetPO> list : groupMap.values()) {
            this.sameTimeRank(list);
        }
        //重新排序
        Collections.sort(txList, new Comparator<TransactionNetPO>() {
            @Override
            public int compare(TransactionNetPO o1, TransactionNetPO o2) {
                if (o1.getOrphanSortSerial() > o2.getOrphanSortSerial()) {
                    return 1;
                } else if (o1.getOrphanSortSerial() < o2.getOrphanSortSerial()) {
                    return -1;
                }
                return 0;
            }
        });
    }


    private void sameTimeRank(List<TransactionNetPO> txList) {
        if (txList.size() <= 1) {
            return;
        }
        OrphanSortResult<TransactionNetPO> result = new OrphanSortResult<>(txList.size());
        txList.forEach(po -> {
            doRank(result, new OrphanSortItem<>(po));
        });
        int index = 0;
        for (TransactionNetPO po : result.getList()) {
            po.setOrphanSortSerial(po.getOrphanSortSerial() + (index++));
        }

    }

    private void doRank(OrphanSortResult<TransactionNetPO> result, OrphanSortItem<TransactionNetPO> thisItem) {
        if (result.getIndex() == -1) {
            result.getArray()[0] = thisItem;
            result.setIndex(0);
            return;
        }
        OrphanSortItem[] array = result.getArray();
        boolean gotFront = false;
        boolean gotNext = false;
        int gotIndex = -1;
        boolean added = false;
        for (int i = result.getIndex(); i >= 0; i--) {
            OrphanSortItem<TransactionNetPO> item = array[i];
            int val = this.compareTo(thisItem.getObj(), item.getObj());
            if (val == 1 && !gotNext) {
                item.setFlower(new OrphanSortItem[]{thisItem});
                item.setHasFlower(true);
                insertArray(i + 1, result, result.getIndex() + 1, thisItem, false);
                gotFront = true;
                gotIndex = i + 1;
                added = true;
            } else if (val == 1 && gotNext) {
//                需要找到之前的一串，挪动到现在的位置
                thisItem = result.getArray()[gotIndex];
                if (i == gotIndex - 1) {
                    item.setHasFlower(true);
                    return;
                }
                boolean hasFlower = thisItem.isHasFlower();
                OrphanSortItem<TransactionNetPO>[] flower = new OrphanSortItem[1];
                int count = 0;
                if (hasFlower) {
                    count = 1;
                }
                int realCount = 1;
                for (int x = 1; x <= count; x++) {
                    OrphanSortItem flr = array[x + gotIndex];
                    flower[x - 1] = flr;
                    realCount++;
                    if (x == count && flr.isHasFlower()) {
                        count += 1;
                        OrphanSortItem<TransactionNetPO>[] flower2 = new OrphanSortItem[flower.length + 1];
                        System.arraycopy(flower, 0, flower2, 0, flower.length);
                        flower = flower2;
                    }
                }
                thisItem.setFlower(flower);
                if (flower.length > 0) {
                    thisItem.setHasFlower(true);
                }
                item.setHasFlower(true);
                // 前移后面的元素
                for (int x = 0; x < result.getIndex() - gotIndex + 1; x++) {
                    int oldIndex = gotIndex + x + realCount;
                    if (oldIndex <= result.getIndex()) {
                        array[gotIndex + x] = array[oldIndex];
                        array[oldIndex] = null;
                    } else {
                        array[gotIndex + x] = null;
                    }
                }
                result.setIndex(result.getIndex() - realCount);
                insertArray(i + 1, result, result.getIndex() + 1, thisItem, true);
                return;
            } else if (val == -1 && !gotFront) {
                thisItem.setHasFlower(true);
                insertArray(i, result, result.getIndex() + 1, thisItem, false);
                gotNext = true;
                gotIndex = i;
                added = true;
            } else if (val == -1 && gotFront) {
                if (gotIndex == i - 1) {
                    return;
                }
                thisItem.setHasFlower(true);
                thisItem = result.getArray()[i];
                boolean hasFlower = thisItem.isHasFlower();
                int count = hasFlower ? 1 : 0;
                int realCount = 1;
                OrphanSortItem<TransactionNetPO>[] flower = new OrphanSortItem[count];
                for (int x = 1; x <= count; x++) {
                    OrphanSortItem flr = array[x + i];
                    flower[x - 1] = flr;
                    realCount++;
                    if (x == count && flr.isHasFlower()) {
                        count += 1;
                        OrphanSortItem<TransactionNetPO>[] flower2 = new OrphanSortItem[flower.length + 1];
                        System.arraycopy(flower, 0, flower2, 0, flower.length);
                        flower = flower2;
                    }
                }
                thisItem.setFlower(flower);
                // 前移后面的元素
                for (int x = 0; x < result.getIndex() - i + 1; x++) {
                    int oldIndex = i + x + realCount;
                    if (oldIndex <= result.getIndex()) {
                        array[i + x] = array[oldIndex];
                        array[oldIndex] = null;
                    } else {
                        array[i + x] = null;
                    }
                }
                result.setIndex(result.getIndex() - realCount);
                this.insertArray(gotIndex + 1 - realCount, result, result.getIndex() + 1, thisItem, true);
                return;
            }
        }
        if (!added) {
            this.insertArray(result.getIndex() + 1, result, result.getIndex() + 1, thisItem, false);
        }
    }

    private void insertArray(int index, OrphanSortResult result, int length, OrphanSortItem item, boolean insertFlowers) {
        OrphanSortItem[] array = result.getArray();
        int count = 1 + item.getFlower().length;
        result.setIndex(result.getIndex() + count);
        if (length >= index) {
            for (int i = length - 1; i >= index; i--) {
                array[i + count] = array[i];
            }
        }
        array[index] = item;
        if (null == item.getFlower() || !insertFlowers) {
            return;
        }
        int add = 1;
        for (OrphanSortItem f : item.getFlower()) {
            array[index + add] = f;
            add++;
        }
    }

    /**
     * 两个交易根据nonce 来排序
     * @param txNeto1
     * @param txNeto2
     * @return
     */
    private int compareTo(TransactionNetPO txNeto1, TransactionNetPO txNeto2) {
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
            byte[] o2HashPrefix = TxUtil.getNonce(o2.getHash().getBytes());
            for (CoinFrom o1CoinFrom : o1CoinData.getFrom()) {
                if (Arrays.equals(o2HashPrefix, o1CoinFrom.getNonce())) {
                    //o1其中一个账户的nonce等于o2的hash，则需要交换位置(说明o2是o1的前一笔交易)
                    //命中一个from直接返回
                    return 1;
                }
            }

            byte[] o1HashPrefix = TxUtil.getNonce(o1.getHash().getBytes());
            for (CoinFrom o2CoinFrom : o2CoinData.getFrom()) {
                if (Arrays.equals(o1HashPrefix, o2CoinFrom.getNonce())) {
                    //o2其中一个账户的nonce等于o1的hash，则不需要交换位置(说明o1是o2的前一笔交易)
                    //命中一个from直接返回
                    return -1;
                }
            }
            return 0;
        } catch (NulsException e) {
            LOG.error(e);
            return 0;
        }

    }

}
