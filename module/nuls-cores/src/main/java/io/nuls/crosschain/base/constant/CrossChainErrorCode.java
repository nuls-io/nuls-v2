package io.nuls.crosschain.base.constant;


import io.nuls.core.constant.ErrorCode;

/**
 * 跨链模块错误码管理类
 * @author tag
 * 2019/04/08
 * */
public interface CrossChainErrorCode {
    /**
     * 参数错误
     * */
    ErrorCode PARAMETER_ERROR = ErrorCode.init("11001");
}
