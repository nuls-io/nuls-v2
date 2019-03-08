package io.nuls.api.provider;

import lombok.Data;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 15:45
 * @Description: 功能描述
 */
@Data
public class Result<T> {

    static final String SUCCESS = "10000";

    String status;

    String message;

    T data;

    List<T> list;

    public Result(T data){
        this.status = SUCCESS;
        this.data = data;
    }

    public Result(List<T> list){
        this.list = list;
        this.status = SUCCESS;
    }

    public Result(String status,String message){
        this.status = status;
        this.message = message;
    }

    public static Result fail(String status,String message){
        return new Result(status,message);
    }

    public boolean isSuccess(){
        return status == SUCCESS;
    }

    public boolean isFailed(){
        return !isSuccess();
    }

}
