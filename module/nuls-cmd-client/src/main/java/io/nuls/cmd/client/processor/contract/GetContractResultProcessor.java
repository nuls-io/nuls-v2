/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.cmd.client.processor.contract;


import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.contract.facade.GetContractResultReq;
import io.nuls.base.data.NulsHash;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.ErrorCodeConstants;
import io.nuls.cmd.client.utils.Na;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;

import java.util.Map;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/19
 */
@Component
public class GetContractResultProcessor extends ContractBaseProcessor {

    @Override
    public String getCommand() {
        return "getcontractresult";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<hash>  transaction hash -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getcontractresult <hash> --get the contract execute result of the transaction by txhash";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        checkArgs(NulsHash.validHash(args[1]),"hash format error");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String hash = args[1];
        if(StringUtils.isBlank(hash)) {
            return CommandResult.getFailed(ErrorCodeConstants.PARAM_ERR.getMsg());
        }
        Result<Map> result = contractProvider.getContractResult(new GetContractResultReq(hash));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        Map<String, Object> map = (Map) result.getData();
        Map<String, Object> dataMap = (Map) map.get("data");
        if(dataMap != null) {
            dataMap.put("totalFee", Na.naToNuls(dataMap.get("totalFee")));
            dataMap.put("txSizeFee", Na.naToNuls(dataMap.get("txSizeFee")));
            dataMap.put("actualContractFee", Na.naToNuls(dataMap.get("actualContractFee")));
            dataMap.put("refundFee", Na.naToNuls(dataMap.get("refundFee")));
            dataMap.put("value", Na.naToNuls(dataMap.get("value")));
            dataMap.put("price", Na.naToNuls(dataMap.get("price")));
            //dataMap.put("balance", Na.naToNuls(dataMap.get("balance")));
        }

        result.setData(dataMap);
        return CommandResult.getResult(result);
    }

}
