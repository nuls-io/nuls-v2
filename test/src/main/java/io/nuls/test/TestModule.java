package io.nuls.test;

import io.nuls.api.provider.Provider;
import io.nuls.api.provider.ServiceManager;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.controller.RpcServerManager;
import io.nuls.test.utils.RestFulUtils;
import io.nuls.test.utils.Utils;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.config.ConfigurationLoader;
import io.nuls.tools.core.ioc.SpringLiteContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * @Author: zhoulijun
 * @Time: 2019-03-18 15:05
 * @Description: 功能描述
 */
@Component
@Slf4j
public class TestModule extends RpcModule {

    @Override
    public Module[] getDependencies() {
        return new Module[0];
    }

    @Override
    public Module moduleInfo() {
        return new Module("test","1,0");
    }

    @Override
    public boolean doStart() {
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        log.info("do running");
        RpcServerManager.getInstance().startServer("0.0.0.0",9999);
        try {
            List<TestCaseIntf> testList = SpringLiteContext.getBeanList(TestCaseIntf.class);
            testList.forEach(tester->{
                TestCase testCase = tester.getClass().getAnnotation(TestCase.class);
                if(testCase == null){
                    return ;
                }
                try {
                    Utils.msg("开始测试"+tester.title());
                    tester.check(null,0);
                    Utils.msg(tester.title() + " 结束 ");
                } catch (TestFailException e) {
                    Utils.fail( tester.title() + " FAIL ");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    public static class TestBootstrap {

        public static void main(String[] args) {
            if (args == null || args.length == 0) {
                args = new String[]{"ws://" + HostInfo.getLocalIP() + ":8887/ws","1"};
            }else{
                args = new String[]{args[0],"1"};
            }
            ConfigurationLoader configurationLoader = new ConfigurationLoader();
            configurationLoader.load();
            Provider.ProviderType providerType = Provider.ProviderType.valueOf(configurationLoader.getValue("providerType"));
            int defaultChainId = Integer.parseInt(configurationLoader.getValue("chainId"));
            ServiceManager.init(defaultChainId,providerType);
            NulsRpcModuleBootstrap.run("io.nuls.test",args);
        }

    }

    @Override
    public void init() {
        super.init();
        RestFulUtils.getInstance().setServerUri("http://127.0.0.1:9999/api/");
    }
}
