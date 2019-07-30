package io.nuls.core.rpc.modulebootstrap;

import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.invoke.BaseInvoke;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.parse.MapUtils;

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

    public RegisterInvoke(Module module, Set<Module> dependenices) {
        this.dependenices = dependenices;
        this.module = module;
    }

    @Override
    public void callBack(Response response) {
        Map responseData = (Map) response.getResponseData();
        if (response.isSuccess()) {
            RpcModule rpcModule = SpringLiteContext.getBean(RpcModule.class);
            if (rpcModule.getDependencies().isEmpty()) {
                Log.info("RMB:module rpc is ready");
                return;
            }
            Map methodMap = (Map) responseData.get("RegisterAPI");
            Map dependMap = (Map) methodMap.get("Dependencies");
            StringBuilder logInfo = new StringBuilder("\n有模块信息改变，重新同步：\n");
            for (Object object : dependMap.entrySet()) {
                Map.Entry<String, Map> entry = (Map.Entry<String, Map>) object;
                logInfo.append("注入：[key=").append(entry.getKey()).append(",value=").append(entry.getValue()).append("]\n");
                ConnectManager.ROLE_MAP.put(entry.getKey(), entry.getValue());
            }
            Log.debug(logInfo.toString());
            ConnectManager.updateStatus();
            if (!ConnectManager.isReady()) {
                return;
            }
            Log.info("RMB:module rpc is ready");
            dependMap.entrySet().forEach(obj -> {
                Map.Entry<String, Map> entry = (Map.Entry<String, Map>) obj;
                if (dependenices.stream().anyMatch(d -> d.getName().equals(entry.getKey()))) {
                    if(rpcModule.isDependencieReady(entry.getKey())) {
                        return ;
                    }
                    NotifySender notifySender = SpringLiteContext.getBean(NotifySender.class);
                    notifySender.send("registerModuleDependent_" + entry.getKey(),10,() -> {
                        Response cmdResp = null;
                        try {
                            cmdResp = ResponseMessageProcessor.requestAndResponse(entry.getKey(), "registerModuleDependencies", MapUtils.beanToLinkedMap(module));
                            Log.debug("registerModuleDependent : {},result:{}", entry.getKey(),cmdResp);
                            return cmdResp.isSuccess();
                        } catch (Exception e) {
                            Log.error("Calling remote interface failed. module:{} - interface:{} - message:{}", module, "registerModuleDependencies", e.getMessage());
                            return false;
                        }
                    });

//                    //是当前模块的依赖模块
//                    Request request = MessageUtil.defaultRequest();
//                    request.getRequestMethods().put("registerModuleDependencies", module);
//                    Message message = MessageUtil.basicMessage(MessageType.Request);
//                    message.setMessageData(request);
//                    try {
//                        Log.info("follow module:{}:{}",entry.getKey(),entry.getValue());
//                        Channel channel = ConnectManager.getConnectByUrl("ws://"+entry.getValue().get("IP") + ":" +entry.getValue().get("Port")+"/ws");
//                        ConnectManager.sendMessage(channel, JSONUtils.obj2json(message));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            });

        }

    }

}
