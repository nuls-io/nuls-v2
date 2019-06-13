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
package io.nuls.test.cases.transcation.contract;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.contract.ContractProvider;
import io.nuls.base.api.provider.contract.facade.CreateContractReq;
import io.nuls.core.core.annotation.Component;
import io.nuls.test.cases.BaseTestCase;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;

import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-06-12
 */
@Component
public class CreateContractCase extends BaseTestCase<Map, Map> {

    private ContractProvider contractProvider = ServiceManager.get(ContractProvider.class);

    @Override
    public String title() {
        return "创建合约";
    }

    @Override
    public Map doTest(Map param, int depth) throws TestFailException {
        String sender = (String) param.get("sender");
        String contractCode = (String) param.get("contractCode");
        String remark = (String) param.get("remark");
        Object[] args = (Object[]) param.get("args");
        CreateContractReq req = new CreateContractReq();
        req.setSender(sender);
        req.setPassword(Constants.PASSWORD);
        req.setContractCode(contractCode);
        req.setGasLimit(100000L);
        req.setPrice(25L);
        req.setRemark(remark);
        req.setArgs(args);
        Result<Map> result = contractProvider.createContract(req);
        checkResultStatus(result);
        Map map = result.getData();
        map.put("count", param.get("count"));
        return map;
    }
}
