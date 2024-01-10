package io.nuls.transaction.tx;

import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.utils.TxUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author Niels
 */
public class TxCompareTool {


    public static int compareTo(TransactionNetPO txNeto1, TransactionNetPO txNeto2) {
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

        //Comparative transactionshashandnonceThe relationship between
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
                    //o1One of the accountsnonceequal too2ofhash, then it is necessary to exchange positions(explaino2yeso1The previous transaction of)
                    //Hit onefromDirectly return
                    return 1;
                }
            }

            byte[] o1HashPrefix = TxUtil.getNonce(o1.getHash().getBytes());
            for (CoinFrom o2CoinFrom : o2CoinData.getFrom()) {
                if (Arrays.equals(o1HashPrefix, o2CoinFrom.getNonce())) {
                    //o2One of the accountsnonceequal too1ofhashThen there is no need to swap positions(explaino1yeso2The previous transaction of)
                    //Hit onefromDirectly return
                    return -1;
                }
            }
            return 0;
        } catch (NulsException e) {
            LOG.error(e);
            return 0;
        }

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
        private SortItem[] flower= new SortItem[0];
        private boolean hasFlower;

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

//        public int getFlowerCount() {
//            if (null == flower) {
//                return 0;
//            }
//            return flower.length;
//        }

        public boolean isHasFlower() {
            return hasFlower;
        }

        public void setHasFlower(boolean hasFlower) {
            this.hasFlower = hasFlower;
        }
    }
}
