package io.nuls.crosschain.base.constant;


import io.nuls.core.constant.ErrorCode;

/**
 * Cross chain module error code management class
 * @author tag
 * 2019/04/08
 * */
public interface CrossChainErrorCode {
    /**
     * Parameter error
     * */
    ErrorCode PARAMETER_ERROR = ErrorCode.init("11001");
}
