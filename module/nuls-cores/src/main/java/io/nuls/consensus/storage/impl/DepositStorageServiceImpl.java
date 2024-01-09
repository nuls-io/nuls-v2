package io.nuls.consensus.storage.impl;

import io.nuls.base.data.NulsHash;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.model.po.DepositPo;
import io.nuls.consensus.storage.DepositStorageService;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 委托信息存储管理实现类
 * Delegated Information Storage Management Implementation Class
 *
 * @author tag
 * 2018/11/6
 */
@Component
public class DepositStorageServiceImpl implements DepositStorageService {

    @Override
    public boolean save(DepositPo depositPo, int chainID) {
        if (depositPo == null || depositPo.getTxHash() == null) {
            return false;
        }
        try {
            byte[] key = depositPo.getTxHash().getBytes();
            byte[] value = depositPo.serialize();
            return RocksDBService.put(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT + chainID, key, value);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public DepositPo get(NulsHash hash, int chainID) {
        if (hash == null) {
            return null;
        }
        try {
            byte[] value = RocksDBService.get(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT + chainID, hash.getBytes());
            if (value == null) {
                return null;
            }
            DepositPo po = new DepositPo();
            po.parse(value, 0);
            po.setTxHash(hash);
            return po;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(NulsHash hash, int chainID) {
        if (hash == null) {
            return false;
        }
        try {
            byte[] key = hash.getBytes();
            return RocksDBService.delete(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT + chainID, key);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public List<DepositPo> getList(int chainID) throws NulsException {
        List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT + chainID);
        List<DepositPo> depositList = new ArrayList<>();
        for (Entry<byte[], byte[]> entry : list) {
            DepositPo po = new DepositPo();
            po.parse(entry.getValue(), 0);
            NulsHash hash = new NulsHash(entry.getKey());
            po.setTxHash(hash);
            depositList.add(po);
        }
        return depositList;
    }

    @Override
    public int size(int chainID) {
        List<byte[]> keyList = RocksDBService.keyList(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT + chainID);
        if (keyList != null) {
            return keyList.size();
        }
        return 0;
    }
}
