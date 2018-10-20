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

package io.nuls.rpc.cmd_kernel;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.CmdInfo;
import io.nuls.rpc.info.RpcInfo;
import io.nuls.rpc.model.Module;
import io.nuls.tools.parse.JSONUtils;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/17
 * @description
 */
public class KernelCmd extends BaseCmd {

    @CmdInfo(cmd = "join", version = 1.0, preCompatible = true)
    public Object join(List params) {
        try {
            System.out.println("触发Kernel的Join操作");
            System.out.println("join之前的remote接口数：" + RpcInfo.remoteModuleMap.size());
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(params.get(0)), Module.class);
            System.out.println(module.getName() + " added");
            RpcInfo.remoteModuleMap.put(module.getName(), module);
            System.out.println("join之后的remote接口数：" + RpcInfo.remoteModuleMap.size());

            return SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @CmdInfo(cmd = "fetch", version = 1.0, preCompatible = true)
    public Object fetch(List params) {
        return RpcInfo.remoteModuleMap;
    }
}
