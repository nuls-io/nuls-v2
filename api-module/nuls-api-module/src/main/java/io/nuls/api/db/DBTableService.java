package io.nuls.api.db;

import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import io.nuls.api.ApiContext;
import io.nuls.api.constant.MongoTableConstant;
import io.nuls.api.model.po.db.AssetInfo;
import io.nuls.api.model.po.db.ChainInfo;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.bson.Document;

import java.util.List;

@Component
public class DBTableService {

    @Autowired
    private MongoDBService mongoDBService;
    @Autowired
    private ChainService chainService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountLedgerService ledgerService;
    @Autowired
    private AliasService aliasService;
    @Autowired
    private AgentService agentService;

    public List<ChainInfo> getChainList() {
        return chainService.getChainInfoList();
    }

    public void initCache() {
        chainService.initCache();
        accountService.initCache();
        ledgerService.initCache();
        aliasService.initCache();
        agentService.initCache();

    }

    public void addDefaultChain() {
        addChain(ApiContext.defaultChainId, ApiContext.defaultAssetId, "NULS");
    }

    public void addChain(int chainId, int defaultAssetId, String symbol) {
        initTables(chainId);
        initTablesIndex(chainId);
        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setChainId(chainId);
        AssetInfo assetInfo = new AssetInfo(chainId, defaultAssetId, symbol);
        chainInfo.setDefaultAsset(assetInfo);
        chainService.addChainInfo(chainInfo);

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
        mongoDBService.createCollection(MongoTableConstant.TX_RELATION_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.TX_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.PUNISH_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.ROUND_TABLE + chainId);
        mongoDBService.createCollection(MongoTableConstant.ROUND_ITEM_TABLE + chainId);
    }

    private void initTablesIndex(int chainId) {
        //交易关系表
        mongoDBService.createIndex(MongoTableConstant.TX_RELATION_TABLE + chainId, Indexes.ascending("address"));
        mongoDBService.createIndex(MongoTableConstant.TX_RELATION_TABLE + chainId, Indexes.ascending("address", "type"));
        mongoDBService.createIndex(MongoTableConstant.TX_RELATION_TABLE + chainId, Indexes.ascending("txHash"));
        mongoDBService.createIndex(MongoTableConstant.TX_RELATION_TABLE + chainId, Indexes.descending("height", "createTime"));
        //账户信息表
        mongoDBService.createIndex(MongoTableConstant.ACCOUNT_TABLE + chainId, Indexes.descending("totalBalance"));
        //交易表
        mongoDBService.createIndex(MongoTableConstant.TX_TABLE + chainId, Indexes.descending("height", "createTime"));
        //block 表
        mongoDBService.createIndex(MongoTableConstant.BLOCK_HEADER_TABLE + chainId, Indexes.ascending("hash"));
    }

}
