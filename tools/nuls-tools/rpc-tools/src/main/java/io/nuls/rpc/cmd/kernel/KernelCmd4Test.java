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

package io.nuls.rpc.cmd.kernel;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.rpc.model.Module;
import io.nuls.tools.parse.JSONUtils;

import java.util.*;

/**
 * this class is only used by testing.
 *
 * @author tangyi
 * @date 2018/10/17
 * @description
 */
public class KernelCmd4Test extends BaseCmd {

    @CmdAnnotation(cmd = "version", version = 1.0, preCompatible = true)
    public CmdResponse version(List params) {
        try {
            System.out.println("join之前的kernel remote接口数：" + RuntimeInfo.remoteModuleMap.size());
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(params.get(0)), Module.class);
            if (module != null) {
                System.out.println(module.getName() + " added");
                RuntimeInfo.remoteModuleMap.put(module.getName(), module);
            }
            System.out.println("join之后的kernel remote接口数：" + RuntimeInfo.remoteModuleMap.size());

            Map<String, Object> result = new HashMap<>(16);
            result.put("service", new String[]{"a", "b", "c"});
            result.put("available", true);
            result.put("modules", RuntimeInfo.remoteModuleMap);

            return success(1.0, null, result);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(Constants.INTERNAL_ERROR, 1.0, null);
        }
    }

    @CmdAnnotation(cmd = "fetch", version = 1.0, preCompatible = true)
    public Object fetch(List params) {
        Iterator<String> keyIterator = RuntimeInfo.remoteModuleMap.keySet().iterator();
        List<String> service = new ArrayList<>();
        while (keyIterator.hasNext()) {
            service.add(keyIterator.next());
        }

        Map<String, Object> result = new HashMap<>(16);
        result.put("service", service);
        result.put("modules", RuntimeInfo.remoteModuleMap);

        return success(1.0, null, result);
    }

    @CmdAnnotation(cmd = "cmd1", version = 1.0, preCompatible = true)
    public Object cmd1(List params) {
        return success(1.0, null, "kernel cmd1");
    }
}
