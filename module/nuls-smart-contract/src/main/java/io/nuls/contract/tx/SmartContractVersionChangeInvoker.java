package io.nuls.contract.tx;

import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.VersionChangeInvoker;

/**
 * @author: PierreLuo
 * @date: 2019-12-06
 */
public class SmartContractVersionChangeInvoker implements VersionChangeInvoker {

    /**
     *
     * 协议升级
     *
     * @param currentChainId
     */
    @Override
    public void process(int currentChainId) {
        ChainManager.chainHandle(currentChainId);
        Short currentVersion = ProtocolGroupManager.getCurrentVersion(currentChainId);
        Log.info("触发协议升级，chainId: [{}], 版本为: [{}]", currentChainId, currentVersion);

    }
}
