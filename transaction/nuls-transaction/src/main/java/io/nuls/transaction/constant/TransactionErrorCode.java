package io.nuls.transaction.constant;

import io.nuls.tools.constant.ErrorCode;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public class TransactionErrorCode {

    ErrorCode SUCCESS = ErrorCode.init("10000");
    ErrorCode FAILED = ErrorCode.init("10001");
    ErrorCode SYS_UNKOWN_EXCEPTION = ErrorCode.init("10002");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("10003");
}
