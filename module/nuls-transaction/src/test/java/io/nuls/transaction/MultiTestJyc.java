/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.TestCommonUtil.*;
import static io.nuls.transaction.utils.LoggerUtil.LOG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class MultiTestJyc extends Thread {

    private static List<String> accountList = new ArrayList<>();
    private int num;

    private MultiTestJyc(int num) {
        this.num = num;
    }

    public static void main(String[] args) throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":7771");
        Chain chain = new Chain();
        chain.setConfig(new ConfigBean(CHAIN_ID, ASSET_ID, 1024 * 1024, 1000, 20, 20000, 60000));

        BigInteger balance = getBalance(chain, SOURCE_ADDRESS);
        LOG.info(SOURCE_ADDRESS + "-----balance:{}", balance);

        int count = 6000;
        {
            LOG.info("##########create " + count + " accounts##########");
            int loop = count / 100 == 0 ? 1 : count / 100;
            for (int i = 0; i < loop; i++) {
                Map<String, Object> params = new HashMap<>();
                params.put(Constants.VERSION_KEY_STR, VERSION);
                params.put(Constants.CHAIN_ID, CHAIN_ID);
                params.put("count", Math.min(count, 100));
                params.put("password", PASSWORD);
                Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
                assertTrue(response.isSuccess());
                accountList.addAll((List<String>) ((HashMap) ((HashMap) response.getResponseData()).get("ac_createAccount")).get("list"));
            }
            assertEquals(count, accountList.size());
        }
        {
            LOG.info("##########transfer from seed address to " + count + " accounts##########");
            for (String account : accountList) {
                Response response = transfer(SOURCE_ADDRESS, account, "1000000000000");
                assertTrue(response.isSuccess());
                Thread.sleep(1);
            }
        }
        Thread.sleep(15000);
        new MultiTestJyc(0).start();
        new MultiTestJyc(1).start();
        new MultiTestJyc(2).start();
        new MultiTestJyc(3).start();
        new MultiTestJyc(4).start();
        new MultiTestJyc(5).start();
    }

    @Override
    public void run() {
        LOG.info("##########" + 1000 + " accounts Transfer to each other##########");
        //100个地址之间互相转账
        int total = 100_000_000;
        for (int j = 0; j < total / 1000; j++) {
            for (int i = 0; i < 1000; i++) {
                String from = accountList.get((i % 1000) + num * 1000);
                String to = accountList.get(((i + 1) % 1000) + num * 1000);
                try {
                    Response response = transfer(from, to, "100000000");
                    assertTrue(response.isSuccess());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            LOG.info(num + "##########" + j + " round end##########");
        }
    }

}
