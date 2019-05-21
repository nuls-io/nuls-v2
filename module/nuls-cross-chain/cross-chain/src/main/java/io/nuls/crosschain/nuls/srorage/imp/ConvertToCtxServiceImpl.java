package io.nuls.crosschain.nuls.srorage.imp;

import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.srorage.ConvertToCtxService;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 接收到新的跨链交易数据库相关操作
 * New Cross-Chain Transaction Database Related Operations
 *
 * @author  tag
 * 2019/4/16
 * */
@Component
public class ConvertToCtxServiceImpl implements ConvertToCtxService {
    @Override
    public boolean save(NulsDigestData originalHash, NulsDigestData localHash, int chainID) {
        if(originalHash == null || localHash == null){
            return false;
        }
        try {
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_CONVERT_TO_CTX+chainID,originalHash.serialize(),localHash.serialize());
        }catch(Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public NulsDigestData get(NulsDigestData originalHash, int chainID) {
        if(originalHash == null){
            return null;
        }
        try {
            byte[] valueBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_CONVERT_TO_CTX+chainID, originalHash.serialize());
            if(valueBytes == null){
                return null;
            }
            NulsDigestData localHash = new NulsDigestData();
            localHash.parse(valueBytes,0);
            return localHash;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }

    @Override
    public boolean delete(NulsDigestData originalHash, int chainID) {
        try {
            if(originalHash == null){
                return false;
            }
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_CONVERT_TO_CTX+chainID,originalHash.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<NulsDigestData> getList(int chainID){
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_CONVERT_TO_CTX+chainID);
            List<NulsDigestData> hashList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                NulsDigestData localHash = new NulsDigestData();
                localHash.parse(entry.getValue(),0);
                hashList.add(localHash);
            }
            return hashList;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }
}
