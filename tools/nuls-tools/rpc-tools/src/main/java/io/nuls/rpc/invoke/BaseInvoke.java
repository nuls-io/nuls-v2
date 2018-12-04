package io.nuls.rpc.invoke;

import io.nuls.rpc.model.message.Response;

/**
 * @author tangyi
 * @date 2018/12/4
 * @description
 */
public abstract class BaseInvoke {
    /**
     * 自动回调的类需要重写的方法
     * A method that needs to be rewritten for a class that calls back automatically
     * @param response 请求的响应信息，Response information to requests
     */
    public abstract void callBack(Response response);
}
