package io.nuls.cmd.client.processor;

import io.nuls.tools.constant.ErrorCode;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 14:10
 * @Description: error code constants
 */
public final class ErrorCodeConstants {

    public static final String  SYSTEM_ERR = "10001";

    /**
     * 参数错误
     */
    public static final ErrorCode PARAM_ERR = ErrorCode.init("10012");

}
