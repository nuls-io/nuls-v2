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

import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;

/**
 * @program: nuls2
 * @description:
 * @author: lan
 * @create: 2018/11/27
 **/
public interface CmErrorCode extends CommonCodeConstanst {
    ErrorCode BALANCE_NOT_ENOUGH = ErrorCode.init("cm_0001");
    ErrorCode ERROR_TX_HEX = ErrorCode.init("cm_0002");
    ErrorCode ERROR_NOT_CROSS_TX = ErrorCode.init("cm_0003");
    ErrorCode ERROR_PARAMETER = ErrorCode.init("cm_0004");

    ErrorCode ERROR_CHAIN_ID_EXIST = ErrorCode.init("cm_1001");
    ErrorCode ERROR_CHAIN_NAME_LENGTH = ErrorCode.init("cm_1002");
    ErrorCode ERROR_CHAIN_NOT_FOUND = ErrorCode.init("cm_1003");
    ErrorCode ERROR_CHAIN_STATUS = ErrorCode.init("cm_1005");
    ErrorCode ERROR_CHAIN_ASSET_MUTI = ErrorCode.init("cm_1006");
    ErrorCode ERROR_MAGIC_NUMBER_EXIST = ErrorCode.init("cm_1007");
    ErrorCode ERROR_CHAIN_NAME_EXIST = ErrorCode.init("cm_1008");
    ErrorCode ERROR_VERIFIER_LIST_EMPTY = ErrorCode.init("cm_1009");
    ErrorCode ERROR_SIGNATURE_BYZANTINE_RATIO = ErrorCode.init("cm_1010");
    ErrorCode ERROR_MAX_SIGNATURE_COUNT = ErrorCode.init("cm_1011");
    ErrorCode ERROR_CHAIN_ADDRESS_PREFIX = ErrorCode.init("cm_1012");


    /**
     * 错误编码
     */
    ErrorCode ERROR_ASSET_SYMBOL_NULL = ErrorCode.init("cm_2000");
    ErrorCode ERROR_ASSET_SYMBOL_LENGTH = ErrorCode.init("cm_2001");
    ErrorCode ERROR_ASSET_SYMBOL_EXIST = ErrorCode.init("cm_2002");
    ErrorCode ERROR_JSON_TO_ASSET = ErrorCode.init("cm_2003");
    ErrorCode ERROR_ASSET_RECOVERY_RATE = ErrorCode.init("cm_2004");
    ErrorCode ERROR_ASSET_ID_EXIST = ErrorCode.init("cm_2005");
    ErrorCode ERROR_ASSET_NAME_NULL = ErrorCode.init("cm_2006");
    ErrorCode ERROR_ASSET_NAME_MAX = ErrorCode.init("cm_2007");
    ErrorCode ERROR_ASSET_DEPOSITNULS = ErrorCode.init("cm_2008");
    ErrorCode ERROR_ASSET_INITNUMBER = ErrorCode.init("cm_2009");
    ErrorCode ERROR_ASSET_DECIMALPLACES = ErrorCode.init("cm_2011");
    ErrorCode ERROR_CHAIN_ASSET_NOT_MATCH = ErrorCode.init("cm_2013");
    ErrorCode ERROR_ASSET_NOT_EXIST = ErrorCode.init("cm_2014");
    ErrorCode ERROR_ASSET_EXCEED_INIT = ErrorCode.init("cm_2015");
    ErrorCode ERROR_ADDRESS_ERROR = ErrorCode.init("cm_2016");
    ErrorCode ERROR_TX_HASH = ErrorCode.init("cm_2017");
    ErrorCode ERROR_TX_REG_RPC = ErrorCode.init("cm_2018");
    ErrorCode ERROR_LEDGER_BALANCE_RPC = ErrorCode.init("cm_2019");



    ErrorCode ERROR_ACCOUNT_VALIDATE = ErrorCode.init("cm_3000");
    ErrorCode ERROR_SIGNDIGEST = ErrorCode.init("cm_3001");

}
