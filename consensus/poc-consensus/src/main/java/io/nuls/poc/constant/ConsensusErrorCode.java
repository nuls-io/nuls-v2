package io.nuls.poc.constant;

import io.nuls.tools.constant.ErrorCode;

public interface ConsensusErrorCode {
    ErrorCode SUCCESS = ErrorCode.init("10000");
    ErrorCode FAILED = ErrorCode.init("10001");
    ErrorCode SYS_UNKOWN_EXCEPTION = ErrorCode.init("10002");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("10003");
    ErrorCode PARAM_NUMBER_ERROR = ErrorCode.init("70000");
    ErrorCode DATA_ERROR = ErrorCode.init("70001");
    ErrorCode TX_NOT_EXIST = ErrorCode.init("70002");
    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init("70003");
    ErrorCode ACCOUNT_IS_ALREADY_ENCRYPTED = ErrorCode.init("70004");
    ErrorCode AGENT_NOT_EXIST = ErrorCode.init("70005");
    ErrorCode DATA_NOT_EXIST = ErrorCode.init("70006");
}
