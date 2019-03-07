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

    int status;

    String message;

    T data;

    List<T> list;

    public Result(T data){
        this.status = 0;
        this.data = data;
    }

    public Result(List<T> list){
        this.list = list;
        this.status = 0;
    }

    public Result(int status,String message){
        this.status = status;
        this.message = message;
    }

    public static Result fail(int status,String message){
        return new Result(status,message);
    }

    public boolean isSuccess(){
        return status == 0;
    }

}
