/**
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.db.constant;

public interface DBErrorCode {

    ErrorCode SUCCESS = ErrorCode.init("10000");
    ErrorCode FAILED = ErrorCode.init("10001");
    ErrorCode LANGUAGE_CANNOT_SET_NULL = ErrorCode.init("10005");
    ErrorCode NULL_PARAMETER = ErrorCode.init("10013");
    ErrorCode DB_MODULE_START_FAIL = ErrorCode.init("20000");
    ErrorCode DB_UNKOWN_EXCEPTION = ErrorCode.init("20001");
    ErrorCode DB_SESSION_MISS_INIT = ErrorCode.init("20002");
    ErrorCode DB_SAVE_CANNOT_NULL = ErrorCode.init("20003");
    ErrorCode DB_SAVE_BATCH_LIMIT_OVER = ErrorCode.init("20004");
    ErrorCode DB_DATA_ERROR = ErrorCode.init("20005");
    ErrorCode DB_SAVE_ERROR = ErrorCode.init("20006");
    ErrorCode DB_UPDATE_ERROR = ErrorCode.init("20007");
    ErrorCode DB_ROLLBACK_ERROR = ErrorCode.init("20008");
    ErrorCode DB_TABLE_EXIST = ErrorCode.init("20009");
    ErrorCode DB_TABLE_NOT_EXIST = ErrorCode.init("20010");
    ErrorCode DB_TABLE_CREATE_EXCEED_LIMIT = ErrorCode.init("20011");
    ErrorCode DB_TABLE_CREATE_ERROR = ErrorCode.init("20012");
    ErrorCode DB_TABLE_CREATE_PATH_ERROR = ErrorCode.init("20013");
    ErrorCode DB_TABLE_DESTROY_ERROR = ErrorCode.init("20014");
    ErrorCode DB_BATCH_CLOSE = ErrorCode.init("20015");
}