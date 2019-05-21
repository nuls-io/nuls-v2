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
package io.nuls.ledger.test.cmd;

import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.ledger.test.constant.TestConfig;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.parse.JSONUtils;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/01/14
 **/
public class CmdChainAsset {
    //    String address = "JgT2JCQvKGRKRjKqyfxRAj2zSCpGca01f";
    String address = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    //入账金额
    BigInteger amount = BigInteger.valueOf(100000000000000L);

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
    }


    @Test
    public void getAssetsByChainIdTest() throws Exception {
        // Build params map
       Map<String,Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, TestConfig.chainId);

        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getAssetsByChainId", params);
        Log.debug("response {}", JSONUtils.obj2json(response));
    }
}
