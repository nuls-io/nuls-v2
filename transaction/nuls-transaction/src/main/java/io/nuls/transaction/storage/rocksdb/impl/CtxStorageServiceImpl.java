package io.nuls.transaction.storage.rocksdb.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.storage.rocksdb.CtxStorageService;
import io.nuls.transaction.model.bo.CrossTx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.Log;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
@Service
public class CtxStorageServiceImpl implements CtxStorageService {

    @Override
    public boolean putTx(int chainId, CrossTx ctx) {
        if (null == ctx) {
            return false;
        }
        byte[] txHashBytes = null;
        try {
            txHashBytes = ctx.getTx().getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.put(TxDBConstant.DB_PROGRESS_CROSSCHAIN + chainId, txHashBytes, ctx.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }

    @Override
    public boolean putTxs(int chainId, List<CrossTx> ctxList) {
        if (null == ctxList || ctxList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> ctxMap = new HashMap<>();
        try {
            for (CrossTx ctx : ctxList) {
                //序列化对象为byte数组存储
                ctxMap.put(ctx.getTx().getHash().serialize(), ctx.serialize());
            }
            return RocksDBService.batchPut(TxDBConstant.DB_PROGRESS_CROSSCHAIN + chainId, ctxMap);
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(TxErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

    @Override
    public boolean removeTx(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        try {
            return RocksDBService.delete(TxDBConstant.DB_PROGRESS_CROSSCHAIN + chainId, hash.serialize());
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }

    }

    @Override
    public CrossTx getTx(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        byte[] hashBytes = null;
        try {
            hashBytes = hash.serialize();
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        byte[] txBytes = RocksDBService.get(TxDBConstant.DB_PROGRESS_CROSSCHAIN + chainId, hashBytes);

        if (null == txBytes) {
            return null;
        }
        CrossTx tx = new CrossTx();
        try {
            tx.parse(txBytes, 0);
        } catch (NulsException e) {
            Log.error(e);
        }
        return tx;
    }

    @Override
    public List<CrossTx> getTxList(int chainId) {
        List<CrossTx> ccTxPoList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(TxDBConstant.DB_PROGRESS_CROSSCHAIN + chainId);
            if (list != null) {
                for (byte[] value : list) {
                    CrossTx ccTx = new CrossTx();
                    //将byte数组反序列化为Object返回
                    ccTx.parse(value, 0);
                    ccTxPoList.add(ccTx);
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return ccTxPoList;
    }
}
