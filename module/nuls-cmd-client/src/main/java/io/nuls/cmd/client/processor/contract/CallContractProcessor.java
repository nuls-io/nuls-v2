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
import io.nuls.base.api.provider.contract.facade.CallContractReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.utils.Na;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/19
 */
@Component
public class CallContractProcessor extends ContractBaseProcessor {


    private ThreadLocal<CallContractReq> paramsData = new ThreadLocal<>();

    @Override
    public String getCommand() {
        return "callcontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<sender>            source address    -required")
                .newLine("\t<gasLimit>          gas limit    -required")
                .newLine("\t<price>             price (Unit: Na/Gas)    -required")
                .newLine("\t<contractAddress>   contract address    -required")
                .newLine("\t<methodName>        the method to call    -required")
                .newLine("\t<value>             transfer nuls to the contract (Unit: Nuls)    -required")
                .newLine("\t[-d methodDesc]        the method description    -not required")
                .newLine("\t[-r remark]            remark    -not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "callcontract <sender> <gasLimit> <price> <contractAddress> <methodName> <value> [-d methodDesc] [-r remark] --call contract";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,6,7,8);
        checkAddress(config.getChainId(),args[1],args[4]);
        checkIsNumeric(args[2],"gasLimit");
        checkIsNumeric(args[3],"price");
//        checkArgs(()->{
//            BigDecimal amount = new BigDecimal(args[6]);
//            return amount.compareTo(BigDecimal.valueOf(0.01D)) >= 0;
//        },"value must be a numeric and greater than 0.01");
        CallContractReq form = getContractCall(args);
        if(null == form){
            return false;
        }
        paramsData.set(form);
        return true;
    }

    private CallContractReq getContractCall(String[] args) {
        CallContractReq call = null;
        try {
            call = new CallContractReq();
            call.setSender(args[1].trim());
            call.setGasLimit(Long.valueOf(args[2].trim()));
            call.setPrice(Long.valueOf(args[3].trim()));
            call.setContractAddress(args[4].trim());
            call.setMethodName(args[5].trim());
            long naValue = 0L;
            call.setValue(config.toSmallUnit(args[6]).longValue());
            if(args.length == 9) {
                String argType = args[7].trim();
                if ("-d".equals(argType)) {
                    call.setMethodDesc(args[8].trim());
                } else if ("-r".equals(argType)) {
                    call.setRemark(args[8].trim());
                } else {
                    return null;
                }
            }else if(args.length == 11) {
                String argType0 = args[7].trim();
                String argType1 = args[9].trim();
                boolean isType0D = "-d".equals(argType0);
                boolean isType1D = "-d".equals(argType1);
                boolean isType0R = "-r".equals(argType0);
                boolean isType1R = "-r".equals(argType1);
                if((isType0D && isType1D) || (isType0R && isType1R)) {
                    // 不能同时为-d或-r
                    return null;
                }
                if(isType0D) {
                    call.setMethodDesc(args[8].trim());
                }
                if(isType0R) {
                    call.setRemark(args[8].trim());
                }
                if(isType1D) {
                    call.setMethodDesc(args[10].trim());
                }
                if(isType1R) {
                    call.setRemark(args[10].trim());
                }
            }
            return call;
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }


    @Override
    public CommandResult execute(String[] args) {
        CallContractReq form = paramsData.get();
        if (null == form) {
            form = getContractCall(args);
        }
        if (null == form) {
            return CommandResult.getFailed("parameter error.");
        }
        String password = CommandHelper.getPwd();
        form.setArgs(getContractCallArgsJson());
        form.setPassword(password);
        Result<String> result = contractProvider.callContract(form);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }


}
