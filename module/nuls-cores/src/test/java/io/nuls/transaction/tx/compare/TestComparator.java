/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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

import java.util.Comparator;

/**
 * @author: Charlie
 * @date: 2019/5/13
 */
public class TestComparator implements Comparator<CompareObj> {

    @Override
    public int compare(CompareObj o1, CompareObj o2) {

        if(o1.getA() < o2.getA()){
            return -1;
        } else if(o1.getA() > o2.getA()){
            return 1;
        }else{
            if(o1 == null || o2 == null){
                return 0;
            }
            if(o1 == null){
                return 0;
            }
            if(o2 == null){
                return 0;
            }
            if(o1.getB() < o2.getB()){
                return -1;
            }
            if(o1.getB()>o2.getB()){
                return 1;
            }
            return 0;
        }
    }
}
