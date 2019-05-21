package io.nuls.protocol;

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
import io.nuls.core.rpc.util.ModuleHelper;
import io.nuls.protocol.manager.ChainManager;
import io.nuls.protocol.model.ProtocolConfig;

import static io.nuls.protocol.constant.Constant.PROTOCOL_CONFIG;
import static io.nuls.protocol.constant.Constant.VERSION;

/**
 * 协议升级模块启动类
 *
 * @author captain
 * @version 1.0
 * @date 19-3-4 下午4:09
 */
@Component
public class ProtocolBootstrap extends RpcModule {

    @Autowired
    public static ProtocolConfig protocolConfig;

    @Autowired
    private ChainManager chainManager;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    /**
     * 返回此模块的依赖模块
     *
     * @return
     */
    @Override
    public Module[] declareDependent() {
        return new Module[]{Module.build(ModuleE.BL)};
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
     * 初始化模块信息,比如初始化RockDB等,在此处初始化后,可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        try {
            super.init();
            initDB();
            chainManager.initChain();
            ModuleHelper.init(this);
        } catch (Exception e) {
            Log.error("ProtocolBootstrap init error!");
            throw new RuntimeException(e);
        }
    }

    /**
     * 初始化数据库
     * Initialization database
     */
    private void initDB() throws Exception {
        //读取配置文件,数据存储根目录,初始化打开该目录下所有表连接并放入缓存
        RocksDBService.init(protocolConfig.getDataFolder());
        RocksDBService.createTable(PROTOCOL_CONFIG);
        RocksDBService.createTable(VERSION);
    }

    /**
     * 已完成spring init注入,开始启动模块
     * @return 如果启动完成返回true, 模块将进入ready状态, 若启动失败返回false, 10秒后会再次调用此方法
     */
    @Override
    public boolean doStart() {
        try {
            while (!isDependencieReady(new Module(ModuleE.BL.abbr, ROLE))) {
                Thread.sleep(1000);
            }
            //启动链
            chainManager.runChain();
        } catch (Exception e) {
            Log.error("protocol module doStart error!");
            return false;
        }
        Log.info("protocol module ready");
        return true;
    }

    /**
     * 所有外部依赖进入ready状态后会调用此方法,正常启动后返回Running状态
     * @return
     */
    @Override
    public RpcModuleState onDependenciesReady() {
        Log.info("protocol onDependenciesReady");
        return RpcModuleState.Running;
    }

    /**
     * 某个外部依赖连接丢失后,会调用此方法,可控制模块状态,如果返回Ready,则表明模块退化到Ready状态,当依赖重新准备完毕后,将重新触发onDependenciesReady方法,若返回的状态是Running,将不会重新触发onDependenciesReady
     * @param module
     * @return
     */
    @Override
    public RpcModuleState onDependenciesLoss(Module module) {
        return RpcModuleState.Running;
    }

}
