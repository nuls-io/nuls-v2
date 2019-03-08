package io.nuls.block;

import io.nuls.block.manager.ContextManager;
import io.nuls.block.service.BlockService;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.thread.monitor.*;
import io.nuls.block.utils.ConfigLoader;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.block.utils.module.ProtocolUtil;
import io.nuls.block.utils.module.TransactionUtil;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

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

    public static void main(String[] args) {
        NulsRpcModuleBootstrap.run("io.nuls", new String[]{HostInfo.getLocalIP() + ":8887/ws"});
    }

    /**
     * 返回此模块的依赖模块
     *
     * @return
     */
    @Override
    public Module[] getDependencies() {
        return new Module[]{
                new Module(ModuleE.TX.abbr, "1.0"),
                new Module(ModuleE.NW.abbr, "1.0"),
//                new Module(ModuleE.PU.abbr, "1.0"),
                new Module(ModuleE.CS.abbr, "1.0"),
                new Module(ModuleE.LG.abbr, "1.0"),
                new Module(ModuleE.AC.abbr, "1.0")
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
     * 初始化模块信息，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        super.init();
        initCfg();
        //读取配置文件，数据存储根目录，初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(DATA_PATH);
    }

    /**
     * 已完成spring init注入，开始启动模块
     * @return 如果启动完成返回true，模块将进入ready状态，若启动失败返回false，10秒后会再次调用此方法
     */
    @Override
    public boolean doStart() {
        try {
            RocksDBService.createTable(CHAIN_LATEST_HEIGHT);
            RocksDBService.createTable(CHAIN_PARAMETERS);
            RocksDBService.createTable(PROTOCOL_CONFIG);
            //加载配置
            ConfigLoader.load();
            while (!isDependencieReady(new Module(ModuleE.TX.abbr, "1.0"))) {
                Thread.sleep(1000);
            }
            List<Integer> chainIds = ContextManager.chainIds;
            BlockService service = SpringLiteContext.getBean(BlockService.class);
            for (Integer chainId : chainIds) {
                //服务初始化
                service.init(chainId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("block module doStart error!");
            return false;
        }
        Log.info("block module ready");
        return true;
    }

    /**
     * 所有外部依赖进入ready状态后会调用此方法，正常启动后返回Running状态
     * @return
     */
    @Override
    public RpcModuleState onDependenciesReady() {
        Log.info("block onDependenciesReady");
        NetworkUtil.register();
        List<Integer> chainIds = ContextManager.chainIds;
        for (Integer chainId : chainIds) {
            ProtocolUtil.subscribe(chainId);
        }
        //开启区块同步线程
        ThreadUtils.createAndRunThread("block-synchronizer", BlockSynchronizer.getInstance());
        //开启分叉链处理线程
        ScheduledThreadPoolExecutor forkExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("fork-chains-monitor"));
        forkExecutor.scheduleWithFixedDelay(ForkChainsMonitor.getInstance(), 0, 10, TimeUnit.SECONDS);
        //开启孤儿链处理线程
        ScheduledThreadPoolExecutor orphanExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("orphan-chains-monitor"));
        orphanExecutor.scheduleWithFixedDelay(OrphanChainsMonitor.getInstance(), 0, 30, TimeUnit.SECONDS);
        //开启孤儿链维护线程
        ScheduledThreadPoolExecutor maintainExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("orphan-chains-maintainer"));
        maintainExecutor.scheduleWithFixedDelay(OrphanChainsMaintainer.getInstance(), 0, 2, TimeUnit.SECONDS);
        //开启数据库大小监控线程
        ScheduledThreadPoolExecutor dbSizeExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("storage-size-monitor"));
        dbSizeExecutor.scheduleWithFixedDelay(ChainsDbSizeMonitor.getInstance(), 0, 5, TimeUnit.MINUTES);
        //开启区块监控线程
        ScheduledThreadPoolExecutor monitorExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("network-monitor"));
        monitorExecutor.scheduleWithFixedDelay(NetworkResetMonitor.getInstance(), 0, 5, TimeUnit.MINUTES);
        return RpcModuleState.Running;
    }

    /**
     * 某个外部依赖连接丢失后，会调用此方法，可控制模块状态，如果返回Ready,则表明模块退化到Ready状态，当依赖重新准备完毕后，将重新触发onDependenciesReady方法，若返回的状态是Running，将不会重新触发onDependenciesReady
     * @param module
     * @return
     */
    @Override
    public RpcModuleState onDependenciesLoss(Module module) {
        return RpcModuleState.Ready;
    }

    public static void initCfg() {

    }

}
