package io.nuls.transaction.tx;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 */
public class EntitySortTest {


    public static void main(String[] args) {
        List<SortEntity> list = new ArrayList<>();
        list.add(new SortEntity(6, 1, 5));
        list.add(new SortEntity(4, 1, 3));
        list.add(new SortEntity(9, 1, 8));
        list.add(new SortEntity(3, 1, 2));
        list.add(new SortEntity(8, 1, 7));
        list.add(new SortEntity(7, 1, 6));
        list.add(new SortEntity(10, 1, 9));
        list.add(new SortEntity(1, 1, 0));
        list.add(new SortEntity(5, 1, 4));
        list.add(new SortEntity(2, 1, 1));
        list.add(new SortEntity(11, 1, 1));
        list.add(new SortEntity(12, 1, 10));

//        list.add(new SortEntity(18, 1, 17));
//        list.add(new SortEntity(13, 1, 12));
//        list.add(new SortEntity(5, 1, 4));
//        list.add(new SortEntity(15, 1, 14));
//        list.add(new SortEntity(8, 1, 7));
//        list.add(new SortEntity(12, 1, 11));
//        list.add(new SortEntity(3, 1, 2));
//        list.add(new SortEntity(4, 1, 3));
//        list.add(new SortEntity(19, 1, 18));
//        list.add(new SortEntity(1, 1, 0));
//        list.add(new SortEntity(16, 1, 15));
//        list.add(new SortEntity(14, 1, 13));
//        list.add(new SortEntity(6, 1, 5));
//        list.add(new SortEntity(10, 1, 9));
//        list.add(new SortEntity(2, 1, 1));
//        list.add(new SortEntity(20, 1, 19));
//        list.add(new SortEntity(7, 1, 6));
//        list.add(new SortEntity(17, 1, 16));
//        list.add(new SortEntity(9, 1, 8));
//        list.add(new SortEntity(11, 1, 10));

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
        boolean gotFront = false;
        boolean gotNext = false;
        int gotIndex = -1;
        boolean added = false;
        for (int i = result.getIndex(); i >= 0; i--) {
            SortItem<SortEntity> item = array[i];
            int val = ((Comparable<SortEntity>) thisItem.getObj()).compareTo(item.getObj());
            if (val == 1 && !gotNext) {
                item.setFlower(new SortItem[]{thisItem});
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
                SortItem<SortEntity>[] flower = new SortItem[1];
                int count = 0;
                if (hasFlower) {
                    count = 1;
                }
                int realCount = 1;
                for (int x = 1; x <= count; x++) {
                    SortItem flr = array[x + gotIndex];
                    flower[x - 1] = flr;
                    realCount++;
                    if (x == count && flr.isHasFlower()) {
                        count += 1;
                        SortItem<SortEntity>[] flower2 = new SortItem[flower.length + 1];
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
                SortItem<SortEntity>[] flower = new SortItem[count];
                for (int x = 1; x <= count; x++) {
                    SortItem flr = array[x + i];
                    flower[x - 1] = flr;
                    realCount++;
                    if (x == count && flr.isHasFlower()) {
                        count += 1;
                        SortItem<SortEntity>[] flower2 = new SortItem[flower.length + 1];
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
                insertArray(gotIndex + 1 - realCount, result, result.getIndex() + 1, thisItem, true);
                return;

            }
        }
        if (!added) {
            insertArray(result.getIndex() + 1, result, result.getIndex() + 1, thisItem, false);
        }
    }

    private static void insertArray(int index, SortResult result, int length, SortItem item, boolean insertFlowers) {
        SortItem[] array = result.getArray();
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
        private SortItem[] flower = new SortItem[0];
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

        public boolean isHasFlower() {
            return hasFlower;
        }

        public void setHasFlower(boolean hasFlower) {
            this.hasFlower = hasFlower;
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
