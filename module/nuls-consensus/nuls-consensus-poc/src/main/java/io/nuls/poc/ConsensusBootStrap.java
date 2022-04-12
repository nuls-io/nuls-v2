package io.nuls.poc;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ModuleHelper;
import io.nuls.base.protocol.RegisterHelper;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.util.AddressPrefixDatas;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.poc.constant.ConsensusConfig;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.utils.enumeration.ConsensusStatus;
import io.nuls.poc.utils.manager.ChainManager;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 共识模块启动及初始化管理
 * Consensus Module Startup and Initialization Management
 *
 * @author tag
 * 2018/3/4
 */
@Component
public class ConsensusBootStrap extends RpcModule {

    @Autowired
    private ConsensusConfig consensusConfig;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run(ConsensusConstant.BOOT_PATH, args);
    }

    /**
     * 初始化模块，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     * 在onStart前会调用此方法
     */
    @Override
    public void init() {
        try {
            initSys();
            AddressTool.init(addressPrefixDatas);
            initDB();
            chainManager.initChain();
            ModuleHelper.init(this);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                Module.build(ModuleE.BL),
                Module.build(ModuleE.AC),
                Module.build(ModuleE.NW),
                Module.build(ModuleE.LG),
                Module.build(ModuleE.TX)
        };
    }

    /**
     * 指定RpcCmd的包名
     * 可以不实现此方法，若不实现将使用spring init扫描的包
     *
     * @return
     */
    @Override
    public Set<String> getRpcCmdPackage() {
        return Set.of(ConsensusConstant.RPC_PATH);
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CS.abbr, ConsensusConstant.RPC_VERSION);
    }

    @Override
    public boolean doStart() {
        try {
            while (!isDependencieReady(ModuleE.TX.abbr) || !isDependencieReady(ModuleE.BL.abbr)) {
                Log.debug("wait depend modules ready");
                Thread.sleep(2000L);
            }
            chainManager.runChain();
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public void onDependenciesReady(Module module) {
        try {
            //共识交易注册
            if (module.getName().equals(ModuleE.TX.abbr)) {
                chainManager.registerTx();
            }
            //智能合约交易注册
            if (module.getName().equals(ModuleE.SC.abbr)) {
                chainManager.registerContractTx();
                for (Chain chain : chainManager.getChainMap().values()) {
                    CallMethodUtils.sendState(chain, chain.isPacker());
                }
            }
            //协议注册
            if (module.getName().equals(ModuleE.PU.abbr)) {
                chainManager.getChainMap().keySet().forEach(RegisterHelper::registerProtocol);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        for (Chain chain : chainManager.getChainMap().values()) {
            chain.setConsensusStatus(ConsensusStatus.RUNNING);
        }
        Log.debug("cs onDependenciesReady");
        NulsDateUtils.getInstance().start();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        if (dependenciesModule.getName().equals(ModuleE.TX.abbr) || dependenciesModule.getName().equals(ModuleE.BL.abbr)) {
            for (Chain chain : chainManager.getChainMap().values()) {
                chain.setConsensusStatus(ConsensusStatus.WAIT_RUNNING);
            }
        }
        return RpcModuleState.Ready;
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
        RocksDBService.init(consensusConfig.getDataFolder());
        RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_CONGIF);
        if (consensusConfig.getMainChainId() != 1) {
            return;
        }

//        List<byte[]> list2 = RocksDBService.valueList("consensus_agent1");
//        for (byte[] arr : list2) {
//            AgentPo po = new AgentPo();
//            po.parse(arr, 0);
//            boolean up = false;
//            if (po.getHash().toHex().equals("528a630b43f5d1eeea5b4567e87c7f7f3d4b86046b8a3d079ef0b9a1aea64360") && po.getDelHeight() < 7865610L) {
//                po.setDelHeight(-1L);
//                up = true;
//            }
//            if (po.getHash().toHex().equals("a27170a4ad246758cc7fb45ded14b065f6a1919836a2bba34e6dcd9335a054da") && po.getDelHeight() < 8084100) {
//                po.setDelHeight(-1);
//                up = true;
//            }
//            if (po.getHash().toHex().equals("ab00e76ba14fdc1e14dc1a3c7d86e9751de81fa0dfa98c98b5f236f6638a3cc0") && po.getDelHeight() < 8084100) {
//                po.setDelHeight(-1L);
//                up = true;
//            }
//            if (po.getHash().toHex().equals("cc1b60c282d297f4431c283bc88615f8d70f81e065405d0d8448190620032a91") && po.getDelHeight() < 8084100) {
//                po.setDelHeight(-1);
//                up = true;
//            }
//            if (po.getHash().toHex().equals("d11d29e38b3db75aec0ebb69dc66eb4f6276d0a1d9c7faa6a4fa33b699637447") && po.getDelHeight() < 8084100) {
//                po.setDelHeight(-1);
//                up = true;
//            }
//            if (up) {
//                byte[] key = po.getHash().getBytes();
//                byte[] value = po.serialize();
//                RocksDBService.put("consensus_agent1", key, value);
//            }
//        }

    }
}