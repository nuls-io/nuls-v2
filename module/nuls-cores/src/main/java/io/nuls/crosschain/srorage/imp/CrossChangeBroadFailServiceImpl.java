package io.nuls.crosschain.srorage.imp;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.model.po.VerifierChangeSendFailPO;
import io.nuls.crosschain.srorage.CrossChangeBroadFailService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CrossChangeBroadFailServiceImpl implements CrossChangeBroadFailService {
    @Override
    public boolean save(long height, VerifierChangeSendFailPO po, int chainID) {
        if(height == 0 || po == null){
            return false;
        }
        try {
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_CROSS_CHANGE_FAILED+chainID, ByteUtils.longToBytes(height),po.serialize());
        }catch(Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public VerifierChangeSendFailPO get(long height, int chainID) {
        if(height == 0){
            return null;
        }
        try {
            byte[] valueBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_CROSS_CHANGE_FAILED+chainID, ByteUtils.longToBytes(height));
            if(valueBytes == null){
                return null;
            }
            VerifierChangeSendFailPO po = new VerifierChangeSendFailPO();
            po.parse(valueBytes,0);
            return po;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }

    @Override
    public boolean delete(long height, int chainID) {
        try {
            if(height == 0){
                return false;
            }
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_CROSS_CHANGE_FAILED+chainID, ByteUtils.longToBytes(height));
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public Map<Long, VerifierChangeSendFailPO> getList(int chainID) {
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_CROSS_CHANGE_FAILED+chainID);
            Map<Long, VerifierChangeSendFailPO> poMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
            for (Entry<byte[], byte[]> entry:list) {
                VerifierChangeSendFailPO po = new VerifierChangeSendFailPO();
                po.parse(entry.getValue(),0);
                poMap.put(ByteUtils.byteToLong(entry.getKey()), po);
            }
            return poMap;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }
}
