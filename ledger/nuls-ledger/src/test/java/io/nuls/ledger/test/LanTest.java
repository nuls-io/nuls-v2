/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.ledger.test;

import io.nuls.ledger.model.po.FreezeHeightState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 * @description
 * @date 2019/01/07
 **/
public class LanTest {
    @Test
    public void test1(){
        List<FreezeHeightState> a = new ArrayList<FreezeHeightState>();
//        FreezeHeightState f1= new FreezeHeightState();
//        f1.setHeight(BigInteger.valueOf(23));
//        a.add(f1);
//        FreezeHeightState f2= new FreezeHeightState();
//        f2.setHeight(BigInteger.valueOf(24));
//        a.add(f2);
//        FreezeHeightState f3= new FreezeHeightState();
//        f3.setHeight(BigInteger.valueOf(19));
//        a.add(f3);
        a.sort((x, y) -> Long.compare(x.getHeight(),y.getHeight()));
       for(FreezeHeightState freezeHeightState:a){
           System.out.println(freezeHeightState.getHeight());
       }
    }
}
