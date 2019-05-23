package io.nuls.transaction.utils;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class HashSetDuplicateProcessorTest {

    @Test
    public void name() {
        List<Integer> list = List.of(100, 1000, 10000, 100000, 1000000, 10000000);
        list.forEach(this::test);
    }

    private void test(int size) {
        {
            long begin = System.nanoTime();
            HashSetDuplicateProcessor processor = new HashSetDuplicateProcessor(size);
            for (int i = 0; i < size * 2; i++) {
                processor.insertAndCheck(System.nanoTime()+"");
            }
            long end = System.nanoTime();
            System.out.println("HashSetDuplicateProcessor\t"+(end - begin));
        }
        {
            long begin = System.nanoTime();
            Set<Object> sizedSet = getSizedSet1(size);
            for (int i = 0; i < size * 2; i++) {
                sizedSet.add(System.nanoTime()+"");
            }
            long end = System.nanoTime();
            System.out.println("getSizedSet1\t\t\t\t"+(end - begin));
        }
        {
            long begin = System.nanoTime();
            Set<Object> sizedSet = getSizedSet2(size);
            for (int i = 0; i < size * 2; i++) {
                sizedSet.add(System.nanoTime()+"");
            }
            long end = System.nanoTime();
            System.out.println("getSizedSet2\t\t\t\t"+(end - begin));
        }
        {
            long begin = System.nanoTime();
            Set<Object> sizedSet = getSizedSet3(size);
            for (int i = 0; i < size * 2; i++) {
                sizedSet.add(System.nanoTime()+"");
            }
            long end = System.nanoTime();
            System.out.println("getSizedSet3\t\t\t\t"+(end - begin));
        }
        System.out.println("*****************************************************");
    }

    /**
     * 获取线程安全的固定大小的set
     *
     * @param size  set元素上限
     * @return Set  set对象
     */
    public static <T> Set<T> getSizedSet1(int size) {
        return new SizedSet<>(size);
    }
    static class SizedSet<T> extends ConcurrentSkipListSet<T>{
        private int size;

        public SizedSet(int size) {
            this.size = size;
        }

        @Override
        public boolean add(T t) {
            if (size() >= size) {
                pollLast();
            }
            return super.add(t);
        }
    }

    /**
     * 获取线程安全的固定大小的set
     *
     * @param size  set元素上限
     * @return Set  set对象
     */
    public static <T> Set<T> getSizedSet2(int size) {
        return Collections.synchronizedSet(new HashSet<>(){
            @Override
            public boolean add(T t) {
                if (size() >= size) {
                    clear();
                }
                return super.add(t);
            }
        });
    }

    /**
     * 获取线程安全的固定大小的set
     *
     * @param size  set元素上限
     * @return Set  set对象
     */
    public static <T> Set<T> getSizedSet3(int size) {
        return Collections.synchronizedSet(new TreeSet<>(){
            @Override
            public boolean add(T t) {
                if (size() >= size) {
                    pollLast();
                }
                return super.add(t);
            }
        });
    }
}