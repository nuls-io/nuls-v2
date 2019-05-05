package io.nuls.api.db.mongo;

import com.mongodb.client.model.Indexes;
import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.db.DBTableService;
import io.nuls.api.model.po.db.AssetInfo;
import io.nuls.api.model.po.db.ChainInfo;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class MongoDBTableServiceImpl implements DBTableService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private MongoChainServiceImpl mongoChainServiceImpl;
    @Autowired
    private MongoAccountServiceImpl mongoAccountServiceImpl;
    @Autowired
    private MongoAccountLedgerServiceImpl ledgerService;
    @Autowired
    private MongoAliasServiceImpl mongoAliasServiceImpl;
    @Autowired
    private MongoAgentServiceImpl mongoAgentServiceImpl;

    public List<ChainInfo> getChainList() {
        return mongoChainServiceImpl.getChainInfoList();
    }

    public void initCache() {
        mongoChainServiceImpl.initCache();
        mongoAccountServiceImpl.initCache();
        ledgerService.initCache();
        mongoAliasServiceImpl.initCache();
        mongoAgentServiceImpl.initCache();
    }

    public void addDefaultChain() {
        addChain(ApiContext.defaultChainId, ApiContext.defaultAssetId, "NULS");
    }

    public void addChain(int chainId, int defaultAssetId, String symbol) {
        Result<Map> result = WalletRpcHandler.getConsensusConfig(chainId);
        if (result.isFailed()) {
            throw new RuntimeException("find consensus config error");
        }
        Map map = result.getData();
        List<String> seedNodeList = (List<String>) map.get("seedNodeList");
        String inflationAmount = (String) map.get("inflationAmount");

        initTables(chainId);
        initTablesIndex(chainId);

        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setChainId(chainId);
        AssetInfo assetInfo = new AssetInfo(chainId, defaultAssetId, symbol);
        chainInfo.setDefaultAsset(assetInfo);
        chainInfo.getAssets().add(assetInfo);
        for (String address : seedNodeList) {
            chainInfo.getSeeds().add(address);
        }
        chainInfo.setInflationCoins(new BigInteger(inflationAmount));
        mongoChainServiceImpl.addChainInfo(chainInfo);
    }

    public void initTables(int chainId) {
        //mongoDBService.createCollection(MongoTableConstant.CHAIN_INFO_TABLE + chainId);

        mongoDBService.createCollection(MongoTableConstant.SYNC_INFO_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.BLOCK_HEADER_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.ACCOUNT_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.ACCOUNT_LEDGER_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.AGENT_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.ALIAS_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.DEPOSIT_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.TX_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.COINDATA_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.PUNISH_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.ROUND_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.ROUND_ITEM_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.ACCOUNT_TOKEN_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.CONTRACT_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.CONTRACT_TX_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.TOKEN_TRANSFER_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.CONTRACT_RESULT_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.STATISTICAL_TABLE + chainId);

        for (int i = 0; i < 32; i++) {
            mongoDBService.createCollection(MongoTableConstant.TX_RELATION_TABLE + chainId + "_" + i);
        }
    }

    private void initTablesIndex(int chainId) {
        //交易关系表
        for (int i = 0; i < 32; i++) {
            mongoDBService.createIndex(MongoTableConstant.TX_RELATION_TABLE + chainId + "_" + i, Indexes.ascending("address"));
            mongoDBService.createIndex(MongoTableConstant.TX_RELATION_TABLE + chainId + "_" + i, Indexes.ascending("address", "type"));
            mongoDBService.createIndex(MongoTableConstant.TX_RELATION_TABLE + chainId + "_" + i, Indexes.ascending("txHash"));
            mongoDBService.createIndex(MongoTableConstant.TX_RELATION_TABLE + chainId + "_" + i, Indexes.descending("createTime"));
        }
        //账户信息表
        mongoDBService.createIndex(MongoTableConstant.ACCOUNT_TABLE + chainId, Indexes.descending("totalBalance"));
        //交易表
        mongoDBService.createIndex(MongoTableConstant.TX_TABLE + chainId, Indexes.descending("createTime"));
        //block 表
        mongoDBService.createIndex(MongoTableConstant.BLOCK_HEADER_TABLE + chainId, Indexes.ascending("hash"));
    }

}
