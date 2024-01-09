package io.nuls.crosschain.srorage.imp;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.utils.LoggerUtil;
import io.nuls.crosschain.utils.manager.ChainManager;

/**
 * 已注册跨链的交易数据库操作实现类
 * Registered Cross-Chain Transaction Database Operations Implementation Class
 *
 * @author  tag
 * 2019/5/30
 * */
@Component
public class RegisteredCrossChainServiceImpl implements RegisteredCrossChainService {
    private final byte[] key = NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN.getBytes();

    @Autowired
    private ChainManager chainManager;

    @Override
    public boolean save(RegisteredChainMessage registeredChainMessage) {
        registeredChainMessage.getChainInfoList().stream().filter(d->d.getChainId() == 9)
                .forEach(chainInfo -> {
                    LoggerUtil.commonLog.info("chain id {} 验证人列表：{}",chainInfo.getChainId(),chainInfo.getVerifierList());
                    LoggerUtil.commonLog.info("当前高度:{}",chainManager.getChainHeaderMap().get(1).getHeight() + 1);
                });
        try {
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN, key,registeredChainMessage.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public RegisteredChainMessage get() {
        try {
            byte[] messageBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN,key);
            if(messageBytes == null){
                return null;
            }
            RegisteredChainMessage registeredChainMessage = new RegisteredChainMessage();
            registeredChainMessage.parse(messageBytes,0);
            return registeredChainMessage;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }


    @Override
    public boolean canCross(int assetChainId, int assetId) {
        RegisteredChainMessage all = get();
        if(all == null || all.getChainInfoList() == null){
            Log.error("数据为空了");
            return false;
        }
        return all.getChainInfoList().stream().
                anyMatch(chainInfo->
                        chainInfo.getAssetInfoList().stream().anyMatch(
                                assetInfo-> assetInfo.getAssetId() == assetId && chainInfo.getChainId() == assetChainId));
    }

}
