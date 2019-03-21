package io.nuls.test.controller;

import io.nuls.api.provider.Result;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:43
 * @Description: 功能描述
 */

@Data
@AllArgsConstructor
public class RemoteResult<T> {

    boolean success = true;

    T data;

    String msg;

    public RemoteResult() {
    }

    public RemoteResult(T data){
        this.data = data;
    }

    public RemoteResult(boolean success,String msg){
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccss(){
        return success;
    }

}
