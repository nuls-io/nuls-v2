package io.nuls.protocol;

import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.thread.monitor.ProtocolMonitor;
import io.nuls.protocol.utils.ConfigLoader;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.protocol.constant.Constant.DATA_PATH;
import static io.nuls.protocol.constant.Constant.PROTOCOL_CONFIG;

/**
 * 区块模块启动类
 *
 * @author captain
 * @version 1.0
 * @date 19-3-4 下午4:09
 */
@Component
public class ProtocolModule extends RpcModule {

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
        return new Module[]{};
    }

    /**
     * 返回当前模块的描述信息
     * @return
     */
    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.PU.abbr, "1.0");
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
            RocksDBService.createTable(PROTOCOL_CONFIG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.info("protocol module ready");
        return true;
    }

    /**
     * 所有外部依赖进入ready状态后会调用此方法，正常启动后返回Running状态
     * @return
     */
    @Override
    public RpcModuleState onDependenciesReady() {
        Log.info("protocol 依赖准备就绪");
        //加载配置
        try {
            ConfigLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //开启一些监控线程
        ScheduledThreadPoolExecutor executor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("protocol-monitor"));
        executor.scheduleWithFixedDelay(ProtocolMonitor.getInstance(), 0, 5, TimeUnit.SECONDS);
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
