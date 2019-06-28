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
import io.nuls.base.api.provider.contract.facade.CreateContractReq;
import io.nuls.base.api.provider.contract.facade.GetContractConstructorArgsReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.ErrorCodeConstants;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/19
 */
@Component
public class CreateContractProcessor extends ContractBaseProcessor {

    private ThreadLocal<CreateContractReq> paramsData = new ThreadLocal<>();

    @Override
    public String getCommand() {
        return "createcontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<sender>         source address    -required")
                .newLine("\t<gasLimit>       gas limit    -required")
                .newLine("\t<price>          price (Unit: Na/Gas)    -required")
                .newLine("\t<contractCode>   contract code    -required")
                .newLine("\t<alias>          contract alias    -required")
                .newLine("\t[remark]         remark    -not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createcontract <sender> <gasLimit> <price> <contractCode> [remark] --create contract";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,4,5);
        checkAddress(config.getChainId(),args[1]);
        checkIsNumeric(args[2],"gasLimit");
        checkIsNumeric(args[3],"price");
        CreateContractReq form = getContractCreate(args);
        if(null == form){
            return false;
        }
        paramsData.set(form);
        return true;
    }

    private CreateContractReq getContractCreate(String[] args) {
        CreateContractReq create = null;
        try {
            create = new CreateContractReq();
            create.setSender(args[1].trim());
            create.setGasLimit(Long.valueOf(args[2].trim()));
            create.setPrice(Long.valueOf(args[3].trim()));
            create.setContractCode(args[4].trim());
            create.setAlias(args[5].trim());
            if(args.length == 7) {
                create.setRemark(args[6].trim());
            }
            return create;
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }


    @Override
    public CommandResult execute(String[] args) {
        CreateContractReq form = paramsData.get();
        if (null == form) {
            form = getContractCreate(args);
        }
        if (null == form) {
            return CommandResult.getFailed("parameter error.");
        }
        String password = CommandHelper.getPwd();
        String contractCode = form.getContractCode();
        Result<Object[]> res = createContractArgs(contractCode);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        form.setArgs(res.getData());
        form.setPassword(password);
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("sender", sender);
//        parameters.put("gasLimit", form.getGasLimit());
//        parameters.put("price", form.getPrice());
//        parameters.put("password", password);
//        parameters.put("remark", form.getRemark());
//        parameters.put("contractCode", form.getContractCode());
//        parameters.put("args", contractArgs);
//        Result<Map> result = restFul.post("/contract/create", parameters);
        Result<Map> result = contractProvider.createContract(form);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }

    private Result<Object[]> createContractArgs(String contractCode) {
        GetContractConstructorArgsReq req = new GetContractConstructorArgsReq(contractCode);
        Result<Map> result = contractProvider.getContractConstructorArgs(req);
        if (result.isSuccess()) {
            Result rpcClientResult = new Result();
            try {
                Map<String, Object> map = result.getData();
                Map<String, Object> constructorMap = (Map) map.get("constructor");
                List<Object> argsList = (List) constructorMap.get("args");
                Object[] argsObj;
                if(argsList.size() > 0) {
                    String argsListStr = JSONUtils.obj2PrettyJson(argsList);
                    // 再次交互输入构造参数
                    String argsJson = getArgsJson(argsListStr);
                    argsObj = CommandHelper.parseArgsJson(argsJson);
                } else {
                    argsObj = new Object[0];
                }
                rpcClientResult.setData(argsObj);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.fail(ErrorCodeConstants.PARAM_ERR.getCode(),ErrorCodeConstants.PARAM_ERR.getMsg());
            }
            return rpcClientResult;
        }
        return Result.fail(ErrorCodeConstants.SYSTEM_ERR.getCode(),ErrorCodeConstants.SYSTEM_ERR.getMsg());
    }

    public String getArgsJson(String constructor) {
        System.out.println("The arguments structure: ");
        System.out.println(constructor);
        String prompt = "Please enter the arguments you want to fill in according to the arguments structure(eg. \"a\",2,[\"c\",4],\"\",\"e\" or \"'a',2,['c',4],'','e'\").\nEnter the arguments:";
        System.out.print(prompt);
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String args = reader.readLine();
            if(StringUtils.isNotBlank(args)) {
                args = "[" + args + "]";
            }
            return args;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (!reader.delete()) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
