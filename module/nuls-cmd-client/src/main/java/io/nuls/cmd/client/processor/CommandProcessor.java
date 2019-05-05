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

package io.nuls.cmd.client.processor;


import io.nuls.base.api.provider.Result;
import io.nuls.base.basic.AddressTool;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.ParameterException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命令行处理接口，其他模块的RPC实现须实现该接口
 */
public interface CommandProcessor {

    Pattern IS_NUMBERIC = Pattern.compile("[0-9]+");

    Pattern IS_AMOUNT = Pattern.compile("^[0-9]+([.]{1}[0-9]+){0,1}$");

    default void checkArgsNumber(String[] args, int... numbers) throws ParameterException {
        if (!Arrays.stream(numbers).anyMatch(number -> args.length - 1 == number)) {
            ParameterException.throwParameterException();
        }
    }

    default void checkArgs(boolean condition, String message) throws ParameterException {
        if (!condition) {
            ParameterException.throwParameterException(message);
        }
    }

    default void checkAddress(int chainId, String... addressList) throws ParameterException {
        for (String address : addressList) {
            checkArgs(AddressTool.validAddress(chainId, address), "address format error");
        }
    }

    default void checkIsNumeric(String arg,String name) {
        Matcher matcher = IS_NUMBERIC.matcher(arg);
        if (!matcher.matches()) {
            ParameterException.throwParameterException(name + " must be a numeric");
        }
    }

    default void checkIsAmount(String arg,String name) {
        Matcher matcher = IS_AMOUNT.matcher(arg);
        if(!matcher.find()){
            ParameterException.throwParameterException(name + " must be a numeric");
        }
        BigDecimal amount = new BigDecimal(arg);
        if (amount.compareTo(BigDecimal.valueOf(0.01D)) < 0) {
            ParameterException.throwParameterException(name + " must be a numeric and greater than 0.01");
        }
    }

    default void checkArgs(Supplier<Boolean> check, String message) throws ParameterException {
        if (!check.get()) {
            ParameterException.throwParameterException(message);
        }
    }

    default String getPwd() {
        return getPwd(null);
    }

    default String getPwd(String prompt) {
        if (StringUtils.isBlank(prompt)) {
            prompt = "Please enter the password.\nEnter your password:";
        }
        System.out.print(prompt);
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String npwd = null;
            do {
                npwd = reader.readLine('*');
                if ("".equals(npwd)) {
                    System.out.print("The password is required.\nEnter your password:");
                }
            } while ("".equals(npwd));
            return npwd;
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

    default Result dataTransformList(Result rpcResult) {
        rpcResult.setData(rpcResult.getList());
        return rpcResult;
    }

    default CommandResult getResult(Result rpcResult) {
        if (null == rpcResult) {
            return CommandResult.getFailed("Result is null!");
        }
        CommandResult result = new CommandResult();
        result.setSuccess(rpcResult.isSuccess());
        String message = "";
        if (!rpcResult.isSuccess()) {
            Map<String, Object> map = (Map) rpcResult.getData();
            message = (String) map.get("msg");
            //message += ":";
        } else {
            try {
                if (rpcResult.getData() != null) {
                    message += JSONUtils.obj2PrettyJson(rpcResult.getData());
                } else if (rpcResult.getList() != null) {
                    message += JSONUtils.obj2PrettyJson(rpcResult.getList());
                } else {
                    message += "success";
                }
            } catch (Exception e) {
                Log.error("return data format exception :", e);
            }
        }
        result.setMessage(message);
        return result;
    }

    String getCommand();

    CommandGroup getGroup();

    String getHelp();

    String getCommandDescription();

    boolean argsValidate(String[] args);

    CommandResult execute(String[] args);
}
