package io.nuls.ledger.service.v1;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.po.LedgerAsset;
import io.nuls.ledger.model.tx.txdata.TxLedgerAsset;
import io.nuls.ledger.service.AssetRegMngService;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("AssetRegTransferProcessorV1")
public class AssetRegTransferProcessor implements TransactionProcessor {
    @Autowired
    private AssetRegMngService assetRegMngService;


    @Override
    public int getType() {
        return TxType.LEDGER_ASSET_REG_TRANSFER;
    }

    private LedgerAsset buildLedgerAssetByTx(Transaction tx, int chainId) throws NulsException {
        String txHash = tx.getHash().toHex();
        TxLedgerAsset txLedgerAsset = new TxLedgerAsset();
        txLedgerAsset.parse(tx.getTxData(), 0);
        byte[] stream = tx.getCoinData();
        CoinData coinData = new CoinData();
        coinData.parse(new NulsByteBuffer(stream));
        List<CoinTo> coinTos = coinData.getTo();
        List<CoinFrom> coinFroms = coinData.getFrom();
        byte[] fromAddress = null;
        BigInteger destroyAsset = coinTos.get(0).getAmount();
        fromAddress = coinFroms.get(0).getAddress();
        LedgerAsset asset = new LedgerAsset(txLedgerAsset, chainId, destroyAsset, txHash, tx.getTime(), fromAddress,LedgerConstant.COMMON_ASSET_TYPE);
        return asset;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        List<Transaction> errorList = new ArrayList<>();
        Map<String, Object> rtData = new HashMap<>(2);
        rtData.put("errorCode", "");
        rtData.put("txList", errorList);
        try {
            Map<String, Integer> assetMap = new HashMap<>();
            for (Transaction tx : txs) {
                String txHash = tx.getHash().toHex();
                TxLedgerAsset txLedgerAsset = new TxLedgerAsset();
                txLedgerAsset.parse(tx.getTxData(), 0);
                byte[] stream = tx.getCoinData();
                CoinData coinData = new CoinData();
                coinData.parse(new NulsByteBuffer(stream));
                List<CoinTo> coinTos = coinData.getTo();
                List<CoinFrom> coinFroms = coinData.getFrom();
                BigInteger destroyAsset = BigInteger.ZERO;
                byte[] toAddress = null;
                byte[] fromAddress = null;
                if (coinTos.size() == 1) {
                    destroyAsset = coinTos.get(0).getAmount();
                    toAddress = coinTos.get(0).getAddress();
                } else {
                    rtData.put("errorCode", LedgerErrorCode.TX_IS_WRONG);
                    errorList.add(tx);
                    continue;
                }
                if (coinFroms.size() == 1) {
                    fromAddress = coinFroms.get(0).getAddress();
                } else {
                    rtData.put("errorCode", LedgerErrorCode.TX_IS_WRONG);
                    errorList.add(tx);
                    continue;
                }
                ErrorCode errorCode = assetRegMngService.batchAssetRegValidator(txLedgerAsset, toAddress, destroyAsset, chainId);
                if (null == errorCode) {
                    LoggerUtil.COMMON_LOG.debug("txHash = {},reg batchValidate success!", txHash);
                } else {
                    rtData.put("errorCode", errorCode.getCode());
                    LoggerUtil.COMMON_LOG.error("txHash = {},reg batchValidate fail!", txHash);
                    errorList.add(tx);
                }
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            throw new RuntimeException(e);
        }
        return rtData;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        List<LedgerAsset> assets = new ArrayList<>();
        try {
            for (Transaction tx : txs) {
                LedgerAsset asset = buildLedgerAssetByTx(tx, chainId);
                assets.add(asset);
            }
            assetRegMngService.registerTxAssets(chainId, assets);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            //通知远程调用回滚
            return false;
        }
        return true;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        try {
            List<LedgerAsset> list = new ArrayList<>();
            for (Transaction tx : txs) {
                LedgerAsset ledgerAsset = buildLedgerAssetByTx(tx,chainId);
                list.add(ledgerAsset);
            }
            assetRegMngService.rollBackTxAssets(chainId, list);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
