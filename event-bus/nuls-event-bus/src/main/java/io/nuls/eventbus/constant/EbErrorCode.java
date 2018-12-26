package io.nuls.eventbus.constant;

import io.nuls.tools.constant.ErrorCode;

/**
 * Error codes for event bus
 * @author naveen
 */
public interface EbErrorCode {

    ErrorCode SUCCESS = ErrorCode.init("10000");
    ErrorCode FAILED = ErrorCode.init("10001");
    ErrorCode UNKNOWN_ERROR = ErrorCode.init("10002");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("10003");
    ErrorCode PARAMS_MISSING = ErrorCode.init("10004");

    ErrorCode TOPIC_NOT_FOUND = ErrorCode.init("20000");
}
