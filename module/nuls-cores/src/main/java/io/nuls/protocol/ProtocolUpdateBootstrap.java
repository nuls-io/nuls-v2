package io.nuls.protocol;

import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.protocol.manager.ChainManager;

/**
 * 协议升级模块启动类
 *
 * @author captain
 * @version 1.0
 * @date 19-3-4 下午4:09
 */
@Component
public class ProtocolUpdateBootstrap implements INulsCoresBootstrap {

    @Autowired
    public static NulsCoresConfig protocolConfig;

    @Autowired
    private ChainManager chainManager;

    @Override
    public int order() {
        return 1;
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
        return new Module(ModuleE.PU.abbr, "1.0");
    }

    public void init() {
        try {
            chainManager.initChain();
        } catch (Exception e) {
            Log.error("ProtocolUpdateBootstrap init error!");
            throw new RuntimeException(e);
        }
    }


    private boolean doStart() {
        try {
            //启动链
            chainManager.runChain();
        } catch (Exception e) {
            Log.error("protocol module doStart error!");
            return false;
        }
        Log.info("protocol module ready");
        return true;
    }

    @Override
    public void onDependenciesReady() {
        doStart();
        Log.info("protocol onDependenciesReady");
    }

}
