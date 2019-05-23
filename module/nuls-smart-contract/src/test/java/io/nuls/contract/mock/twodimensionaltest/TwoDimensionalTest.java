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
package io.nuls.contract.mock.twodimensionaltest;

import org.junit.Test;

import java.lang.reflect.Array;

/**
 * @author: PierreLuo
 * @date: 2019-04-28
 */
public class TwoDimensionalTest {

    @Test
    public void test() {
        String[][] qwe = {new String[]{"1","2","3"}, new String[]{"4","5","6"}};
        Object qweObject = qwe;
        System.out.println(qweObject instanceof String[][]);
        System.out.println(qweObject.getClass().getName());
        String[][] result2Array = (String[][]) qweObject;
        String result = "";
        for(String[] ss : result2Array) {
            result += ss + " - [";
            for(String s1 : ss) {
                result += s1 + "\n";
            }
            result += "]";
        }
        System.out.println(result);
    }

    @Test
    public void new2ArrayTest() {
        String[][] resultArray = {new String[]{"1","2","3"}, new String[]{"4","5","6"}};
        Object o = Array.newInstance(String[].class, 2);
        int i = 0;
        for(String[] valueArray : resultArray) {
            Array.set(o, i++, valueArray);
        }
        System.out.println(o.toString());
    }
}
