package io.nuls.consensus.storage.impl;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.model.po.PunishLogPo;
import io.nuls.consensus.storage.PunishStorageService;
import io.nuls.core.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 惩罚存贮管理实现类
 * Punishment Storage Management Implementation Class
 *
 * @author tag
 * 2018/11/6
 * */
@Component
public class PunishStorageServiceImpl implements PunishStorageService {
    @Override
    public boolean save(PunishLogPo po,int chainID) {
        if (po == null || po.getKey() == null) {
            return false;
        }
        try {
            return  RocksDBService.put(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH+chainID,po.getKey(),po.serialize());
        }catch (Exception  e){
            Log.error(e);
            return false;
        }
    }

    @Override
    public boolean delete(byte[] key,int chainID) {
        if(key == null){
            return false;
        }
        try {
            return RocksDBService.delete(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH+chainID,key);
        }catch (Exception e){
            Log.error(e);
            return false;
        }
    }

    @Override
    public List<PunishLogPo> getPunishList(int chainID)throws Exception{
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH+chainID);
            List<PunishLogPo> agentList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                PunishLogPo po = new PunishLogPo();
                po.parse(entry.getValue(),0);
                agentList.add(po);
            }
            return  agentList;
        }catch (Exception e){
            Log.error(e);
            throw e;
        }
    }
}
