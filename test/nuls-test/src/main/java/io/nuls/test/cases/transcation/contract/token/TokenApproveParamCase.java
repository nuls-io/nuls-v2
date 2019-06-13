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
package io.nuls.test.cases.transcation.contract.token;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.transcation.contract.CallContractParamCase;

import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-06-12
 */
@Component
public class TokenApproveParamCase extends CallContractParamCase {

    String defaultSender = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    String defaultTokenReceiver = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";

    @Override
    public String title() {
        return "token授权参数";
    }

    @Override
    public Map doTest(Map param, int depth) throws TestFailException {
        String sender = (String) param.get("from");
        if(StringUtils.isBlank(sender)) {
            sender = defaultSender;
        }
        String tokenReceiver = (String) param.get("to");
        if(StringUtils.isBlank(tokenReceiver)) {
            tokenReceiver = defaultTokenReceiver;
        }
        param.put("sender", sender);
        param.put("tokenReceiver", tokenReceiver);
        Object[] args = new Object[]{tokenReceiver, 1_0000_0000L};
        param.put("methodName", "approve");
        param.put("args", args);
        return param;
    }
}
