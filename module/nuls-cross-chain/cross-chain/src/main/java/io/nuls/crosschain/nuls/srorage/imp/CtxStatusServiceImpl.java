package io.nuls.crosschain.nuls.srorage.imp;

import io.nuls.base.data.NulsHash;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.model.Entry;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.srorage.CtxStatusService;
import java.util.ArrayList;
import java.util.List;

/**
 * 跨链交易数据库相关操作实现类
 * Cross-Chain Transaction Database Related Operations
 *
 * @author  tag
 * 2019/6/24
 * */
@Component
public class CtxStatusServiceImpl implements CtxStatusService {

    @Override
    public boolean save(NulsHash atxHash, CtxStatusPO ctx, int chainID) {
        try {
            if(atxHash == null || ctx == null){
                return false;
            }
            return RocksDBService.put(NulsCrossChainConstant.DB_NAME_CTX_STATUS+chainID,atxHash.getBytes(),ctx.serialize());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public CtxStatusPO get(NulsHash atxHash, int chainID) {
        try {
            if(atxHash == null){
                return null;
            }
            byte[] txBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_CTX_STATUS+chainID,atxHash.getBytes());
            if(txBytes == null){
                return null;
            }
            CtxStatusPO tx = new CtxStatusPO();
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
            return RocksDBService.delete(NulsCrossChainConstant.DB_NAME_CTX_STATUS+chainID,atxHash.getBytes());
        }catch (Exception e){
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<CtxStatusPO> getList(int chainID){
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(NulsCrossChainConstant.DB_NAME_CTX_STATUS+chainID);
            List<CtxStatusPO> txList = new ArrayList<>();
            for (Entry<byte[], byte[]> entry:list) {
                CtxStatusPO tx = new CtxStatusPO();
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
