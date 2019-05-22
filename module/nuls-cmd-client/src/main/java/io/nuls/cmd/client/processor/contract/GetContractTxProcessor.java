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
import io.nuls.base.api.provider.contract.facade.GetContractTxReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.ErrorCodeConstants;
import io.nuls.cmd.client.utils.Na;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.DateUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.HashUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/19
 */
@Component
public class GetContractTxProcessor extends ContractBaseProcessor {

    @Override
    public String getCommand() {
        return "getcontracttx";
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
        return "getcontracttx <hash> --get the contract transaction information by txhash";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        checkArgs(HashUtil.validHash(args[1]),"hash format error");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String hash = args[1];
        if(StringUtils.isBlank(hash)) {
            return CommandResult.getFailed(ErrorCodeConstants.PARAM_ERR.getMsg());
        }
        Result<Map> result = contractProvider.getContractTx(new GetContractTxReq(hash));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        Map<String, Object> map = (Map)result.getData();
        map.put("fee", Na.naToNuls(map.get("fee")));
        map.put("value", Na.naToNuls(map.get("value")));
        map.put("time",  DateUtils.convertDate(new Date((Long)map.get("time"))));
        map.put("status", statusExplain((Integer)map.get("status")));
        map.put("type", CommandHelper.txTypeExplain((Integer)map.get("type")));

        List<Map<String, Object>> inputs = (List<Map<String, Object>>)map.get("inputs");
        for(Map<String, Object> input : inputs){
            input.put("value", Na.naToNuls(input.get("value")));
        }
        map.put("inputs", inputs);
        List<Map<String, Object>> outputs = (List<Map<String, Object>>)map.get("outputs");
        for(Map<String, Object> output : outputs){
            output.put("value", Na.naToNuls(output.get("value")));
//            output.put("status", statusExplainForOutPut((Integer) output.get("status")));
        }
        map.put("outputs", outputs);

        Map<String, Object> txDataMap = (Map) map.get("txData");
        if(txDataMap != null) {
            Map<String, Object> dataMap = (Map) txDataMap.get("data");
            if(dataMap != null) {
                dataMap.put("value", Na.naToNuls(dataMap.get("value")));
                dataMap.put("price", Na.naToNuls(dataMap.get("price")));
            }
        }

        Map<String, Object> contractResultMap = (Map) map.get("contractResult");
        if(contractResultMap != null) {
            contractResultMap.put("totalFee", Na.naToNuls(contractResultMap.get("totalFee")));
            contractResultMap.put("txSizeFee", Na.naToNuls(contractResultMap.get("txSizeFee")));
            contractResultMap.put("actualContractFee", Na.naToNuls(contractResultMap.get("actualContractFee")));
            contractResultMap.put("refundFee", Na.naToNuls(contractResultMap.get("refundFee")));
            contractResultMap.put("value", Na.naToNuls(contractResultMap.get("value")));
            contractResultMap.put("price", Na.naToNuls(contractResultMap.get("price")));
            //contractResultMap.put("balance", Na.naToNuls(contractResultMap.get("balance")));
        }


        result.setData(map);
        return CommandResult.getResult(result);
    }

    private String statusExplain(Integer status){
        if(status == 0){
            return "unConfirm";
        }
        if(status == 1){
            return"confirm";
        }
        return "unknown";
    }

    /**
     * 状态 0:usable(未花费), 1:timeLock(高度锁定), 2:consensusLock(参与共识锁定), 3:spent(已花费)
     * @param status
     * @return
     */
    private String statusExplainForOutPut(Integer status){
        if(status == null){
            return "unknown";
        }

        if(status == 0){
            return "usable";
        }
        if(status == 1){
            return"timeLock";
        }
        if(status == 2){
            return"consensusLock";
        }
        if(status == 3){
            return"spent";
        }
        return "unknown";
    }
}
