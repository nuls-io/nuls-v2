package io.nuls.crosschain.nuls.srorage.imp;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.srorage.CommitedOtherCtxService;

import java.util.ArrayList;
import java.util.List;

/**
 * 已打包的其他链签名拜占庭通过的交易数据库操作相关实现类
 * Other packaged chain signatures Byzantine-approved transactions
 *
 * @author  tag
 * 2019/6/21
 * */
@Component
public class CommitedOtherCtxServiceImpl implements CommitedOtherCtxService {
    @Override
    public boolean save(NulsHash atxHash, Transaction ctx, int chainID) {
        try {
            if(atxHash == null || ctx == null){
                return false;
            }
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_COMMITED_CTX+chainID,atxHash.getBytes(),ctx.serialize());
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
            byte[] txBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_COMMITED_CTX+chainID,atxHash.getBytes());
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
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_COMMITED_CTX+chainID,atxHash.getBytes());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<Transaction> getList(int chainID){
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_COMMITED_CTX+chainID);
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
