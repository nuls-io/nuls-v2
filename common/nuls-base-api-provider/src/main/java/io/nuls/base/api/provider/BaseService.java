package io.nuls.base.api.provider;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.model.StringUtils;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 15:44
 * @Description: 功能描述
 */
public abstract class BaseService {

    /**
     * 默认chainId
     * 从配置文件中注入
     */
    private int chainId;

    protected <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    protected <T> Result<T> success(List<T> list) {
        return new Result<>(list);
    }

    public static Result fail(ErrorCode errorCode, String message) {
        return new Result(errorCode.getCode(), StringUtils.isNotBlank(message) ? message : StringUtils.isBlank(errorCode.getMsg()) ?
                "fail,error code:" + errorCode.getCode() : errorCode.getMsg());
    }

    public static Result fail(String errorCode) {
        return fail(ErrorCode.init(errorCode));
    }

    public static Result fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMsg());
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }
}
