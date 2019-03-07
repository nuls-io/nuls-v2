package io.nuls.api.provider;

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

    public static final int ERROR_CODE = -1;

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

    protected Result fail(int status,String message){
        return new Result(status,message);
    }

}
