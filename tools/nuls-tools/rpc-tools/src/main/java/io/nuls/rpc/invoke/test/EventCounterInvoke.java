package io.nuls.rpc.invoke.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

/**
 * @author tangyi
 * @date 2018/12/4
 * @description
 */
public class EventCounterInvoke extends BaseInvoke {
    /**
     * 自动回调的类需要重写的方法
     * A method that needs to be rewritten for a class that calls back automatically
     *
     * @param response 请求的响应信息，Response information to requests
     */
    @Override
    public void callBack(Response response) {
        try {
            Log.info("EventCounterInvoke 进入了！！！！！" + JSONUtils.obj2json(response));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
