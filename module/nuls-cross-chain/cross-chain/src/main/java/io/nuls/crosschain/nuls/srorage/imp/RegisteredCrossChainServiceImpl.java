package io.nuls.crosschain.nuls.srorage.imp;

import io.nuls.base.RPCUtil;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;

/**
 * 已注册跨链的交易数据库操作实现类
 * Registered Cross-Chain Transaction Database Operations Implementation Class
 *
 * @author  tag
 * 2019/5/30
 * */
@Component
public class RegisteredCrossChainServiceImpl implements RegisteredCrossChainService {
    @Override
    public boolean save(RegisteredChainMessage registeredChainMessage) {
        try {
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN, RPCUtil.decode(NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN),registeredChainMessage.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public RegisteredChainMessage get() {
        try {
            byte[] messageBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN,RPCUtil.decode(NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN));
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
}
