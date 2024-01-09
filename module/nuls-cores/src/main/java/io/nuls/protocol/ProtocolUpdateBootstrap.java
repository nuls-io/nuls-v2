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
 * Protocol upgrade module startup class
 *
 * @author captain
 * @version 1.0
 * @date 19-3-4 afternoon4:09
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
     * Return the description information of the current module
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
            //Start Chain
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
