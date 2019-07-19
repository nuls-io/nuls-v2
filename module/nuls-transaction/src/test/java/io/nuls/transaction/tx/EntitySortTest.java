package io.nuls.transaction.tx;

import java.util.*;

/**
 * @author Niels
 */
public class EntitySortTest {


    public static void main(String[] args) {
        List<SortEntity> list = new ArrayList<>();
        list.add(new SortEntity(12, 1, 11));
        list.add(new SortEntity(1, 1, 0));
        list.add(new SortEntity(4, 1, 3));
        list.add(new SortEntity(3, 1, 2));
        list.add(new SortEntity(5, 1, 4));
        list.add(new SortEntity(11, 1, 10));
        list.add(new SortEntity(8, 1, 7));
        list.add(new SortEntity(6, 1, 5));
        list.add(new SortEntity(2, 1, 1));
        list.add(new SortEntity(7, 1, 6));
        list.add(new SortEntity(9, 1, 8));
        list.add(new SortEntity(10, 1, 9));
        SortResult<SortEntity> result = new SortResult<>(list.size());
        list.forEach(sortEntity -> {
            doRank(result, new SortItem<>(sortEntity));
        });
        for (SortEntity sortEntity : result.getList()) {
            System.out.println(sortEntity.getId());
        }
    }

    private static void doRank(SortResult<SortEntity> result, SortItem thisItem) {
        if (result.getIndex() == -1) {
            result.getArray()[0] = thisItem;
            result.setIndex(0);
            return;
        }
        SortItem[] array = result.getArray();
        for (int i = result.getIndex(); i >= 0; i--) {
            SortItem<SortEntity> item = array[i];
            int val = ((Comparable<SortEntity>) thisItem.getObj()).compareTo(item.getObj());
            if (val == 1) {
                insertArray(i + 1, result, result.getIndex() + 1, thisItem);
                return;
            }
            if (val == -1) {
                int count = item.getFlowerCount();
                SortItem<SortEntity>[] flower = new SortItem[count + 1];
                flower[0] = item;
                for (int x = 1; x <= count; x++) {
                    flower[x] = array[x + i];
                }
                thisItem.setFlower(flower);
                // 前移后面的元素
                for (int x = count + 1; x <= result.getIndex() - i; x++) {
                    array[i + x - count - 1] = array[i + x];
                    array[i + x] = null;
                }
                result.setIndex(result.getIndex() - count - 1);
            }
        }
        insertArray(result.getIndex() + 1, result, result.getIndex() + 1, thisItem);
    }

    private static void insertArray(int index, SortResult result, int length, SortItem item) {
        SortItem[] array = result.getArray();
        int count = 1 + item.getFlowerCount();
        result.setIndex(result.getIndex() + count);
        if (length >= index) {
            for (int i = length - 1; i >= index; i--) {
                array[i + count] = array[i];
            }
        }
        array[index] = item;
        if (null == item.getFlower()) {
            return;
        }
        int add = 1;
        for (SortItem f : item.getFlower()) {
            array[index + add] = f;
            add++;
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

    static class SortEntity implements Comparable<SortEntity> {

        public SortEntity(long id, long time, long preId) {
            this.id = id;
            this.time = time;
            this.preId = preId;
        }

        private long id;

        private long time;

        private long preId;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getPreId() {
            return preId;
        }

        public void setPreId(long preId) {
            this.preId = preId;
        }

        @Override
        public int compareTo(SortEntity o2) {
//            System.out.println(o1.getId() + "--->" + o2.getId());
//            if (this.getTime() < o2.getTime()) {
//                return -1;
//            }
//            if (o2.getTime() < this.getTime()) {
//                return 1;
//            }
            //当时间相同后的排序逻辑
            if (this.getPreId() == o2.getId()) {
                return 1;
            }
            if (o2.getPreId() == this.getId()) {
                return -1;
            }
            return 0;
        }
    }
}
