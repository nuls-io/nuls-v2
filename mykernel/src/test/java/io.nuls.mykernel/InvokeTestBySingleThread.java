package io.nuls.mykernel;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.bootstrap.NettyServer;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InvokeTestBySingleThread {


    public static void main(String[] args) {
        try {

            int port = 8887;
            NettyServer.startServer(port);
            // Start server instance
            ConnectManager.LOCAL.setApiMethods(new ArrayList<>());
            ConnectManager.LOCAL.setModuleAbbreviation(ModuleE.KE.abbr);
            ConnectManager.LOCAL.setModuleName(ModuleE.KE.name);
            ConnectManager.LOCAL.setModuleDomain(ModuleE.KE.domain);
            Map<String, String> connectionInformation = new HashMap<>(2);
            connectionInformation.put(Constants.KEY_IP, HostInfo.getLocalIP());
            connectionInformation.put(Constants.KEY_PORT, port + "");
            ConnectManager.LOCAL.setConnectionInformation(connectionInformation);
            ConnectManager.startService = true;
            SpringLiteContext.init("io.nuls.rpc.cmd.kernel");
            ConnectManager.scanPackage("io.nuls.rpc.cmd.kernel");
            ConnectManager.ROLE_MAP.put(ModuleE.KE.abbr,connectionInformation);
            ConnectManager.updateStatus();


            String url = "ws://"+ HostInfo.getLocalIP()+":" + port + "/ws";

            ResponseMessageProcessor.handshake(url);

            /*
             * 和指定地址同步
             * */
            ResponseMessageProcessor.syncKernel(url);

            long time = System.currentTimeMillis();

            Map<String, Object> params = new HashMap<>(1);
            params.put("count", 10);

            String msId = ResponseMessageProcessor.requestAndInvokeWithAck(ModuleE.KE.abbr, "sum",
                    params, "5", "0", new BaseInvoke() {
                        @Override
                        public void callBack(Response response) {
                            System.out.println("===== : "+ response);
                        }
                    });

            System.out.println("消息ID：" + msId + "，耗时：" + (System.currentTimeMillis() - time));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
