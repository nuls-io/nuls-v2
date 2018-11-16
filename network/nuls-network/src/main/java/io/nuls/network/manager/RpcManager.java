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
package io.nuls.network.manager;

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.server.WsServer;

/**
 * @program: nuls2.0
 * @description: Rpc init
 * @author: lan
 * @create: 2018/11/07
 **/
public class RpcManager extends BaseManager{
    private static RpcManager instance = new RpcManager();
    public static RpcManager getInstance(){
        return instance;
    }
    @Override
    public void init() {

    }

    @Override
    public void start() {
        try {
            /*
             * 初始化websocket服务器，供其他模块调用本模块接口
             * 端口随机，会自动分配未占用端口
             */
            WsServer s = new WsServer(HostInfo.randomPort());
            /*
             * 初始化，参数说明：
             * 1. 本模块的code
             * 2. 依赖的模块的code，类型为String[]
             * 3. 本模块提供的对外接口所在的包路径
             */
            s.init("nw", null, "io.nuls.network.rpc");
            /*
             * 启动服务
             */
            s.start();
            /*
             * 向核心模块汇报本模块信息
             */
            CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
