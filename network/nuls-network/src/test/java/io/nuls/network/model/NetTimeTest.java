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
package io.nuls.network.model;

import io.nuls.network.manager.TimeManager;
import io.nuls.network.model.dto.NetTimeUrl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lan
 * @description
 * @date 2018/12/06
 **/
public class NetTimeTest {
    @Test
    public void listTest(){
        List<NetTimeUrl> list = new ArrayList<>();
        NetTimeUrl netTimeUrl1 = new NetTimeUrl("a",111);
        NetTimeUrl netTimeUrl2 = new NetTimeUrl("b",2222);
        NetTimeUrl netTimeUrl3 = new NetTimeUrl("c",333);
        NetTimeUrl netTimeUrl4 = new NetTimeUrl("d",155);
        list.add(netTimeUrl1);
        list.add(netTimeUrl2);
        list.add(netTimeUrl3);
        list.add(netTimeUrl4);
        Collections.sort(list);
        for(int i=0;i<list.size();i++){
            System.out.println(list.get(i).getUrl()+"==="+list.get(i).getTime());
        }

    }
    @Test
   public void timeServiceListTest(){
        TimeManager.getInstance().initWebTimeServer();
        List<NetTimeUrl> list =  TimeManager.getInstance().getNetTimeUrls();
        for(NetTimeUrl netTimeUrl:list){
            System.out.println(netTimeUrl.getTime() + "================"+netTimeUrl.getUrl());
        }
   }
}
