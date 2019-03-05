package io.nuls.rpc.modulebootstrap;

import io.netty.channel.Channel;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoulijun
 * @Time: 2019-02-28 14:52
 * @Description: 功能描述
 */
@Slf4j
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
            }
            Log.info(logInfo.toString());
            ConnectManager.updateStatus();
            if(!ConnectManager.isReady()){
                return ;
            }
            log.info("RMB:module rpc is ready");
            while(!dependenices.stream().allMatch(dm->{
                try {
                    Response res = ResponseMessageProcessor.requestAndResponse(dm.getName(), "connectReady", null);
                    Map rd = (Map) res.getResponseData();
                    if(rd == null)return false;
                    Boolean isReady = (Boolean) rd.get("connectReady");
                    if (!res.isSuccess()) {
                        return false;
                    }
                    return isReady;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            })){
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            dependMap.entrySet().forEach(obj->{
                Map.Entry<String, Map> entry = (Map.Entry<String, Map>) obj;
                if(dependenices.stream().anyMatch(d->d.getName().equals(entry.getKey()))){
                    //是当前模块的依赖模块
                    Request request = MessageUtil.defaultRequest();
                    request.getRequestMethods().put("registerModuleDependencies", module);
                    Message message = MessageUtil.basicMessage(MessageType.Request);
                    message.setMessageData(request);
                    try {
                        Log.info("follow module:{}:{}",entry.getKey(),entry.getValue());
                        Channel channel = ConnectManager.getConnectByUrl("ws://"+entry.getValue().get("IP") + ":" +entry.getValue().get("Port")+"/ws");
                        ConnectManager.sendMessage(channel, JSONUtils.obj2json(message));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

}
