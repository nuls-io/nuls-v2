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
package io.nuls.test.cases.transcation;

import io.nuls.core.core.annotation.Component;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.transcation.contract.CallContractCase;
import io.nuls.test.cases.transcation.contract.CreateContractCase;
import io.nuls.test.cases.transcation.contract.token.CreateTokenContractParamCase;
import io.nuls.test.cases.transcation.contract.token.TokenTransferParamCase;

/**
 * @author: PierreLuo
 * @date: 2019-06-12
 */
@Component
@TestCase("singleTokenTransfer")
public class SingleTokenTransferCase extends TestCaseChain {
    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[] {
                CreateTokenContractParamCase.class,
                CreateContractCase.class,
                SleepAdapter.$15SEC.class,
                TokenTransferParamCase.class,
                CallContractCase.class
        };
    }

    @Override
    public String title() {
        return "合约token交易";
    }
}
