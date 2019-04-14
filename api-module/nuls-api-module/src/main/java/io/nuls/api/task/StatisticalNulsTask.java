package io.nuls.api.task;


import io.nuls.api.ApiContext;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.mongo.MongoAccountServiceImpl;
import io.nuls.api.db.mongo.MongoAgentServiceImpl;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.ContextInfo;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.math.BigInteger;

public class StatisticalNulsTask implements Runnable {

    private int chainId;

    private MongoAccountServiceImpl mongoAccountServiceImpl;

    private MongoAgentServiceImpl mongoAgentServiceImpl;


    public StatisticalNulsTask(int chainId) {
        this.chainId = chainId;
        mongoAccountServiceImpl = SpringLiteContext.getBean(MongoAccountServiceImpl.class);
        mongoAgentServiceImpl = SpringLiteContext.getBean(MongoAgentServiceImpl.class);
    }

    @Override
    public void run() {
        BigInteger totalCoin = mongoAccountServiceImpl.getAllAccountBalance(chainId);
        BigInteger consensusTotal = mongoAgentServiceImpl.getConsensusCoinTotal(chainId);

        ApiCache apiCache = CacheManager.getCache(chainId);
        ContextInfo contextInfo = apiCache.getContextInfo();
        //团队持有数量
        BigInteger teamNuls = mongoAccountServiceImpl.getAccountTotalBalance(chainId, ApiContext.TEAM_ADDRESS);
        //销毁数量
        BigInteger destroyNuls = mongoAccountServiceImpl.getAccountTotalBalance(chainId, ApiContext.DESTROY_ADDRESS);
        //商务持有数量
        BigInteger businessNuls = mongoAccountServiceImpl.getAccountTotalBalance(chainId, ApiContext.BUSINESS_ADDRESS);
        //社区持有数量
        BigInteger communityNuls = mongoAccountServiceImpl.getAccountTotalBalance(chainId, ApiContext.COMMUNITY_ADDRESS);

        contextInfo.setTotal(totalCoin);
        contextInfo.setConsensusTotal(consensusTotal);
        contextInfo.setCirculation(totalCoin.subtract(teamNuls).subtract(destroyNuls).subtract(businessNuls).subtract(communityNuls));
    }
}
