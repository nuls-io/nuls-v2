package io.nuls.transaction.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.model.po.TransactionUnconfirmedPO;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 验证通过但未打包的交易
 * Save verified transaction (unpackaged)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
@Component
public class UnconfirmedTxStorageServiceImpl implements UnconfirmedTxStorageService {

    @Override
    public boolean putTx(int chainId, Transaction tx) {
        return putTx(chainId,tx, 0L);
    }

    @Override
    public boolean putTx(int chainId, Transaction tx, long originalSendNanoTime) {
        if (tx == null) {
            return false;
        }
        TransactionUnconfirmedPO txPO = new TransactionUnconfirmedPO(tx, NulsDateUtils.getCurrentTimeSeconds(), originalSendNanoTime);
        byte[] txHashBytes;
        txHashBytes = tx.getHash().getBytes();
        boolean result = false;
        try {
            result = RocksDBService.put(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, txHashBytes, txPO.serialize());
        } catch (Exception e) {
            LOG.error(e);
        }
        return result;
    }

    @Override
    public boolean putTxList(int chainId, List<TransactionNetPO> txNetPOList) {
        if (null == txNetPOList || txNetPOList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> txPOMap = new HashMap<>();
        try {
            for (TransactionNetPO txNetPO : txNetPOList) {
                Transaction tx = txNetPO.getTx();
                TransactionUnconfirmedPO txPO = new TransactionUnconfirmedPO(tx, NulsDateUtils.getCurrentTimeSeconds(), txNetPO.getOriginalSendNanoTime());
                //序列化对象为byte数组存储
                txPOMap.put(tx.getHash().getBytes(), txPO.serialize());
            }
            return RocksDBService.batchPut(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, txPOMap);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new NulsRuntimeException(TxErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }


    @Override
    public TransactionUnconfirmedPO getTx(int chainId, NulsHash hash) {
        if (hash == null) {
            return null;
        }
        return getTx(chainId, hash.getBytes());
    }

    @Override
    public boolean isExists(int chainId, NulsHash hash) {
        return RocksDBService.keyMayExist(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, hash.getBytes());
//        byte[] txBytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, hash.getBytes());
//        if (null != txBytes && txBytes.length > 0) {
//            return true;
//        }
//        return false;
    }

    @Override
    public TransactionUnconfirmedPO getTx(int chainId, String hash) {
        if (StringUtils.isBlank(hash)) {
            return null;
        }
        return getTx(chainId, HexUtil.decode(hash));
    }

    private TransactionUnconfirmedPO getTx(int chainId, byte[] hashSerialize) {
        byte[] txBytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, hashSerialize);
        TransactionUnconfirmedPO txPO = null;
        if (null != txBytes) {
            try {
                txPO = new TransactionUnconfirmedPO();
                txPO.parse(new NulsByteBuffer(txBytes, 0));
            } catch (Exception e) {
                LOG.error(e);
                return null;
            }
        }
        return txPO;
    }


    @Override
    public boolean removeTx(int chainId, NulsHash hash) {
        if (hash == null) {
            return false;
        }
        return removeTx(chainId, hash.getBytes());
    }

    @Override
    public boolean removeTx(int chainId, byte[] hash) {
        boolean result = false;
        try {
            result = RocksDBService.delete(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, hash);
        } catch (Exception e) {
            LOG.error(e);
        }
        return result;
    }

    @Override
    public List<Transaction> getTxList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return null;
        }
        List<Transaction> txList = new ArrayList<>();
        //根据交易hash批量查询交易数据
        List<byte[]> list = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, hashList);
        if (list != null) {
            for (byte[] txBytes : list) {
                Transaction tx = new Transaction();
                try {
                    TransactionUnconfirmedPO txPO = new TransactionUnconfirmedPO();
                    txPO.parse(txBytes, 0);
                    tx = txPO.getTx();
                } catch (NulsException e) {
                    LOG.error(e);
                }
                txList.add(tx);
            }
        }
        return txList;
    }

    @Override
    public boolean removeTxList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return true;
        }

        try {
            //delete transaction
            return RocksDBService.deleteKeys(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, hashList);
        } catch (Exception e) {
            LOG.error(e);
        }
        return false;
    }

    @Override
    public List<byte[]> getAllTxkeyList(int chainId) {
        return RocksDBService.keyList(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId);
    }

    @Override
    public List<TransactionUnconfirmedPO> getTransactionUnconfirmedPOList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return null;
        }
        List<TransactionUnconfirmedPO> txPOList = new ArrayList<>();
        //根据交易hash批量查询交易数据
        List<byte[]> list = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, hashList);
        if (list != null) {
            for (byte[] txBytes : list) {
                TransactionUnconfirmedPO txPO = new TransactionUnconfirmedPO();
                try {
                    txPO.parse(txBytes, 0);
                } catch (NulsException e) {
                    LOG.error(e);
                }
                txPOList.add(txPO);
            }
        }
        return txPOList;
    }

    @Override
    public List<byte[]> getExistKeys(int chainId, List<byte[]> hashList) {
        if (hashList == null || hashList.size() == 0) {
            return null;
        }
        //根据交易hash批量查询交易数据
        return RocksDBService.multiGetKeyList(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId, hashList);
    }

    @Override
    public List<String> getExistKeysStr(int chainId, List<byte[]> hashList) {

        List<String> list = new ArrayList<>();
        for(byte[] hash : getExistKeys(chainId, hashList)){
            list.add(HexUtil.encode(hash));
        }
        return list;
    }
}
