package io.nuls.crosschain.nuls.srorage.imp;

import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.srorage.ConvertFromCtxService;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.log.Log;

import java.util.ArrayList;
import java.util.List;
/**
 * 记录本链发起广播给其他链的交易新的跨链交易数据库相关操作
 * New Cross-Chain Transaction Database Related Operations
 *
 * @author  tag
 * 2019/4/16
 * */
@Component
public class ConvertFromCtxServiceImpl implements ConvertFromCtxService {
    @Override
    public boolean save(byte[] originalHash, byte[] localHash, int chainID) {
        if(originalHash == null || localHash == null){
            return false;
        }
        try {
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_CONVERT_FROM_CTX+chainID,originalHash,localHash);
        }catch(Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public byte[] get(byte[] originalHash, int chainID) {
        if(originalHash == null){
            return null;
        }
        try {
            byte[] valueBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_CONVERT_FROM_CTX+chainID, originalHash);
            return valueBytes;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }

    @Override
    public boolean delete(byte[] originalHash, int chainID) {
        try {
            if(originalHash == null){
                return false;
            }
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_CONVERT_FROM_CTX+chainID,originalHash);
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<byte[]> getList(int chainID){
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_CONVERT_FROM_CTX+chainID);
            List<byte[]> hashList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                hashList.add(entry.getValue());
            }
            return hashList;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }
}
