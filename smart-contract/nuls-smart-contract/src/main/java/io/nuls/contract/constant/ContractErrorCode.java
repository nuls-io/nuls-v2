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
package io.nuls.contract.constant;

import io.nuls.tools.constant.ErrorCode;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/17
 */
public interface ContractErrorCode {

    ErrorCode SUCCESS = ErrorCode.init("10000");
    ErrorCode FAILED = ErrorCode.init("10001");

    ErrorCode PARAMETER_ERROR = ErrorCode.init("10012");
    ErrorCode NULL_PARAMETER = ErrorCode.init("10013");
    ErrorCode DATA_NOT_FOUND = ErrorCode.init("10015");
    ErrorCode INSUFFICIENT_BALANCE = ErrorCode.init("10027");

    ErrorCode DB_UNKOWN_EXCEPTION = ErrorCode.init("20001");
    ErrorCode DB_AREA_EXIST = ErrorCode.init("20009");
    ErrorCode DB_AREA_NOT_EXIST = ErrorCode.init("20010");
    ErrorCode DB_AREA_CREATE_EXCEED_LIMIT = ErrorCode.init("20011");
    ErrorCode DB_AREA_CREATE_ERROR = ErrorCode.init("20012");
    ErrorCode DB_AREA_CREATE_PATH_ERROR = ErrorCode.init("20013");
    ErrorCode DB_AREA_DESTROY_ERROR = ErrorCode.init("20014");
    ErrorCode DB_BATCH_CLOSE = ErrorCode.init("20015");

    ErrorCode UTXO_STATUS_CHANGE = ErrorCode.init("31002");
    ErrorCode INVALID_AMOUNT = ErrorCode.init("31005");
    ErrorCode TX_DATA_VALIDATION_ERROR = ErrorCode.init("31008");
    ErrorCode TOO_SMALL_AMOUNT = ErrorCode.init("31012");

    ErrorCode ADDRESS_ERROR = ErrorCode.init("50004");

    ErrorCode CONTRACT_EXECUTE_ERROR = ErrorCode.init("100001");
    ErrorCode CONTRACT_ADDRESS_NOT_EXIST = ErrorCode.init("100002");
    ErrorCode CONTRACT_TX_CREATE_ERROR = ErrorCode.init("100003");
    ErrorCode ILLEGAL_CONTRACT_ADDRESS = ErrorCode.init("100004");
    ErrorCode NON_CONTRACTUAL_TRANSACTION = ErrorCode.init("100005");
    ErrorCode NON_CONTRACTUAL_TRANSACTION_NO_TRANSFER = ErrorCode.init("100006");
    ErrorCode CONTRACT_NAME_FORMAT_INCORRECT = ErrorCode.init("100007");
    ErrorCode CONTRACT_NOT_NRC20 = ErrorCode.init("100008");
    ErrorCode CONTRACT_NON_VIEW_METHOD = ErrorCode.init("100009");
    ErrorCode ILLEGAL_CONTRACT = ErrorCode.init("100010");
    ErrorCode CONTRACT_DUPLICATE_TOKEN_NAME = ErrorCode.init("100011");
    ErrorCode CONTRACT_NRC20_SYMBOL_FORMAT_INCORRECT = ErrorCode.init("100012");
    ErrorCode CONTRACT_LOCK = ErrorCode.init("100013");
    ErrorCode CONTRACT_NRC20_MAXIMUM_DECIMALS = ErrorCode.init("100014");
    ErrorCode CONTRACT_NRC20_MAXIMUM_TOTAL_SUPPLY = ErrorCode.init("100015");
    ErrorCode CONTRACT_MINIMUM_PRICE = ErrorCode.init("100016");
    ErrorCode CONTRACT_DELETE_BALANCE = ErrorCode.init("100017");
    ErrorCode CONTRACT_DELETE_CREATER = ErrorCode.init("100018");
    ErrorCode CONTRACT_DELETED = ErrorCode.init("100019");
    ErrorCode CONTRACT_GAS_LIMIT = ErrorCode.init("100020");
    ErrorCode CONTRACT_OTHER_ERROR = ErrorCode.init("100099");
}
