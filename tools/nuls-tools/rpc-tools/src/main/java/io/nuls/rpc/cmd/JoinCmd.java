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

package io.nuls.rpc.cmd;

import io.nuls.rpc.info.RpcInfo;
import io.nuls.rpc.pojo.Rpc;
import io.nuls.tools.parse.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */

public class JoinCmd extends BaseCmd {

    /**
     * @param param param是一个Map:
     *              key format: cmd-version
     *              value: Rpc对象
     * @return String
     */
    @Override
    public String execRpc(Object param) {
        try {
            Map<String, Rpc> failedMap = new HashMap<>(16);
            @SuppressWarnings("unchecked") Map<String, Rpc> rpcMap = (Map<String, Rpc>) param;
            for (String key : rpcMap.keySet()) {
                Rpc rpc = JSONUtils.json2pojo(JSONUtils.obj2json(rpcMap.get(key)), Rpc.class);
                if (RpcInfo.remoteInterfaceMap.containsKey(key)) {
                    failedMap.put(key, rpc);
                } else {
                    RpcInfo.remoteInterfaceMap.put(key, rpc);

                    // 维护心跳
                    RpcInfo.heartbeatMap.put(rpc.getUri(), System.currentTimeMillis());
                }
            }

            RpcInfo.print();
            if (failedMap.size() == 0) {
                return SUCCESS;
            } else {
                return JSONUtils.obj2json(failedMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
