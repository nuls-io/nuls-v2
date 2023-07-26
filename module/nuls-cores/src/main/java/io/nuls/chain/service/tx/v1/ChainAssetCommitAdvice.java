package io.nuls.chain.service.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.CommonAdvice;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.po.BlockHeight;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.List;

@Component
public class ChainAssetCommitAdvice implements CommonAdvice {
    @Autowired
    private CacheDataService cacheDataService;
    @Autowired
    private RpcService rpcService;
    @Override
    public void begin(int chainId, List<Transaction> txList, BlockHeader blockHeader) {
        try {
            long commitHeight = blockHeader.getHeight();
            /*begin bak datas*/
            BlockHeight dbHeight = cacheDataService.getBlockHeight(chainId);
            cacheDataService.bakBlockTxs(chainId, commitHeight, txList, false);
            /*end bak datas*/
            /*begin bak height*/
            cacheDataService.beginBakBlockHeight(chainId, commitHeight);
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void end(int chainId, List<Transaction> txList, BlockHeader blockHeader) {
        try {
            long commitHeight = blockHeader.getHeight();
            /*begin bak height*/
            cacheDataService.endBakBlockHeight(chainId, commitHeight);
            /*end bak height*/
            rpcService.crossChainRegisterChange(CmRuntimeInfo.getMainIntChainId());
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            throw new RuntimeException(e);
        }

    }
}
