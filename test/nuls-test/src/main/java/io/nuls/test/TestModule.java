package io.nuls.test;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.ImportAccountByPrivateKeyReq;
import io.nuls.base.api.provider.network.NetworkProvider;
import io.nuls.base.api.provider.network.facade.NetworkInfo;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.I18nUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.controller.RpcServerManager;
import io.nuls.test.utils.LoggerUtil;
import io.nuls.test.utils.RestFulUtils;
import io.nuls.test.utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @Author: zhoulijun
 * @Time: 2019-03-18 15:05
 * @Description: Function Description
 */
@Component
public class TestModule extends RpcModule {

    static NulsLogger log = LoggerUtil.logger;

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired Config config;


    NetworkProvider networkProvider = ServiceManager.get(NetworkProvider.class);

    @Override
    protected long getTryRuningTimeout() {
        return Long.MAX_VALUE;
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                new Module(ModuleE.BL.abbr,"1.0"),
                new Module(ModuleE.TX.abbr,"1.0"),
                new Module(ModuleE.LG.abbr,"1.0"),
                new Module(ModuleE.CS.abbr,"1.0"),
                Module.build(ModuleE.AC)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module("test","1.0");
    }

    @Override
    public boolean doStart()
    {
        log.info("Waiting for dependent modules to be ready");
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        log.info("do running");
        RpcServerManager.getInstance().startServer("0.0.0.0", config.getHttpPort());
        if(config.getNodeType().equals("master")){
            Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(Constants.PASSWORD,config.getTestSeedAccount(),true));
            config.setSeedAddress(result.getData());
            Result<NetworkInfo> networkInfo = networkProvider.getInfo();
            Utils.success("=".repeat(100));
            Utils.success("network environment");
            Utils.success("localBestHeight:"+networkInfo.getData().getLocalBestHeight());
            Utils.success("netBestHeight:"+networkInfo.getData().getNetBestHeight());
            Utils.success("timeOffset:"+networkInfo.getData().getTimeOffset());
            Utils.success("inCount:"+networkInfo.getData().getInCount());
            Utils.success("outCount:"+networkInfo.getData().getOutCount());
            Utils.success("nodes:" + networkProvider.getNodes().getList().toString());
            Utils.success("Time:" + NulsDateUtils.timeStamp2DateStr(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss"));
            Utils.success("packetMagic:"+config.getPacketMagic());
            Utils.success("=".repeat(100));
            if(networkProvider.getNodes().getList().size()<config.getTestNodeCount()){
                log.error("The number of network nodes is less than the required number of nodes for testing, and testing cannot be conducted. The required number of nodes for testing:{}",config.getTestNodeCount());
                System.exit(0);
            }
            System.out.println();
            System.out.println();
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            try {
                List<TestCaseIntf> testList = SpringLiteContext.getBeanList(TestCaseIntf.class);
                testList.forEach(tester->{
                    TestCase testCase = tester.getClass().getAnnotation(TestCase.class);
                    if(testCase == null){
                        return ;
                    }
                    if(StringUtils.isNotBlank(System.getProperty("test.case"))){
                        String testCaseName = System.getProperty("test.case");
                        if(!testCase.value().equals(testCaseName)){
                            return ;
                        }
                    }
                    try {
                        Utils.successDoubleLine("Start testing"+tester.title() + "   " + tester.getClass());
                        tester.check(null,0);
                    } catch (TestFailException e) {
                        Utils.failLine( "【" + tester.title() + "】 Test failed :" + e.getMessage());
                        isSuccess.set(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(isSuccess.get()){
                Utils.successLine(" TEST DONE ");
            }else{
                Utils.failLine(" TEST FAIL ");
            }
            System.exit(0);
        }
        return RpcModuleState.Running;
    }

    @Override
    public void onDependenciesReady(Module module) {
        if(module.getName().equals(ModuleE.AC.name)){
            Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(Constants.PASSWORD,config.getTestSeedAccount(),true));
            config.setSeedAddress(result.getData());
        }
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    @Override
    public void init() {
        super.init();
        I18nUtils.loadLanguage(this.getClass(), "languages", "en");
//        I18nUtils.setLanguage("en");
        RestFulUtils.getInstance().setServerUri("http://127.0.0.1:" + config.getHttpPort() + "/api/");
    }
}
