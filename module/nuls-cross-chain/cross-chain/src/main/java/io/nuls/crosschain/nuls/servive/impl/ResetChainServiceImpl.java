package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CrossChainErrorCode;
import io.nuls.crosschain.base.service.ResetChainService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.utils.LoggerUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.util.*;

@Component
public class ResetChainServiceImpl implements ResetChainService {


    @Autowired
    private ChainManager chainManager;

    @Autowired
    private NulsCrossChainConfig config;

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {

        //todo 验证是种子节点签名的交易（是否需要多个种子节点签名）
        List<Transaction> errorList = new ArrayList<>();
        Map<String, Object> rtData = new HashMap<>(2);
        rtData.put("errorCode", "");
        rtData.put("txList", errorList);

        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            errorList.addAll(txs);
            chain.getLogger().error("Chain id is invaild");
            rtData.put("errorCode", NulsCrossChainErrorCode.DATA_ERROR.getCode());
            return rtData;
        }

        try {
            boolean success = true;
            ErrorCode errorCode = NulsCrossChainErrorCode.MUST_SEED_ADDRESS_SIGN;
            for (Transaction tx : txs) {
                String txHash = tx.getHash().toHex();
                //todo 验证是种子节点签名的交易
                tx.getTransactionSignature();
                Set<String> set = SignatureUtil.getAddressFromTX(tx, chainId);
                for (String txAddress : set) {
                    if (!config.getSeedNodeList().contains(txAddress)) {
                        success = false;
                        break;
                    }
                }


                if (success) {
                    chain.getLogger().debug("txHash = {},chainId={} reset validate success!", txHash, chainId);
                } else {
                    rtData.put("errorCode", errorCode);
                    chain.getLogger().error("txHash = {},chainId={} reset validate fail!", txHash, chainId);
                    errorList.add(tx);
                }
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
            throw new RuntimeException(e);
        }
        return rtData;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        //todo 解析chain信息，如果有则覆盖，如果没有就存储（缓存和数据库）
        //考虑要不要直接组装一个交易，发送到平行链(或者手动去平行链发交易)
        return false;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        //todo 反向
        return false;
    }
}
