package io.nuls.transaction.db.rocksdb.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.basic.TransactionManager;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.VarInt;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.ConfirmedTxStorageService;

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
public class ConfirmedTxStorageServiceImpl implements ConfirmedTxStorageService {

    @Override
    public boolean saveTx(int chainId, Transaction tx) {
        if (tx == null) {
            return false;
        }
        byte[] txHashBytes = null;
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.put(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId, txHashBytes, tx.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }

    @Override
    public boolean saveTxList(int chainId, List<Transaction> txList) {
        if (null == txList || txList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> txPoMap = new HashMap<>();
        try {
            for (Transaction tx : txList) {
                //序列化对象为byte数组存储
                txPoMap.put(tx.getHash().serialize(), tx.serialize());
            }
            return RocksDBService.batchPut(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId, txPoMap);
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(TxErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

    @Override
    public Transaction getTx(int chainId, NulsDigestData hash) {
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
        byte[] txBytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId, hashBytes);
        Transaction tx = null;
        if (null != txBytes) {
            try {
                tx = TransactionManager.getInstance(new NulsByteBuffer(txBytes, 0));
            } catch (Exception e) {
                Log.error(e);
                return null;
            }
        }
        return tx;
    }

    @Override
    public boolean removeTx(int chainId,NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.delete(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId, hash.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }

    @Override
    public boolean removeTxList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return false;
        }

        try {
            //delete transaction
            return RocksDBService.deleteKeys(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId, hashList);
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<Transaction> getTxList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return null;
        }
        List<Transaction> txList = new ArrayList<>();
        //根据交易hash批量查询交易数据
        List<byte[]> list = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId, hashList);
        if (list != null) {
            for (byte[] value : list) {
                Transaction tx = new Transaction();
                try {
                    tx.parse(value, 0);
                } catch (NulsException e) {
                    Log.error(e);
                }
                txList.add(tx);
            }
        }
        return txList;
    }

    /**
     * 处理跨链交易生效高度
     */
    private class CrossTxEffectList extends BaseNulsData{
        List<NulsDigestData> hashList;
        @Override
        protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
            int fromCount = hashList == null ? 0 : hashList.size();
            stream.writeVarInt(fromCount);
            if (null != hashList) {
                for (NulsDigestData hash : hashList) {
                    stream.writeNulsData(hash);
                }
            }
        }

        @Override
        public void parse(NulsByteBuffer byteBuffer) throws NulsException {
            int fromCount = (int) byteBuffer.readVarInt();
            if (0 < fromCount) {
                List<NulsDigestData> hashs = new ArrayList<>();
                for (int i = 0; i < fromCount; i++) {
                    hashs.add(byteBuffer.readNulsData(new NulsDigestData()));
                }
                this.hashList = hashs;
            }
        }

        @Override
        public int size() {
            int size = SerializeUtils.sizeOfVarInt(hashList == null ? 0 : hashList.size());
            if (null != hashList) {
                for (NulsDigestData hash : hashList) {
                    size += SerializeUtils.sizeOfNulsData(hash);
                }
            }
            return size;
        }
    }

    @Override
    public boolean saveCrossTxEffectList(int chainId, long height, List<NulsDigestData> hashList) {
        if (hashList == null || hashList.size() == 0 || height < 0) {
            return true;
        }
        CrossTxEffectList crossTxEffectList = new CrossTxEffectList();
        crossTxEffectList.hashList = hashList;
        boolean result = false;
        try {
            result = RocksDBService.put(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId,
                    new VarInt(height).encode(), crossTxEffectList.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
       return result;

    }

    @Override
    public List<NulsDigestData> getCrossTxEffectList(int chainId, long height) {
        List<NulsDigestData> hashList = new ArrayList<>();
        if ( height < 0) {
            return hashList;
        }
        try {
            byte[] bytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId, new VarInt(height).encode());
            CrossTxEffectList crossTxEffectList = new CrossTxEffectList();
            crossTxEffectList.parse(new NulsByteBuffer(bytes));
            hashList = crossTxEffectList.hashList;
        } catch (Exception e) {
            Log.error(e);
        }
        return hashList;

    }

    @Override
    public boolean removeCrossTxEffectList(int chainId, long height) {
        if ( height < 0) {
            return false;
        }
        try {
            //delete transaction
            return RocksDBService.delete(TxDBConstant.DB_TRANSACTION_CONFIRMED + chainId, new VarInt(height).encode());
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

}
