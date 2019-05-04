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

package io.nuls.cmd.client;

import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author: Charlie
 */
public class CommandHelper {

    public static void checkArgsNumber(String[] args,int...numbers) throws ParameterException{
        if(!Arrays.stream(numbers).anyMatch(number->args.length == number)){
            ParameterException.throwParameterException();
        }
    }

    public static void checkArgs(boolean condition,String message) throws ParameterException{
        if(!condition){
            ParameterException.throwParameterException(message);
        }
    }

    public static void checkArgs(Supplier<Boolean> check,String message) throws ParameterException{
        if(!check.get()){
            ParameterException.throwParameterException(message);
        }
    }

    public static boolean checkArgsIsNull(String... args) {
        for (String arg : args) {
            if (arg == null || arg.trim().length() == 0) {
                return false;
            }
        }
        return true;
    }



    //    /**
//     * 确认新密码
//     * @param newPwd
//     */
    public static void confirmPwd(String newPwd) {
        System.out.print("Please confirm new password:");
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String confirmed = null;
            do {
                confirmed = reader.readLine('*');
                if (!newPwd.equals(confirmed)) {
                    System.out.print("Password confirmation doesn't match the password.\nConfirm new password: ");
                }
            } while (!newPwd.equals(confirmed));
        } catch (IOException e) {

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

    //
//    /**
//     * 得到用户输入的密码,必须输入
//     * 提示信息为默认
//     * @return
//     */
    public static String getPwd() {
        return getPwd(null);
    }

    //    /**
//     * 得到用户输入的密码,必须输入
//     * @param prompt 提示信息
//     * @return
//     */
    public static String getPwd(String prompt) {
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

    //
    /**
     * 得到用户输入的密码,允许不输入
     * @param prompt
     * @return
     */
    public static String getPwdOptional(String prompt) {
        if (StringUtils.isBlank(prompt)) {
            prompt = "Please enter the password (password is between 8 and 20 inclusive of numbers and letters), " +
                    "If you do not want to set a password, return directly.\nEnter your password:";
        }
        System.out.print(prompt);
        ConsoleReader reader = null;
        try {
            reader = new ConsoleReader();
            String npwd = null;
            do {
                npwd = reader.readLine('*');
                if (!"".equals(npwd) && !validPassword(npwd)) {
                    System.out.print("Password invalid, password is between 8 and 20 inclusive of numbers and letters.\nEnter your password:");
                }
            } while (!"".equals(npwd) && !validPassword(npwd));
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


    /**
     *  Check the difficulty of the password
     *  length between 8 and 20, the combination of characters and numbers
     *
     * @return boolean
     */
    public static boolean validPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }
        if (password.matches("(.*)[a-zA-z](.*)")
                && password.matches("(.*)\\d+(.*)")
                && !password.matches("(.*)\\s+(.*)")
                && !password.matches("(.*)[\u4e00-\u9fa5\u3000]+(.*)")) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 得到用户输入的密码,允许不输入
     * 提示信息为默认
     *
     * @return
     */
    public static String getPwdOptional() {
        return getPwdOptional(null);
    }




    public static String txTypeExplain(Integer type) {
        if (null == type) {
            return null;
        }
        switch (type) {
            case 1:
                return "coinbase";
            case 2:
                return "transfer";
            case 3:
                return "account_alias";
            case 4:
                return "register_agent";
            case 5:
                return "join_consensus";
            case 6:
                return "cancel_deposit";
            case 7:
                return "yellow_punish";
            case 8:
                return "red_punish";
            case 9:
                return "stop_agent";
            default:
                return type.toString();
        }
    }

    public static String consensusExplain(Integer status) {
        if (null == status) {
            return null;
        }
        switch (status) {
            case 0:
                return "unconsensus";
            case 1:
                return "consensus";

            default:
                return status.toString();
        }
    }

    public static String statusConfirmExplain(Integer status) {
        if (null == status) {
            return null;
        }
        switch (status) {
            case 0:
                return "confirm";
            case 1:
                return "unConfirm";

            default:
                return status.toString();
        }
    }

    public static String getArgsJson() {
        String prompt = "Please enter the arguments according to the arguments structure(eg. \"a\",2,[\"c\",4],\"\",\"e\" or \"'a',2,['c',4],'','e'\")," +
                "\nIf this method has no arguments(Refer to the command named \"getcontractinfo\" for the arguments structure of the method.), return directly.\nEnter the arguments:";
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

    public static Object[] parseArgsJson(String argsJson) {
        if(StringUtils.isBlank(argsJson)) {
            return new Object[0];
        }
        try {
            List<Object> list = JSONUtils.json2pojo(argsJson, ArrayList.class);
            return list.toArray();
        } catch (Exception e) {
            System.out.println("arguments format error (eg. \"a\",2,[\"c\",4],\"\",\"e\" or \"'a',2,['c',4],'','e'\")");
            return null;
        }
    }
}
