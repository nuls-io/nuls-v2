package io.nuls.crosschain.nuls.srorage.imp;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPo;
import io.nuls.crosschain.nuls.srorage.SendHeightService;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 广播给其他链节点的区块高度和广播的跨链交易Hash列表数据库相关操作
 * Block Height Broadcast to Other Chain Nodes and Related Operation of Broadcast Cross-Chain Transaction Hash List Database
 *
 * @author  tag
 * 2019/4/16
 * */
@Component
public class SendHeightServiceImpl implements SendHeightService {
    @Override
    public boolean save(long height, SendCtxHashPo po, int chainID) {
        if(height == 0 || po == null){
            return false;
        }
        try {
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_SEND_HEIGHT+chainID, ByteUtils.longToBytes(height),po.serialize());
        }catch(Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public SendCtxHashPo get(long height, int chainID) {
        if(height == 0){
            return null;
        }
        try {
            byte[] valueBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_SEND_HEIGHT+chainID, ByteUtils.longToBytes(height));
            if(valueBytes == null){
                return null;
            }
            SendCtxHashPo po = new SendCtxHashPo();
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
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_SEND_HEIGHT+chainID,ByteUtils.longToBytes(height));
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public Map<Long, SendCtxHashPo> getList(int chainID) {
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_SEND_HEIGHT+chainID);
            Map<Long, SendCtxHashPo> poMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
            for (Entry<byte[], byte[]> entry:list) {
                SendCtxHashPo po = new SendCtxHashPo();
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
