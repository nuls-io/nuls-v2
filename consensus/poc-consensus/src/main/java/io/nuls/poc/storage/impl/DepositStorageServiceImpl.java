package io.nuls.poc.storage.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.poc.utils.util.PoConvertUtil;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 共识管理实现类
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
            boolean dbSuccess = RocksDBService.put(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainID,key,value);
            if(!dbSuccess){
                return false;
            }
            ConsensusManager.getInstance().addDeposit(chainID,PoConvertUtil.poToDeposit(depositPo));
            return true;
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
            boolean dbSuccess = RocksDBService.delete(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainID,key);
            if(!dbSuccess){
                return false;
            }
            ConsensusManager.getInstance().removeDeposit(chainID,hash);
            return true;
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

    /*@Override
    public void afterPropertiesSet() throws NulsException {
        try {
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT);
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
    }*/
}
