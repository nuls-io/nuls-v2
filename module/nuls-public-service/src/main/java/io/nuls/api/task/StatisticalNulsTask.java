package io.nuls.api.task;


import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.AccountService;
import io.nuls.api.db.AgentService;
import io.nuls.api.db.ChainService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.AssetInfo;
import io.nuls.api.model.po.CoinContextInfo;
import io.nuls.api.model.po.DestroyInfo;
import io.nuls.api.model.po.SyncInfo;
import io.nuls.api.model.rpc.BalanceInfo;
import io.nuls.api.utils.AssetTool;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StatisticalNulsTask implements Runnable {

    private int chainId;

    private AccountService accountService;

    private AgentService agentService;

    private ChainService chainService;

    public StatisticalNulsTask(int chainId) {
        this.chainId = chainId;
        accountService = SpringLiteContext.getBean(AccountService.class);
        agentService = SpringLiteContext.getBean(AgentService.class);
        chainService = SpringLiteContext.getBean(ChainService.class);
    }

    @Override
    public void run() {
        try {

            BigInteger totalCoin = BigInteger.ZERO;
            SyncInfo syncInfo = chainService.getSyncInfo(chainId);
            if (syncInfo != null) {
                totalCoin = syncInfo.getTotalSupply();
            }
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

            for (String blackAddress : AddressTool.BLOCK_HOLE_ADDRESS_SET) {
                BigInteger blackNuls = accountService.getAccountTotalBalance(chainId, blackAddress);
                destroyNuls = destroyNuls.add(blackNuls);
            }
            // add by pierre at 2020-04-02 协议升级黑洞地址
            if (ApiContext.protocolVersion >= 5) {
                for (String blackAddress : AddressTool.BLOCK_HOLE_ADDRESS_SET_5) {
                    BigInteger blackNuls = accountService.getAccountTotalBalance(chainId, blackAddress);
                    destroyNuls = destroyNuls.add(blackNuls);
                }
            }
            // end code by pierre
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
            contextInfo.setDestroy(destroyNuls);

            BigInteger circulation = totalCoin.subtract(destroyNuls);
            if (balanceInfo != null) {
                circulation = circulation.subtract(balanceInfo.getTimeLock());
            }
            circulation = circulation.subtract(businessNuls);
            circulation = circulation.subtract(communityNuls);
            circulation = circulation.subtract(unmapped);
            contextInfo.setCirculation(circulation);

            setDestroyInfo(contextInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }


    private void setDestroyInfo(CoinContextInfo contextInfo) {
        List<DestroyInfo> list = new LinkedList<>();
        //销毁数量
        byte[] address = AddressTool.getAddress(ApiContext.blackHolePublicKey, chainId);
        String destroyAddress = AddressTool.getStringAddressByBytes(address);
        BigInteger destroyNuls = accountService.getAccountTotalBalance(chainId, destroyAddress);
        String reason = "account set alias destroy nuls";
        DestroyInfo destroyInfo = new DestroyInfo(destroyAddress, reason, AssetTool.toCoinString(destroyNuls));
        list.add(destroyInfo);

        reason = "stolen blacklist";
        for (String blackAddress : AddressTool.BLOCK_HOLE_ADDRESS_SET) {
            BigInteger blackNuls = accountService.getAccountTotalBalance(chainId, blackAddress);
            destroyNuls = destroyNuls.add(blackNuls);
            destroyInfo = new DestroyInfo(blackAddress, reason, AssetTool.toCoinString(blackNuls));
            list.add(destroyInfo);
        }
        // add by pierre at 2020-04-02 协议升级黑洞地址
        if (ApiContext.protocolVersion >= 5) {
            for (String blackAddress : AddressTool.BLOCK_HOLE_ADDRESS_SET_5) {
                BigInteger blackNuls = accountService.getAccountTotalBalance(chainId, blackAddress);
                destroyNuls = destroyNuls.add(blackNuls);
                destroyInfo = new DestroyInfo(blackAddress, reason, AssetTool.toCoinString(blackNuls));
                list.add(destroyInfo);
            }
        }
        // end code by pierre
        contextInfo.setDestroyInfoList(list);
    }
}
