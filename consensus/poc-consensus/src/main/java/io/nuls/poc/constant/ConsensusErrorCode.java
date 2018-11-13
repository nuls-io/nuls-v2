package io.nuls.poc.constant;

import io.nuls.tools.constant.ErrorCode;

public interface ConsensusErrorCode {
    ErrorCode SUCCESS = ErrorCode.init("10000");
    ErrorCode FAILED = ErrorCode.init("10001");
    ErrorCode SYS_UNKOWN_EXCEPTION = ErrorCode.init("10002");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("10003");
    ErrorCode PARAM_NUMBER_ERROR = ErrorCode.init("70000");
    ErrorCode DATA_ERROR = ErrorCode.init("70001");
}
