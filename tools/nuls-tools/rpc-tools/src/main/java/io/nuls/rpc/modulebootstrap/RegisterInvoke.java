package io.nuls.rpc.modulebootstrap;

import io.netty.channel.Channel;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Map;
import java.util.Set;

/**
 * @Author: zhoulijun
 * @Time: 2019-02-28 14:52
 * @Description: 功能描述
 */
public class RegisterInvoke extends BaseInvoke {

    Set<Module> dependenices;

    Module module;

    public RegisterInvoke(Module module,Set<Module> dependenices){
        this.dependenices = dependenices;
        this.module = module;
    }

    @Override
    public void callBack(Response response) {
        Map responseData = (Map) response.getResponseData();
        Map methodMap = (Map) responseData.get("registerAPI");
        Map dependMap = (Map) methodMap.get("Dependencies");
        String status = response.getResponseStatus();
        StringBuilder logInfo = new StringBuilder("\n有模块信息改变，重新同步：\n");
        if("1".equals(status)){
            for (Object object : dependMap.entrySet()) {
                Map.Entry<String, Map> entry = (Map.Entry<String, Map>) object;
                logInfo.append("注入：[key=").append(entry.getKey()).append(",value=").append(entry.getValue()).append("]\n");
                ConnectManager.ROLE_MAP.put(entry.getKey(), entry.getValue());
                if(dependenices.stream().anyMatch(d->d.getName().equals(entry.getKey()))){
                    //是当前模块的依赖模块
                    Request request = MessageUtil.defaultRequest();
                    request.getRequestMethods().put("registerModuleDependencies", module);
                    Message message = MessageUtil.basicMessage(MessageType.Request);
                    message.setMessageData(request);
                    try {
                        Channel channel = ConnectManager.getConnectByUrl("ws://"+entry.getValue().get("IP") + ":" +entry.getValue().get("Port")+"/ws");
                        ConnectManager.sendMessage(channel, JSONUtils.obj2json(message));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            ConnectManager.updateStatus();
        }
        Log.info(logInfo.toString());
    }

}
