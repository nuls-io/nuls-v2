package io.nuls.api.task;


import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.AccountService;
import io.nuls.api.db.AgentService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.AssetInfo;
import io.nuls.api.model.po.CoinContextInfo;
import io.nuls.api.model.rpc.BalanceInfo;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.StringUtils;

import java.math.BigInteger;

public class StatisticalNulsTask implements Runnable {

    private int chainId;

    private AccountService accountService;

    private AgentService agentService;


    public StatisticalNulsTask(int chainId) {
        this.chainId = chainId;
        accountService = SpringLiteContext.getBean(AccountService.class);
        agentService = SpringLiteContext.getBean(AgentService.class);
    }

    @Override
    public void run() {
        try {
            BigInteger totalCoin = accountService.getAllAccountBalance(chainId);
            BigInteger consensusTotal = agentService.getConsensusCoinTotal(chainId);

            ApiCache apiCache = CacheManager.getCache(chainId);
            CoinContextInfo contextInfo = apiCache.getCoinContextInfo();
            //团队持有数量
            BigInteger teamNuls = BigInteger.ZERO;
            BalanceInfo balanceInfo = null;
            if (!StringUtils.isBlank(ApiContext.TEAM_ADDRESS)) {
                teamNuls = accountService.getAccountTotalBalance(chainId, ApiContext.TEAM_ADDRESS);
                AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
                balanceInfo = WalletRpcHandler.getAccountBalance(chainId, ApiContext.TEAM_ADDRESS, defaultAsset.getChainId(), defaultAsset.getAssetId());
            }
            contextInfo.setTeam(teamNuls);
            //销毁数量
            byte[] address = AddressTool.getAddress(ApiContext.blackHolePublicKey, chainId);
            String destroyAddress = AddressTool.getStringAddressByBytes(address);
            BigInteger destroyNuls = accountService.getAccountTotalBalance(chainId, destroyAddress);
            //商务持有数量
            BigInteger businessNuls = BigInteger.ZERO;
            if (!StringUtils.isBlank(ApiContext.BUSINESS_ADDRESS)) {
                businessNuls = accountService.getAccountTotalBalance(chainId, ApiContext.BUSINESS_ADDRESS);
            }
            contextInfo.setBusiness(businessNuls);
            //社区持有数量
            BigInteger communityNuls = BigInteger.ZERO;
            if (!StringUtils.isBlank(ApiContext.COMMUNITY_ADDRESS)) {
                communityNuls = accountService.getAccountTotalBalance(chainId, ApiContext.COMMUNITY_ADDRESS);
            }
            contextInfo.setCommunity(communityNuls);

            BigInteger unmapped = BigInteger.ZERO;
            if (ApiContext.MAPPING_ADDRESS != null) {
                for (String mapAddress : ApiContext.MAPPING_ADDRESS) {
                    unmapped = unmapped.add(accountService.getAccountTotalBalance(chainId, mapAddress));
                }
            }
            contextInfo.setUnmapped(unmapped);
            contextInfo.setTotal(totalCoin);
            contextInfo.setConsensusTotal(consensusTotal);

            BigInteger circulation = totalCoin.subtract(destroyNuls);
            if (balanceInfo != null) {
                circulation = circulation.subtract(balanceInfo.getTimeLock());
            }
            circulation = circulation.subtract(businessNuls);
            circulation = circulation.subtract(communityNuls);
            circulation = circulation.subtract(unmapped);
            contextInfo.setCirculation(circulation);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }
}
