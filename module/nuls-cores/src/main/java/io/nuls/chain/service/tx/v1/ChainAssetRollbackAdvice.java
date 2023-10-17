package io.nuls.chain.service.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.CommonAdvice;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.List;
@Component
public class ChainAssetRollbackAdvice implements CommonAdvice {
    @Autowired
    RpcService rpcService;
    @Override
    public void begin(int chainId, List<Transaction> txList, BlockHeader blockHeader) {

    }
    @Override
    public void end(int chainId, List<Transaction> txList, BlockHeader blockHeader) {
        rpcService.crossChainRegisterChange(CmRuntimeInfo.getMainIntChainId());
    }
}
