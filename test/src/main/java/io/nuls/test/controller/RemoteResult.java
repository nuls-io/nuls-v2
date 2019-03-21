package io.nuls.test.controller;

import io.nuls.api.provider.Result;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:43
 * @Description: 功能描述
 */
public class RemoteResult<T> extends Result<T> {

    public RemoteResult(T data) {
        super(data);
    }

    public RemoteResult(String status,String msg){
        super(status,msg);
    }
}
