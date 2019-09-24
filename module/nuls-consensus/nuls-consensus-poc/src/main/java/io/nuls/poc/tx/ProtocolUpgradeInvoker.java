package io.nuls.poc.tx;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.storage.ConfigService;
import io.nuls.poc.utils.manager.ChainManager;

/**
 * @author: tag
 * @date: 2019/09/12
 */
public class ProtocolUpgradeInvoker implements VersionChangeInvoker {

    @Override
    public void process(int chainId) {
        ChainManager chainManager = SpringLiteContext.getBean(ChainManager.class);
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
        }
        try {
            long time = NulsDateUtils.getCurrentTimeSeconds();
            chain.getLogger().info("协议升级完成,升级时间：{}",time);
        }catch (Exception e){
            chain.getLogger().error("协议升级失败");
            chain.getLogger().error(e);
        }
    }
}
