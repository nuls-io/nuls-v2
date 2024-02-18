package io.nuls.mykernel;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.bootstrap.NettyServer;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.TimeContainer;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RequestAndResponseTestByMultiThread {


    public static void main(String[] args) {
        try {

            int threadCount = Thread.activeCount() * 8;
            ExecutorService executors = Executors.newFixedThreadPool(threadCount);

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

            long time = System.currentTimeMillis();

            Map<String, Object> pms = new HashMap<>(2);
            pms.put(Constants.VERSION_KEY_STR, "1.0");
            pms.put("count", 0);
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.KE.abbr, "sum", pms);

            System.out.println(response);

            System.out.println("Single request time consumption：" + (System.currentTimeMillis() - time));



            TimeContainer.t1 = 0;
            TimeContainer.t2 = 0;
            TimeContainer.t3 = 0;
            TimeContainer.t4 = 0;
            TimeContainer.t5 = 0;

            long now = System.nanoTime();

            int count  = 100000;

            for(int i = 0 ; i < count ; i++) {

                executors.execute(() -> {
                    Map<String, Object> params = new HashMap<>(2);
                    params.put(Constants.VERSION_KEY_STR, "1.0");
                    params.put("count", 0);
                    try {
                        ResponseMessageProcessor.requestAndResponse(ModuleE.KE.abbr, "sum", params);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            try {
                executors.shutdown();
                executors.awaitTermination(10000, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long timeDiff = System.nanoTime() - now;
            float rate = timeDiff / (float) count;

            System.out.println( threadCount+ " Threads making requests");
            System.out.println("request " + count + " Secondary time consumption：" + (timeDiff / 1000000) + " ms , Average time spent per request：" + (rate / 1000000) + " ms");

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
