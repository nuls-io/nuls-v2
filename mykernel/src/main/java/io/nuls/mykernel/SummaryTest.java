/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2018 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.mykernel;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试代码，测试rpc服务稳定性
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
public class SummaryTest {

    public static void main(String[] args) throws Exception {
        NoUse.mockModule();
        long total = 0;
        int round = 20;
        int loop = 1000;
        for (int j = 0; j < round; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < loop; i++) {
                try {
                    Map<String, Object> params = new HashMap<>(2);
                    params.put(Constants.VERSION_KEY_STR, "1.0");
                    params.put("count", i);
                    Response response = CmdDispatcher.requestAndResponse(ModuleE.KE.abbr, "sum", params);
                    if (!response.isSuccess()) {
                        System.out.println(response);
                        throw new RuntimeException();
                    }
                } catch (Exception e) {
                    Log.error(e);
                }
            }
            long time = System.currentTimeMillis() - start;
            total += time;
            System.out.println(j + " round complete!");
        }
        System.out.println("total rpc:" + (round*loop) + ", every 1000 rpc call cost avg time:" + (total / round));
    }

}