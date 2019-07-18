package io.nuls.transaction.tx;

import io.nuls.transaction.model.po.TransactionNetPO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 */
public class TxCompareTool {


    public static int compareTo(TransactionNetPO tx1, TransactionNetPO tx2) {
        //只处理相同时间的排序
//        if (tx1.getOriginalSendNanoTime() > tx2.getOriginalSendNanoTime()) {
//            return 1;
//        } else if (tx1.getOriginalSendNanoTime() < tx2.getOriginalSendNanoTime()) {
//            return -1;
//        }
        //todo 这里判断是否是连续交易

        return 0;
    }

    static class SortResult<T> {
        private SortItem[] array;
        private int index = -1;

        public SortResult(int totalLength) {
            array = new SortItem[totalLength];
        }

        public List<T> getList() {
            List<T> list = new ArrayList<>();
            for (SortItem t : array) {
                list.add((T) t.getObj());
            }
            return list;
        }

        public SortItem[] getArray() {
            return array;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }


    static class SortItem<T> {
        private T obj;
        private SortItem[] flower;

        public SortItem(T obj) {
            this.obj = obj;
        }

        public SortItem[] getFlower() {
            return flower;
        }

        public void setFlower(SortItem[] flower) {
            this.flower = flower;
        }

        public T getObj() {
            return obj;
        }

        public void setObj(T obj) {
            this.obj = obj;
        }

        public int getFlowerCount() {
            if (null == flower) {
                return 0;
            }
            return flower.length;
        }

    }
}
