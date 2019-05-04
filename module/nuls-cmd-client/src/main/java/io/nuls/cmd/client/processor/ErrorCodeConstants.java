package io.nuls.cmd.client.processor;

import io.nuls.core.constant.ErrorCode;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 14:10
 * @Description: error code constants
 */
public final class ErrorCodeConstants {

    public static final ErrorCode  SYSTEM_ERR = ErrorCode.init("10001");

    /**
     * 参数错误
     */
    public static final ErrorCode PARAM_ERR = ErrorCode.init("10012");

    /**
     * 数据格式异常
     */
    public static final ErrorCode DATA_ERROR = ErrorCode.init("10014");


}
