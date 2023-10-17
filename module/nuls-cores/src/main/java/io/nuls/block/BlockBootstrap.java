package io.nuls.block;

import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.thread.monitor.*;
import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.util.AddressPrefixDatas;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.io.File;
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
public class BlockBootstrap implements INulsCoresBootstrap {

    @Autowired
    public static NulsCoresConfig blockConfig;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;

    @Autowired
    private ChainManager chainManager;

    public static boolean started = false;

    @Override
    public int order() {
        return 0;
    }

    @Override
    public void mainFunction(String[] args) {
        this.init();
    }

    /**
     * 返回当前模块的描述信息
     * @return
     */
    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.BL.abbr, "1.0");
    }


    public void init() {
        try {
            initDb();
            chainManager.initChain();
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
        RocksDBService.init(blockConfig.getDataPath() + File.separator + ModuleE.BL.name);
        RocksDBService.createTable(CHAIN_LATEST_HEIGHT);
        RocksDBService.createTable(PROTOCOL_CONFIG);
        RocksDBService.createTable(ROLLBACK_HEIGHT);
    }

    private boolean doStart() {
        try {
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
    public void onDependenciesReady() {
        Log.info("block onDependenciesReady");
        doStart();
        if (started) {
            List<Integer> chainIds = ContextManager.CHAIN_ID_LIST;
            for (Integer chainId : chainIds) {
                BlockSynchronizer.syn(chainId);
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
            started = true;
        }
    }

}
