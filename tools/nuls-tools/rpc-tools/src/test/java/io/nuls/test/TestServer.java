/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.test;

import io.nuls.rpc.model.RpcCmd;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/9
 * @description
 */
public class TestServer {

    @Test
    public void test() throws Exception {

        RpcCmd rpcCmd = new RpcCmd();
        rpcCmd.setCmd("开火");
        rpcCmd.setMinVersion(2.3);
        rpcCmd.setParams(new Object[]{"a", 1, true});

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(rpcCmd);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        for (byte b : bytes)
            System.out.print(b);
        System.out.println();
        System.out.println(bytes.length);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        RpcCmd rpcCmd1 = (RpcCmd) objectInputStream.readObject();
        System.out.println(rpcCmd1.getCmd());

        System.out.println("===============================");

        List<Integer> list= Collections.synchronizedList(new ArrayList<>());
        for(int i=0;i<10;i++){
            list.add(i);
        }
        System.out.println(list.get(5));
        list.remove(5);
        System.out.println(list.get(5));

        for(int i=0;i<list.size();i++){
            System.out.println(list.get(i));
        }
    }
}
