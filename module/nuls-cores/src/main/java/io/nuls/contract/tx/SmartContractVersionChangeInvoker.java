package io.nuls.contract.tx;

import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.ContractTokenAssetsInfo;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.rpc.call.TransactionCall;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.VMFactory;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-12-06
 */
public class SmartContractVersionChangeInvoker implements VersionChangeInvoker {

    private static SmartContractVersionChangeInvoker invoker = new SmartContractVersionChangeInvoker();

    private SmartContractVersionChangeInvoker() {}

    public static SmartContractVersionChangeInvoker instance() {
        return invoker;
    }

    private boolean isloadV8 = false;
    private boolean isloadV14 = false;
    private boolean isloadV15 = false;

    /**
     *
     * 协议升级后，向账本模块请求nrc20-token资产列表，缓存到模块内存中。
     *
     * @param currentChainId
     */
    @Override
    public void process(int currentChainId) {
        ChainManager.chainHandle(currentChainId);
        Short currentVersion = ProtocolGroupManager.getCurrentVersion(currentChainId);
        Log.info("触发协议升级，chainId: [{}], 版本为: [{}]", currentChainId, currentVersion);
        ChainManager chainManager = SpringLiteContext.getBean(ChainManager.class);
        if (currentVersion >= ContractContext.PROTOCOL_15) {
            this.loadV15(chainManager.getChainMap().get(currentChainId), currentVersion);
        } else if (currentVersion >= ContractContext.PROTOCOL_14) {
            this.loadV14(chainManager.getChainMap().get(currentChainId), currentVersion);
        } else if (currentVersion >= ContractContext.UPDATE_VERSION_CONTRACT_ASSET) {
            this.loadV8(chainManager.getChainMap().get(currentChainId), currentVersion);
        }
        // 向交易模块设置智能合约生成交易类型
        setContractGenerateTxTypes(currentChainId, currentVersion);
        // 缓存token注册资产的资产ID和token合约地址
        Map<Integer, Chain> chainMap = chainManager.getChainMap();
        for (Chain chain : chainMap.values()) {
            int chainId = chain.getChainId();
            Short version = ProtocolGroupManager.getCurrentVersion(chainId);
            if(version < ContractContext.UPDATE_VERSION_V250) {
                continue;
            }
            Log.info("协议升级成功，向账本模块获取token资产列表，chainId: [{}], 版本为: [{}]", chainId, version);
            List<Map> regTokenList;
            try {
                regTokenList = LedgerCall.getRegTokenList(chainId);
                if(regTokenList != null && !regTokenList.isEmpty()) {
                    Map<String, ContractTokenAssetsInfo> tokenAssetsInfoMap = chain.getTokenAssetsInfoMap();
                    Map<String, String> tokenAssetsContractAddressInfoMap = chain.getTokenAssetsContractAddressInfoMap();
                    regTokenList.stream().forEach(map -> {
                        int assetId = Integer.parseInt(map.get("assetId").toString());
                        String tokenContractAddress = map.get("assetOwnerAddress").toString();
                        tokenAssetsInfoMap.put(tokenContractAddress, new ContractTokenAssetsInfo(chainId, assetId));
                        tokenAssetsContractAddressInfoMap.put(chainId + "-" + assetId, tokenContractAddress);
                    });
                }
            } catch (NulsException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setContractGenerateTxTypes(int currentChainId, Short currentVersion) {
        List<Integer> list = List.of(
                TxType.CONTRACT_TRANSFER,
                TxType.CONTRACT_CREATE_AGENT,
                TxType.CONTRACT_DEPOSIT,
                TxType.CONTRACT_CANCEL_DEPOSIT,
                TxType.CONTRACT_STOP_AGENT);
        List<Integer> resultList = new ArrayList<>();
        resultList.addAll(list);
        if(currentVersion >= ContractContext.UPDATE_VERSION_V250) {
            resultList.add(TxType.CONTRACT_TOKEN_CROSS_TRANSFER);
        }
        try {
            TransactionCall.setContractGenerateTxTypes(currentChainId, resultList);
        } catch (NulsException e) {
            Log.warn("获取智能合约生成交易类型异常", e);
        }
    }

    private void loadV8(Chain chain, int currentVersion) {
        if (isloadV8) {
            return;
        }
        chain.clearOldBatchInfo();
        Log.info("版本[{}]协议升级成功，重新初始化智能合约VM", currentVersion);
        VMFactory.reInitVM_v8();
        isloadV8 = true;
    }

    private void loadV14(Chain chain, int currentVersion) {
        if (isloadV14) {
            return;
        }
        chain.clearBatchInfo();
        Log.info("版本[{}]协议升级成功，重新初始化智能合约VM", currentVersion);
        VMFactory.reInitVM_v14();
        isloadV14 = true;
    }

    private void loadV15(Chain chain, int currentVersion) {
        if (isloadV15) {
            return;
        }
        chain.clearBatchInfo();
        Log.info("版本[{}]协议升级成功，重新初始化智能合约VM", currentVersion);
        VMFactory.reInitVM_v15();
        isloadV15 = true;
    }
}
