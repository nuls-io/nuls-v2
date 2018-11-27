/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.chain.info;

import io.nuls.tools.constant.ErrorCode;

/**
 * @program: nuls2
 * @description:
 * @author: lan
 * @create: 2018/11/27
 **/
public class CmErrorCode {

    public final static ErrorCode Err10002 = ErrorCode.init("10002");
    public final static ErrorCode C10001 = ErrorCode.init("C10001");
    public final static ErrorCode C10002 = ErrorCode.init("C10002");
    public final static ErrorCode ERROR_CHAIN_NOT_FOUND = ErrorCode.init("C10003");
    public final static ErrorCode ERROR_CHAIN_STATUS = ErrorCode.init("C10005");
    public final static ErrorCode ERROR_CHAIN_ASSET_MUTI = ErrorCode.init("C10006");


    public final static ErrorCode ERROR_ASSET_ID_EXIST = ErrorCode.init("A10005");



    public final static ErrorCode ERROR_ASSET_NOT_EXIST = ErrorCode.init("A10014");
    public final static ErrorCode ERROR_ADDRESS_ERROR =  ErrorCode.init("A10016");
    public final static ErrorCode A10017 =  ErrorCode.init("A10017");

    /**
     * 错误编码
     */
    public static final ErrorCode ERROR_ASSET_SYMBOL_NULL =ErrorCode.init("A10000");
    public static final ErrorCode ERROR_ASSET_SYMBOL_MAX = ErrorCode.init("A10001");
    public static final ErrorCode ERROR_ASSET_SYMBOL_EXIST = ErrorCode.init("A10002");
    public static final ErrorCode ERROR_JSON_TO_ASSET = ErrorCode.init("A10003");
    public static final ErrorCode ERROR_ASSET_RECOVERY_RATE =ErrorCode.init( "A10004");
    public static final ErrorCode ERROR_ASSET_NAME_NULL = ErrorCode.init("A10006");
    public static final ErrorCode ERROR_ASSET_NAME_MAX = ErrorCode.init("A10007");
    public static final ErrorCode ERROR_ASSET_DEPOSITNULS = ErrorCode.init("A10008");
    public static final ErrorCode ERROR_ASSET_INITNUMBER_MIN = ErrorCode.init("A10009");
    public static final ErrorCode ERROR_ASSET_INITNUMBER_MAX = ErrorCode.init("A10010");
    public static final ErrorCode ERROR_ASSET_DECIMALPLACES_MIN = ErrorCode.init("A10011");
    public static final ErrorCode ERROR_ASSET_DECIMALPLACES_MAX = ErrorCode.init("A10012");
    public static final ErrorCode ERROR_CHAIN_ASSET_NOT_MATCH = ErrorCode.init("A10013");
    public static final ErrorCode ERROR_ASSET_EXCEED_INIT = ErrorCode.init("A10015");


    public static final ErrorCode ERROR_TX_REG_RPC =  ErrorCode.init("A10026");

}
