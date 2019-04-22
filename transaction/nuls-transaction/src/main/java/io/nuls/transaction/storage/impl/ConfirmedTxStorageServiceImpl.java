package io.nuls.transaction.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.VarInt;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.storage.ConfirmedTxStorageService;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
@Component
public class ConfirmedTxStorageServiceImpl implements ConfirmedTxStorageService {

    @Override
    public boolean saveTx(int chainId, TransactionConfirmedPO tx) {
        if (tx == null) {
            return false;
        }
        byte[] txHashBytes = null;
        try {
            txHashBytes = tx.getTx().getHash().serialize();
        } catch (IOException e) {
            LOG.error(e);
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.put(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId, txHashBytes, tx.serialize());
        } catch (Exception e) {
            LOG.error(e);
        }
        return result;
    }

    @Override
    public boolean saveTxList(int chainId, List<TransactionConfirmedPO> txList) {
        if (null == txList || txList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> txPoMap = new HashMap<>();
        try {
            for (TransactionConfirmedPO tx : txList) {
                //序列化对象为byte数组存储
                txPoMap.put(tx.getTx().getHash().serialize(), tx.serialize());
            }
            return RocksDBService.batchPut(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId, txPoMap);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new NulsRuntimeException(TxErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

    @Override
    public TransactionConfirmedPO getTx(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        try {
            return getTx(chainId, hash.serialize());
        } catch (IOException e) {
            LOG.error(e);
            throw new NulsRuntimeException(e);
        }
    }

    @Override
    public TransactionConfirmedPO getTx(int chainId, String hash) {
        if(StringUtils.isBlank(hash)){
            return null;
        }
        return getTx(chainId, HexUtil.decode(hash));
    }

    private TransactionConfirmedPO getTx(int chainId, byte[] hashSerialize){
        byte[] txBytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId, hashSerialize);
        TransactionConfirmedPO tx = null;
        if (null != txBytes) {
            try {
                tx = TxUtil.getInstance(txBytes, TransactionConfirmedPO.class);
            } catch (Exception e) {
                LOG.error(e);
                return null;
            }
        }
        return tx;
    }

    @Override
    public boolean removeTx(int chainId, String hash) {
        boolean result = false;
        try {
            result = RocksDBService.delete(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId, HexUtil.decode(hash));
        } catch (Exception e) {
            LOG.error(e);
        }
        return result;
    }

    @Override
    public boolean removeTx(int chainId, NulsDigestData hash) {
        boolean result = false;
        try {
            result = RocksDBService.delete(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId, hash.serialize());
        } catch (Exception e) {
            LOG.error(e);
        }
        return result;
    }

    @Override
    public boolean removeTxList(int chainId, List<Transaction> txList) {
        try {
            List<byte[]> hashList = new ArrayList<>();
            for(Transaction tx : txList){
                hashList.add(tx.getHash().serialize());
            }
            return removeTxListByHashBytes(chainId, hashList);
        } catch (IOException e) {
            LOG.error(e);
            return false;
        }
    }

    @Override
    public boolean removeTxListByHashBytes(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return false;
        }
        try {
            //delete transaction
            return RocksDBService.deleteKeys(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId, hashList);
        } catch (Exception e) {
            LOG.error(e);
        }
        return false;
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
            result = RocksDBService.put(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId,
                    new VarInt(height).encode(), crossTxEffectList.serialize());
        } catch (Exception e) {
            LOG.error(e);
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
            byte[] bytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId, new VarInt(height).encode());
            if(null == bytes){
                return hashList;
            }
            CrossTxEffectList crossTxEffectList = new CrossTxEffectList();
            crossTxEffectList.parse(new NulsByteBuffer(bytes));
            hashList = crossTxEffectList.hashList;
        } catch (Exception e) {
            LOG.error(e);
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
            return RocksDBService.delete(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId, new VarInt(height).encode());
        } catch (Exception e) {
            LOG.error(e);
        }
        return false;
    }

}
