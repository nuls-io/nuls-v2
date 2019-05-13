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

package io.nuls.transaction.tx.compare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2019/5/13
 */
public class TestSort {

    private static List<CompareObj> createObj(){
        List<CompareObj> list = new ArrayList<>();
        CompareObj cb = new CompareObj(1, 1);


        list.add(new CompareObj(1, 5));
        list.add(new CompareObj(1, 4));
        list.add(new CompareObj(1, 5));
        /*for(int i = 0;i<100000;i++) {
            list.add(cb);
            list.add(cb);

            list.add(new CompareObj(1, null));
            list.add(new CompareObj(1, "2"));
            list.add(new CompareObj(1, "abcde"));
            list.add(new CompareObj(1, "abc"));
            list.add(new CompareObj(1, "abcd"));
       }*/
        return list;
    }

    public static void main(String[] args) {
//        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
//        TestComparator testComparator = new TestComparator();
//        List<CompareObj> list = createObj();
//        list.sort(testComparator);
//        for (CompareObj compareObj : list){
//            System.out.println(compareObj.getB());
//        }

        Integer[] array =
                {0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                        0, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 2, 1, 0, 0, 0, 2, 30, 0, 3};
        TestComparator2 testComparator2 = new TestComparator2();
        List<Integer> list2 = Arrays.asList(array);

        list2.sort(testComparator2);
        for (Integer integer : list2){
            System.out.println(integer);
        }
    }
}
