package io.nuls.poc.storage.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.tools.core.annotation.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 委托信息存储管理实现类
 * Delegated Information Storage Management Implementation Class
 *
 * @author tag
 * 2018/11/6
 * */
@Service
public class DepositStorageServiceImpl implements DepositStorageService {

    @Override
    public boolean save(DepositPo depositPo,int chainID) {
        if (depositPo == null || depositPo.getTxHash() == null) {
            return false;
        }
        try {
            byte[] key = depositPo.getTxHash().serialize();
            byte[] value = depositPo.serialize();
            return RocksDBService.put(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainID,key,value);
        }catch (Exception e){
            Log.error(e);
            return false;
        }
    }

    @Override
    public DepositPo get(NulsDigestData hash,int chainID) {
        if(hash == null){
            return  null;
        }
        try {
            byte[] value = RocksDBService.get(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainID,hash.serialize());
            if (value == null){
                return null;
            }
            DepositPo po = new DepositPo();
            po.parse(value,0);
            po.setTxHash(hash);
            return  po;
        }catch (Exception e){
            Log.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(NulsDigestData hash,int chainID) {
        if(hash == null){
            return  false;
        }
        try {
            byte[] key = hash.serialize();
            return RocksDBService.delete(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainID,key);
        }catch (Exception e){
            Log.error(e);
            return  false;
        }
    }

    @Override
    public List<DepositPo> getList(int chainID) throws Exception{
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainID);
            List<DepositPo> depositList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                DepositPo po = new DepositPo();
                po.parse(entry.getValue(),0);
                NulsDigestData hash = new NulsDigestData();
                hash.parse(entry.getKey(),0);
                po.setTxHash(hash);
                depositList.add(po);
            }
            return  depositList;
        }catch (Exception e){
            Log.error(e);
            throw e;
        }
    }

    @Override
    public int size(int chainID) {
        List<byte[]> keyList = RocksDBService.keyList(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainID);
        if(keyList != null){
            return keyList.size();
        }
        return 0;
    }
}
