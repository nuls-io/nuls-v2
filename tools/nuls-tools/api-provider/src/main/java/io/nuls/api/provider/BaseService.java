package io.nuls.api.provider;

import io.nuls.tools.constant.ErrorCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 15:44
 * @Description: 功能描述
 */
@Getter
@Setter
public abstract class BaseService {

    public static final String ERROR_CODE = "10001";

    public static final String RPC_ERROR_CODE = "10002";
    /**
     * 默认chainId
     * 从配置文件中注入
     */
    private int chainId;

    protected <T> Result<T> success(T data){
        return new Result<>(data);
    }

    protected <T> Result<T> success(List<T> list){
        return new Result<>(list);
    }

    public static Result fail(String errorCode,String message){
        return new Result(errorCode,message);
    }

    public static Result fail(String errorCode){
        return fail(errorCode, ErrorCode.init(errorCode).getMsg());
    }

}
