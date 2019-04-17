package io.nuls.transaction.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.model.StringUtils;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.po.TransactionsPO;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.Log;

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
        if (tx == null) {
            return false;
        }
        TransactionsPO txPO = new TransactionsPO(tx);
        //设置入库保存时间
        txPO.setCreateTime(System.currentTimeMillis());
        byte[] txHashBytes;
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.put(TxDBConstant.DB_TRANSACTION_CACHE + chainId, txHashBytes, txPO.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }

    @Override
    public boolean putTxList(int chainId, List<Transaction> txList) {
        if (null == txList || txList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> txPoMap = new HashMap<>();
        try {
            for (Transaction tx : txList) {
                TransactionsPO txPO = new TransactionsPO(tx);
                //设置入库保存时间
                txPO.setCreateTime(System.currentTimeMillis());
                //序列化对象为byte数组存储
                txPoMap.put(tx.getHash().serialize(), txPO.serialize());
            }
            return RocksDBService.batchPut(TxDBConstant.DB_TRANSACTION_CACHE + chainId, txPoMap);
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
        try {
            return getTx(chainId, hash.serialize());
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
    }

    @Override
    public boolean isExists(int chainId, NulsDigestData hash) {
        try {
            byte[] txBytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hash.serialize());
            if (null != txBytes && txBytes.length > 0) {
                return true;
            }
            return false;
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
    }

    @Override
    public Transaction getTx(int chainId, String hash) {
        if (StringUtils.isBlank(hash)) {
            return null;
        }
        return getTx(chainId, HexUtil.decode(hash));
    }

    private Transaction getTx(int chainId, byte[] hashSerialize) {
        byte[] txBytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hashSerialize);
        Transaction tx = null;
        if (null != txBytes) {
            try {
                TransactionsPO txPO = new TransactionsPO();
                txPO.parse(new NulsByteBuffer(txBytes, 0));
                tx = txPO.toTransaction();
            } catch (Exception e) {
                Log.error(e);
                return null;
            }
        }
        return tx;
    }


    @Override
    public boolean removeTx(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.delete(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hash.serialize());
        } catch (Exception e) {
            Log.error(e);
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
        List<byte[]> list = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hashList);
        if (list != null) {
            for (byte[] txBytes : list) {
                Transaction tx = new Transaction();
                try {
                    TransactionsPO txPO = new TransactionsPO();
                    txPO.parse(txBytes, 0);
                    tx = txPO.toTransaction();
                } catch (NulsException e) {
                    Log.error(e);
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
            return false;
        }

        try {
            //delete transaction
            return RocksDBService.deleteKeys(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hashList);
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<TransactionsPO> getAllTxPOList(int chainId) {
        List<TransactionsPO> txList = new ArrayList<>();
        //根据交易hash批量查询交易数据
        List<byte[]> list = RocksDBService.valueList(TxDBConstant.DB_TRANSACTION_CACHE + chainId);
        if (list != null) {
            for (byte[] txBytes : list) {
                TransactionsPO txPO = new TransactionsPO();
                try {
                    txPO.parse(txBytes, 0);
                } catch (NulsException e) {
                    Log.error(e);
                }
                txList.add(txPO);
            }
        }
        return txList;
    }
}
