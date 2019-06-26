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
package io.nuls.test.cases.transcation.batch;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.base.api.provider.account.facade.GetAccountPrivateKeyByAddressReq;
import io.nuls.base.api.provider.account.facade.ImportAccountByPrivateKeyReq;
import io.nuls.base.data.NulsHash;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.test.Config;
import io.nuls.test.cases.*;
import io.nuls.test.cases.transcation.batch.fasttx.FastTransfer;
import io.nuls.test.cases.transcation.contract.CreateContractCase;
import io.nuls.test.cases.transcation.contract.token.CreateTokenContractParamCase;
import io.nuls.test.cases.transcation.contract.token.TokenApproveParamCase;
import io.nuls.test.cases.transcation.contract.token.TokenTransferParamCase;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.nuls.test.cases.account.BaseAccountCase.PASSWORD;
import static io.nuls.test.cases.transcation.batch.BatchCreateAccountCase.TRANSFER_AMOUNT;

/**
 * @author: PierreLuo
 * @date: 2019-06-12
 */
@Component
@TestCase("batchTokenContract")
public class BatchTokenContractCase extends TestCaseChain {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired
    Config config;
    @Autowired
    FastTransfer fastTransfer;

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        SpringLiteContext.putBean(SetApproveParamCase.class.getSimpleName(), new SetApproveParamCase());
        return new Class[] {
                BatchCreateAccountCase.class,
                CreateTokenContractParamCase.class,
                CreateContractCase.class,
                SleepAdapter.$10SEC.class,
                SetApproveParamCase.class,
                BatchBaseContractCase.class,
                SleepAdapter.MAX.class
        };
    }

    private class SetApproveParamCase extends BaseTestCase<Map, Map> {
        @Override
        public String title() {
            return "SetApproveParamCase";
        }
        @Override
        public Map doTest(Map param, int depth) throws TestFailException {
            param.put("callContractParamCase", SpringLiteContext.getBean(TokenApproveParamCase.class));
            return param;
        }
    }

    private class SetTransferParamCase extends BaseTestCase<Map, Map> {
        @Override
        public String title() {
            return "SetTransferParamCase";
        }
        @Override
        public Map doTest(Map param, int depth) throws TestFailException {
            param.put("callContractParamCase", SpringLiteContext.getBean(TokenTransferParamCase.class));
            return param;
        }
    }

    @Override
    public String title() {
        return "批量token交易";
    }

    @Override
    public Object initParam() {
        Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(PASSWORD, config.getTestSeedAccount(),true));
        Result<String> account = accountService.createAccount(new CreateAccountReq(1,PASSWORD));
        try {
            Result<NulsHash> result1 = fastTransfer.transfer(result.getData(),account.getList().get(0), TRANSFER_AMOUNT.multiply(BigInteger.valueOf(5000)),config.getTestSeedAccount(),null);
        } catch (TestFailException e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BatchParam param = new BatchParam();
        param.count = 100L;
        Result<String> priKey = accountService.getAccountPrivateKey(new GetAccountPrivateKeyByAddressReq(PASSWORD,account.getList().get(0)));
        param.formAddressPriKey = priKey.getData();
        return param;
    }
}