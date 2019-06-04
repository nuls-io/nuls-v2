package io.nuls.crosschain.nuls.srorage.imp;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.srorage.NewCtxService;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.log.Log;

import java.util.ArrayList;
import java.util.List;
/**
 * 新创建的跨链交易数据库相关操作
 * New Cross-Chain Transaction Database Related Operations
 *
 * @author  tag
 * 2019/4/16
 * */
@Component
public class NewCtxServiceImpl implements NewCtxService {
    @Override
    public boolean save(NulsHash atxHash, Transaction ctx, int chainID) {
        try {
            if(atxHash == null || ctx == null){
                return false;
            }
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_NEW_CTX+chainID,atxHash.getBytes(),ctx.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public Transaction get(NulsHash atxHash, int chainID) {
        try {
            if(atxHash == null){
                return null;
            }
            byte[] txBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_NEW_CTX+chainID,atxHash.getBytes());
            if(txBytes == null){
                return null;
            }
            Transaction tx = new Transaction();
            tx.parse(txBytes,0);
            return tx;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }

    @Override
    public boolean delete(NulsHash atxHash, int chainID) {
        try {
            if(atxHash == null){
                return false;
            }
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_NEW_CTX+chainID,atxHash.getBytes());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<Transaction> getList(int chainID){
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_NEW_CTX+chainID);
            List<Transaction> txList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                Transaction tx = new Transaction();
                tx.parse(entry.getValue(),0);
                txList.add(tx);
            }
            return txList;
        }catch (Exception e){
            Log.error(e);
        }
        return null;
    }
}
