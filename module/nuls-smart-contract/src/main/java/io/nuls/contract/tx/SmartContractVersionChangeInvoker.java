package io.nuls.contract.tx;

import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.ContractTokenAssetsInfo;
import io.nuls.contract.rpc.call.LedgerCall;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;

import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-12-06
 */
public class SmartContractVersionChangeInvoker implements VersionChangeInvoker {

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
}
