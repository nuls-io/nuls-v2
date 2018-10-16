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

import com.alibaba.fastjson.JSON;
import io.nuls.rpc.RpcInfo;
import io.nuls.rpc.pojo.Rpc;

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
        @SuppressWarnings("unchecked") Map<String, Rpc> rpcMap = (Map<String, Rpc>) param;
        for (String key : rpcMap.keySet()) {
            if (RpcInfo.remoteInterfaceMap.containsKey(key)) {
                // TODO

            } else {
                Rpc rpc = JSON.parseObject(JSON.toJSONString(rpcMap.get(key)), Rpc.class);
                RpcInfo.remoteInterfaceMap.put(key, rpc);

                // 维护心跳
                RpcInfo.heartbeatMap.put(rpc.getUri(), System.currentTimeMillis());
            }
        }
        RpcInfo.print();
        return SUCCESS;
    }
}
