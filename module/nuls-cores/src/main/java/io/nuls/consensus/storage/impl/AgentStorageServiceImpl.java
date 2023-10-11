package io.nuls.consensus.storage.impl;

import io.nuls.base.data.NulsHash;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.model.po.AgentPo;
import io.nuls.consensus.storage.AgentStorageService;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点信息管理实现类
 * Node Information Management Implementation Class
 *
 * @author tag
 * 2018/11/06
 * */
@Component
public class AgentStorageServiceImpl implements AgentStorageService{

    @Override
    public boolean save(AgentPo agentPo,int chainID) {
        if(agentPo == null || agentPo.getHash() == null){
            return false;
        }
        try {
            byte[] key = agentPo.getHash().getBytes();
            byte[] value = agentPo.serialize();
            return RocksDBService.put(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chainID,key,value);
        }catch (Exception e){
            Log.error(e);
            return  false;
        }
    }

    @Override
    public AgentPo get(NulsHash hash,int chainID) {
        if(hash == null){
            return  null;
        }
        try {
            byte[] key = hash.getBytes();
            byte[] value = RocksDBService.get(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chainID,key);
            if(value == null){
                return  null;
            }
            AgentPo agentPo = new AgentPo();
            agentPo.parse(value,0);
            agentPo.setHash(hash);
            return agentPo;
        }catch (Exception e){
            Log.error(e);
            return  null;
        }
    }

    @Override
    public boolean delete(NulsHash hash,int chainID) {
        if(hash == null){
            return  false;
        }
        try {
            byte[] key = hash.getBytes();
            return RocksDBService.delete(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chainID,key);
        }catch (Exception e){
            Log.error(e);
            return  false;
        }
    }

    @Override
    public List<AgentPo> getList(int chainID) throws NulsException {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chainID);
            List<AgentPo> agentList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                AgentPo po = new AgentPo();
                po.parse(entry.getValue(),0);
                NulsHash hash = new NulsHash(entry.getKey());
                po.setHash(hash);
                agentList.add(po);
            }
            return  agentList;
    }

    @Override
    public int size(int chainID) {
        List<byte[]> keyList = RocksDBService.keyList(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chainID);
        if(keyList != null){
            return keyList.size();
        }
        return 0;
    }
}
