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
 * Cross chain module startup class
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
             * Registration Agreement,If it is a non main network, cross chain network activation is required
             */
            for (Chain chain:chainManager.getChainMap().values()) {
                if(!chain.isMainChain()){
                    NetWorkCall.activeCrossNet(chain.getChainId(), chain.getConfig().getMaxOutAmount(), chain.getConfig().getMaxInAmount(), nulsCrossChainConfig.getCrossSeedIps());
                }
            }
            /*
             * If it is the main network, provide complete cross chain registration information to the chain management module
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

            //Smart contract transaction registration
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
     * Initialize database
     * Initialization database
     */
    private void initDB() throws Exception {
        RocksDBService.init(nulsCrossChainConfig.getDataPath() + File.separator + ModuleE.CC.name);
        RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONSUME_LANGUAGE);
        RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONSUME_CONGIF);
        RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER);
        /*
            Registered Cross Chain Chain Information Operation Table
            Registered Cross-Chain Chain Information Operating Table
            key：RegisteredChain
            value:Registered Chain Information List
            */
        RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN);
    }

}
