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

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.RuntimeInfo;
import org.junit.Test;

/**
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class WsM2 {
    @Test
    public void asset() throws Exception {
        RuntimeInfo.kernelUrl = "ws://127.0.0.1:8887";
        CmdDispatcher.syncKernel();

//        System.out.println(CmdDispatcher.call("asset", new Object[]{(short) 188, (short) 765}, 1.0));
//
//        System.out.println(CmdDispatcher.call("assetReg", new Object[]{188, 867, "G", "Gold", 20000, 88888888, 7, false}, 1.0));
//        System.out.println(CmdDispatcher.call("assetReg", new Object[]{188, 765, "G", "Gold", 20000, 77777777, 7, false}, 1.0));
//        System.out.println(CmdDispatcher.call("assetReg", new Object[]{188, 801, "G", "Gold", 20000, 11111111, 7, false}, 1.0));
//
//        System.out.println(CmdDispatcher.call("asset", new Object[]{(short) 188, (short) 765}, 1.0));
//
//        System.out.println(CmdDispatcher.call("assetList", new Object[]{188}, 1.0));
//
//        System.out.println(CmdDispatcher.call("assetCurrNumOfChain", new Object[]{(short) 188, (short) 765, 765765765}, 1.0));
//
//        System.out.println(CmdDispatcher.call("asset", new Object[]{(short) 188, (short) 765}, 1.0));


    }

    @Test
    public void chain() throws Exception {
        RuntimeInfo.kernelUrl = "ws://127.0.0.1:8887";
        CmdDispatcher.syncKernel();

//        System.out.println(CmdDispatcher.call("chain", new Object[]{(short) 99}, 1.0));
//        System.out.println(CmdDispatcher.call("chain", new Object[]{(short) 188}, 1.0));
//
//        System.out.println(CmdDispatcher.call("chainReg", new Object[]{(short) 188, "ilovess", "NULS", 19870921, true, 5, 10, 8}, 1.0));
//
//        System.out.println(CmdDispatcher.call("chain", new Object[]{(short) 188}, 1.0));
    }
}
