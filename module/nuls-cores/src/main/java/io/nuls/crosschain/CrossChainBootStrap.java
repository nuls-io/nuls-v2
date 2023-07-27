package io.nuls.crosschain;

import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.config.ConfigurationLoader;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.rpc.call.ChainManagerCall;
import io.nuls.crosschain.rpc.call.NetWorkCall;
import io.nuls.crosschain.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * 跨链模块启动类
 * Cross Chain Module Startup and Initialization Management
 * @author tag
 * 2019/4/10
 */
@Component
public class CrossChainBootStrap implements INulsCoresBootstrap {
    @Autowired
    private NulsCoresConfig nulsCrossChainConfig;
    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;

    @Autowired
    private ChainManager chainManager;

    @Override
    public int order() {
        return 7;
    }

    @Override
    public void mainFunction(String[] args) {
        this.init();
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CC.name, "1.0");
    }

    public void init() {
        try {
            initDB();
            chainManager.initChain();
        }catch (Exception e){
            Log.error(e);
        }
    }

    public void onDependenciesReady(){
        try {
            chainManager.runChain();
            /*
             * 注册协议,如果为非主网则需激活跨链网络
             */
            for (Chain chain:chainManager.getChainMap().values()) {
                if(!chain.isMainChain()){
                    NetWorkCall.activeCrossNet(chain.getChainId(), chain.getConfig().getMaxOutAmount(), chain.getConfig().getMaxInAmount(), nulsCrossChainConfig.getCrossSeedIps());
                }
            }
            /*
             * 如果为主网，向链管理模块过去完整的跨链注册信息
             */
            if (nulsCrossChainConfig.isMainNet()) {
                RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
                if(registeredChainMessage != null && registeredChainMessage.getChainInfoList() != null){
                    chainManager.setRegisteredCrossChainList(registeredChainMessage.getChainInfoList());
                }else{
                    registeredChainMessage = ChainManagerCall.getRegisteredChainInfo(chainManager);
                    registeredCrossChainService.save(registeredChainMessage);
                    chainManager.setRegisteredCrossChainList(registeredChainMessage.getChainInfoList());
                }
            }

            //智能合约交易注册
            chainManager.registerContractTx();

            ConfigurationLoader configurationLoader = SpringLiteContext.getBean(ConfigurationLoader.class);
            nulsCrossChainConfig.setSeedNodeList(Arrays.stream(configurationLoader.getValue(ModuleE.Constant.CONSENSUS, "seedNodes").split(","))
            .collect(Collectors.toSet()));
            Log.info("cc onDependenciesReady");
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private void initDB() throws Exception {
        RocksDBService.init(nulsCrossChainConfig.getDataPath() + File.separator + ModuleE.CC.name);
        RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONSUME_LANGUAGE);
        RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONSUME_CONGIF);
        RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER);
        /*
            已注册跨链的链信息操作表
            Registered Cross-Chain Chain Information Operating Table
            key：RegisteredChain
            value:已注册链信息列表
            */
        RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN);
    }

}
