package io.nuls.crosschain.nuls.srorage.imp;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.srorage.CtxStateService;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.log.Log;

import java.util.List;

/**
 * 跨链交易处理结果数据库相关操作类
 * Cross-Chain Transaction Processing Result Database Related Operating Classes
 *
 * @author  tag
 * 2019/4/16
 * */
@Component
public class CtxStateServiceImpl implements CtxStateService {
    @Override
    public boolean save(byte[] atxHash, int chainID) {
        try {
            if(atxHash == null){
                return false;
            }
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_CTX_STATE+chainID,atxHash, new byte[0]);
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public boolean get(byte[] atxHash, int chainID) {
        try {
            if(atxHash == null){
                return false;
            }
            byte[] txBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_CTX_STATE+chainID,atxHash);
            if(txBytes != null){
                return true;
            }
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public boolean delete(byte[] atxHash, int chainID) {
        try {
            if(atxHash == null){
                return false;
            }
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_CTX_STATE+chainID,atxHash);
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<byte[]> getList(int chainID) {
        return null;
    }
}
