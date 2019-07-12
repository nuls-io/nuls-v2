package io.nuls.chain.service.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.rpc.call.RpcService;
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

@Component("DestroyChainTxProcessorV1")
public class DestroyChainTransferProcessor implements TransactionProcessor {
    @Autowired
    private ValidateService validateService;
    @Autowired
    private CacheDataService cacheDataService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private ChainService chainService;
    @Autowired
    private RpcService rpcService;
    @Autowired
    CmTransferService cmTransferService;

    @Override
    public int getType() {
        return TxType.DESTROY_CHAIN_AND_ASSET;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        List<Transaction> errorList = new ArrayList<>();
        Map<String, Object> rtData = new HashMap<>(2);
        rtData.put("errorCode", "");
        rtData.put("txList", errorList);
        try {
            //1获取交易类型
            //2进入不同验证器里处理
            //3封装失败交易返回
            Map<String, Integer> chainMap = new HashMap<>();
            Map<String, Integer> assetMap = new HashMap<>();
            BlockChain blockChain = null;
            Asset asset = null;
            ChainEventResult chainEventResult = ChainEventResult.getResultSuccess();
            for (Transaction tx : txs) {
                String txHash = tx.getHash().toHex();
                blockChain = TxUtil.buildChainWithTxData(tx, true);
                chainEventResult = validateService.chainDisableValidator(blockChain);
                if (chainEventResult.isSuccess()) {
                    LoggerUtil.logger().debug("txHash = {},chainId={} destroy batchValidate success!", txHash, blockChain.getChainId());
                } else {
                    rtData.put("errorCode", chainEventResult.getErrorCode().getCode());
                    LoggerUtil.logger().error("txHash = {},chainId={} destroy batchValidate fail!", txHash, blockChain.getChainId());
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
        BlockChain blockChain = null;
        List<Map<String, Object>> chainAssetIds = new ArrayList<>();
        try {
            for (Transaction tx : txs) {
                blockChain = TxUtil.buildChainWithTxData(tx, true);
                chainService.destroyBlockChain(blockChain);
                Map<String, Object> chainAssetId = new HashMap<>(2);
                chainAssetId.put("chainId", blockChain.getChainId());
                chainAssetId.put("assetId", blockChain.getDelAssetId());
                chainAssetIds.add(chainAssetId);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            //通知远程调用回滚
            try {
                chainService.rpcBlockChainRollback(txs);
                //进行回滚
                cacheDataService.rollBlockTxs(chainId, commitHeight);
            } catch (Exception e1) {
                LoggerUtil.logger().error(e);
                throw new RuntimeException(e);
            }
            return false;
        }
        rpcService.cancelCrossChain(chainAssetIds);
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
