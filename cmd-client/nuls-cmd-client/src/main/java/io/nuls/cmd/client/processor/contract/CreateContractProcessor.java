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

import io.nuls.api.provider.Result;
import io.nuls.api.provider.contract.facade.CreateContractReq;
import io.nuls.api.provider.contract.facade.GetContractConstructorArgsReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.ErrorCodeConstants;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.parse.JSONUtils;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/19
 */
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
                .newLine("\t[remark]         remark    -not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createcontract <sender> <gasLimit> <price> <contractCode> [remark] --create contract";
    }

    @Override
    public boolean argsValidate(String[] args) {
        boolean result;
        do {
            int length = args.length;
            if (length != 5 && length != 6) {
                result = false;
                break;
            }
            if (!CommandHelper.checkArgsIsNull(args)) {
                result = false;
                break;
            }

            // gasLimit
            if (!StringUtils.isNumeric(args[2])) {
                result = false;
                break;
            }
            // price
            if (!StringUtils.isNumeric(args[3])) {
                result = false;
                break;
            }
            CreateContractReq form = getContractCreate(args);
            if(null == form){
                result = false;
                break;
            }
            paramsData.set(form);
            result = true;
        } while (false);
        return result;
    }

    private CreateContractReq getContractCreate(String[] args) {
        CreateContractReq create = null;
        try {
            create = new CreateContractReq();
            create.setSender(args[1].trim());
            create.setGasLimit(Long.valueOf(args[2].trim()));
            create.setPrice(Long.valueOf(args[3].trim()));
            create.setContractCode(args[4].trim());
            if(args.length == 6) {
                create.setRemark(args[5].trim());
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
        String sender = form.getSender();
        String password = CommandHelper.getPwd("Enter account password please.");
        String contractCode = form.getContractCode();
        Result<Object[]> res = createContractArgs(contractCode);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        form.setArgs(res.getData());
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
        GetContractConstructorArgsReq req = new GetContractConstructorArgsReq();
        req.setContractCode(contractCode);
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
                    argsObj = parseArgsJson(argsJson);
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

    private Object[] parseArgsJson(String argsJson) {
        if(StringUtils.isBlank(argsJson)) {
            return new Object[0];
        }
        try {
            List<Object> list = JSONUtils.json2pojo(argsJson, ArrayList.class);
            return list.toArray();
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }
}
