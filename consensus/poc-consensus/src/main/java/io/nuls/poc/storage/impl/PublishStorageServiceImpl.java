package io.nuls.poc.storage.impl;

import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.storage.PublishStorageService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 惩罚管理实现类
 * @author tag
 * 2018/11/6
 * */
@Service
public class PublishStorageServiceImpl implements PublishStorageService{
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

    /*@Override
    public void afterPropertiesSet() throws NulsException {
        try {
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH);
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
    }*/
}
