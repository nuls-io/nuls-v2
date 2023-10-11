package io.nuls.consensus;

import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.consensus.utils.enumeration.ConsensusStatus;
import io.nuls.consensus.utils.manager.ChainManager;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.util.AddressPrefixDatas;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 共识模块启动及初始化管理
 * Consensus Module Startup and Initialization Management
 *
 * @author tag
 * 2018/3/4
 */
@Component
public class ConsensusBootStrap implements INulsCoresBootstrap {

    @Autowired
    private NulsCoresConfig consensusConfig;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;

    @Override
    public int order() {
        return 4;
    }

    @Override
    public void mainFunction(String[] args) {
        this.init();
    }

    public void init() {
        try {
            initSys();
            initDB();
            chainManager.initChain();
        } catch (Exception e) {
            Log.error(e);
        }
    }


    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CS.abbr, ConsensusConstant.RPC_VERSION);
    }

    private boolean doStart() {
        try {
            chainManager.runChain();
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    public void onDependenciesReady() {
        try {
            doStart();
            //智能合约交易注册
            chainManager.registerContractTx();
            for (Chain chain : chainManager.getChainMap().values()) {
                CallMethodUtils.sendState(chain, chain.isPacker());
                chain.setConsensusStatus(ConsensusStatus.RUNNING);
            }
            Log.debug("cs onDependenciesReady");
        } catch (Exception e) {
            Log.error(e);
        }
    }

    /**
     * 初始化系统编码
     * Initialization System Coding
     */
    private void initSys() throws Exception {
        System.setProperty(ConsensusConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private void initDB() throws Exception {
        RocksDBService.init(consensusConfig.getDataPath() + File.separator + ModuleE.CS.name);
        RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_CONGIF);
        if (consensusConfig.getMainChainId() != 1) {
            return;
        }
    }
}