package io.nuls.crosschain.nuls.srorage.imp;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.po.LocalVerifierPO;
import io.nuls.crosschain.nuls.srorage.LocalVerifierService;

@Component
public class LocalVerifierServiceImpl implements LocalVerifierService {
    @Override
    public boolean save(LocalVerifierPO po, int chainID) {
        try {
            if(po == null){
                return false;
            }
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER, ByteUtils.intToBytes(chainID), po.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public LocalVerifierPO get(int chainID) {
        byte[] value = RocksDBService.get(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER,ByteUtils.intToBytes(chainID));
        if(value == null){
            Log.info("The chain verifier has not been initialized" );
            return null;
        }
        LocalVerifierPO po = new LocalVerifierPO();
        try {
            po.parse(value,0);
        }catch (Exception e){
            Log.error(e);
        }
        return po;
    }

    @Override
    public boolean delete(int chainID) {
        try {
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_LOCAL_VERIFIER,ByteUtils.intToBytes(chainID));
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }
}
