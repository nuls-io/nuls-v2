package io.nuls.chain.service.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.service.*;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("DisableAssetTxProcessorV1")
public class DisableAssetTransferProcessor implements TransactionProcessor {
    @Autowired
    private ValidateService validateService;
    @Autowired
    private CacheDataService cacheDataService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private ChainService chainService;
    @Autowired
    CmTransferService cmTransferService;

    @Override
    public int getType() {
        return TxType.REMOVE_ASSET_FROM_CHAIN;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        List<Transaction> errorList = new ArrayList<>();
        Map<String, Object> rtData = new HashMap<>(2);
        rtData.put("errorCode", "");
        rtData.put("txList", errorList);
        try {
            Asset asset = null;
            ChainEventResult chainEventResult = ChainEventResult.getResultSuccess();
            for (Transaction tx : txs) {
                String txHash = tx.getHash().toHex();
                asset = TxUtil.buildAssetWithTxChain(tx);
                chainEventResult = validateService.assetDisableValidator(asset);
                if (chainEventResult.isSuccess()) {
                    LoggerUtil.logger().debug("txHash = {},assetKey={} disable batchValidate success!", txHash, CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
                } else {
                    rtData.put("errorCode", chainEventResult.getErrorCode().getCode());
                    LoggerUtil.logger().error("txHash = {},assetKey={} disable batchValidate fail!", txHash, CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
                    errorList.add(tx);
                }
            }

        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            throw new RuntimeException(e);
        }
        return rtData;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        long commitHeight = blockHeader.getHeight();
        Asset asset = null;
        try {
            for (Transaction tx : txs) {
                asset = TxUtil.buildAssetWithTxChain(tx);
                assetService.deleteAsset(asset);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            //通知远程调用回滚
            try {
                //进行回滚
                cacheDataService.rollBlockTxs(chainId, commitHeight);
            } catch (Exception e1) {
                LoggerUtil.logger().error(e);
                throw new RuntimeException(e);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        try {
            return cmTransferService.rollback(chainId, txs, blockHeader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
