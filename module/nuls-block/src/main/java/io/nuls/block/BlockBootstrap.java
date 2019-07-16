package io.nuls.block;

import io.nuls.base.protocol.ModuleHelper;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.protocol.RegisterHelper;
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.BlockConfig;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.thread.monitor.*;
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
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.block.constant.Constant.*;

/**
 * 区块模块启动类
 *
 * @author captain
 * @version 1.0
 * @date 19-3-4 下午4:09
 */
@Component
public class BlockBootstrap extends RpcModule {

    @Autowired
    public static BlockConfig blockConfig;

    @Autowired
    private ChainManager chainManager;

    public static boolean started = false;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                Module.build(ModuleE.TX),
                Module.build(ModuleE.AC),
                Module.build(ModuleE.LG),
                Module.build(ModuleE.CS),
                Module.build(ModuleE.NW)
        };
    }

    /**
     * 返回当前模块的描述信息
     * @return
     */
    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.BL.abbr, "1.0");
    }


    /**
     * 初始化模块信息,比如初始化RockDB等,在此处初始化后,可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        try {
            super.init();
            initDb();
            chainManager.initChain();
            ModuleHelper.init(this);
        } catch (Exception e) {
            Log.error("BlockBootstrap init error!");
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private void initDb() throws Exception {
        //读取配置文件,数据存储根目录,初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(blockConfig.getDataFolder());
        RocksDBService.createTable(CHAIN_LATEST_HEIGHT);
        RocksDBService.createTable(CHAIN_PARAMETERS);
        RocksDBService.createTable(PROTOCOL_CONFIG);
    }

    /**
     * 已完成spring init注入,开始启动模块
     * @return 如果启动完成返回true, 模块将进入ready状态, 若启动失败返回false, 10秒后会再次调用此方法
     */
    @Override
    public boolean doStart() {
        try {
            while (!isDependencieReady(new Module(ModuleE.TX.abbr, "1.0"))) {
                Thread.sleep(1000);
            }
            //启动链
            chainManager.runChain();
        } catch (Exception e) {
            Log.error("block module doStart error!" + e);
            return false;
        }
        Log.info("block module ready");
        return true;
    }

    /**
     * 所有外部依赖进入ready状态后会调用此方法,正常启动后返回Running状态
     * @return
     */
    @Override
    public RpcModuleState onDependenciesReady() {
        Log.info("block onDependenciesReady");
        NulsDateUtils.getInstance().start();
        if (started) {
            List<Integer> chainIds = ContextManager.CHAIN_ID_LIST;
            for (Integer chainId : chainIds) {
                BlockSynchronizer.syn(chainId);
//                ContextManager.getContext(chainId).setStatus(StatusEnum.RUNNING);
            }
        } else {
            //开启区块同步线程
            List<Integer> chainIds = ContextManager.CHAIN_ID_LIST;
            for (Integer chainId : chainIds) {
                BlockSynchronizer.syn(chainId);
            }
            //开启分叉链处理线程
            ScheduledThreadPoolExecutor forkExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("fork-chains-monitor"));
            forkExecutor.scheduleWithFixedDelay(ForkChainsMonitor.getInstance(), 0, blockConfig.getForkChainsMonitorInterval(), TimeUnit.MILLISECONDS);
            //开启孤儿链处理线程
            ScheduledThreadPoolExecutor orphanExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("orphan-chains-monitor"));
            orphanExecutor.scheduleWithFixedDelay(OrphanChainsMonitor.getInstance(), 0, blockConfig.getOrphanChainsMonitorInterval(), TimeUnit.MILLISECONDS);
            //开启孤儿链维护线程
            ScheduledThreadPoolExecutor maintainExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("orphan-chains-maintainer"));
            maintainExecutor.scheduleWithFixedDelay(OrphanChainsMaintainer.getInstance(), 0, blockConfig.getOrphanChainsMaintainerInterval(), TimeUnit.MILLISECONDS);
            //开启数据库大小监控线程
            ScheduledThreadPoolExecutor dbSizeExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("storage-size-monitor"));
            dbSizeExecutor.scheduleWithFixedDelay(StorageSizeMonitor.getInstance(), 0, blockConfig.getStorageSizeMonitorInterval(), TimeUnit.MILLISECONDS);
            //开启区块监控线程
            ScheduledThreadPoolExecutor monitorExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("network-monitor"));
            monitorExecutor.scheduleWithFixedDelay(NetworkResetMonitor.getInstance(), 0, blockConfig.getNetworkResetMonitorInterval(), TimeUnit.MILLISECONDS);
            //开启交易组获取线程
            ScheduledThreadPoolExecutor txGroupExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("txGroup-requestor"));
            txGroupExecutor.scheduleWithFixedDelay(TxGroupRequestor.getInstance(), 0, blockConfig.getTxGroupRequestorInterval(), TimeUnit.MILLISECONDS);
            //开启节点数量监控线程
            ScheduledThreadPoolExecutor nodesExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("nodes-monitor"));
            nodesExecutor.scheduleWithFixedDelay(NodesMonitor.getInstance(), 0, blockConfig.getNodesMonitorInterval(), TimeUnit.MILLISECONDS);

//            new Thread(new Spammer()).start();
            started = true;
        }
        return RpcModuleState.Running;
    }

    /**
     * 某个外部依赖连接丢失后,会调用此方法,可控制模块状态,如果返回Ready,则表明模块退化到Ready状态,当依赖重新准备完毕后,将重新触发onDependenciesReady方法,若返回的状态是Running,将不会重新触发onDependenciesReady
     * @param module
     * @return
     */
    @Override
    public RpcModuleState onDependenciesLoss(Module module) {
        List<Integer> chainIds = ContextManager.CHAIN_ID_LIST;
        for (Integer chainId : chainIds) {
            ContextManager.getContext(chainId).setStatus(StatusEnum.INITIALIZING);
        }
        return RpcModuleState.Ready;
    }

    @Override
    public void onDependenciesReady(Module module) {
        if (ModuleE.NW.abbr.equals(module.getName())) {
            RegisterHelper.registerMsg(ProtocolGroupManager.getOneProtocol());
        }
        if (ModuleE.PU.abbr.equals(module.getName())) {
            ContextManager.CHAIN_ID_LIST.forEach(RegisterHelper::registerProtocol);
        }
    }
}
