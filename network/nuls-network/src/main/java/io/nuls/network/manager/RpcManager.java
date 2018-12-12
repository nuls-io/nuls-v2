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

import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;

/**
 * @description  Rpc init
 * @author lan
 * @date 2018/11/07
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
            // Start server instance
            WsServer.getInstance(ModuleE.NW)
                    .moduleRoles(ModuleE.NW.abbr, new String[]{"1.1", "1.2"})
                    .moduleVersion("1.2")
                    .dependencies(ModuleE.KE.abbr, "1.1")
                    .scanPackage("io.nuls.network.rpc")
                    .connect("ws://127.0.0.1:8887");
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
