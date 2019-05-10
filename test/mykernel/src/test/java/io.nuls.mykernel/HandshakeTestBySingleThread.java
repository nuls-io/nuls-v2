package io.nuls.mykernel;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.bootstrap.NettyServer;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.TimeContainer;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HandshakeTestBySingleThread {


    public static void main(String[] args) {
        try {

            int port = 7771;
            NettyServer.startServer(port);
            // Start server instance
            ConnectManager.LOCAL.setMethods(new ArrayList<>());
            ConnectManager.LOCAL.setAbbreviation(ModuleE.KE.abbr);
            ConnectManager.LOCAL.setModuleName(ModuleE.KE.name);
            ConnectManager.LOCAL.setModuleDomain(ModuleE.KE.domain);
            Map<String, String> connectionInformation = new HashMap<>(2);
            connectionInformation.put(Constants.KEY_IP, HostInfo.getLocalIP());
            connectionInformation.put(Constants.KEY_PORT, port + "");
            ConnectManager.LOCAL.setConnectionInformation(connectionInformation);
            ConnectManager.startService = true;
            SpringLiteContext.init("io.nuls.rpc.cmd.kernel");
            ConnectManager.scanPackage(Set.of("io.nuls.rpc.cmd.kernel"));
            ConnectManager.ROLE_MAP.put(ModuleE.KE.abbr,connectionInformation);
            ConnectManager.updateStatus();


            String url = "ws://"+ HostInfo.getLocalIP()+":" + port + "/ws";


            /*
             * 和指定地址同步
             * */
            ResponseMessageProcessor.syncKernel(url);

            long time = System.currentTimeMillis();
            ResponseMessageProcessor.handshake(url);
            System.out.println("单次握手耗时：" + (System.currentTimeMillis() - time));

            long now = System.nanoTime();

            int count  = 100000;

            for(int i = 0 ; i < count ; i++) {
                ResponseMessageProcessor.handshake(url);
            }

            long timeDiff = System.nanoTime() - now;
            float rate = timeDiff / (float) count;

            System.out.println("请求 " + count + " 次耗时：" + (timeDiff / 1000000) + " ms , 平均每次请求耗时：" + (rate / 1000000) + " ms");

            System.out.println("t1 : " + TimeContainer.t1 / 1000000d + " ms");
            System.out.println("t2 : " + TimeContainer.t2 / 1000000d + " ms");
            System.out.println("t3 : " + TimeContainer.t3 / 1000000d + " ms");
            System.out.println("t4 : " + TimeContainer.t4 / 1000000d + " ms");
            System.out.println("t5 : " + TimeContainer.t5 / 1000000d + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
