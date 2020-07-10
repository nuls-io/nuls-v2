package io.nuls.api.db.mongo;

import com.mongodb.client.model.Indexes;
import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.constant.DBTableConstant;
import io.nuls.api.db.*;
import io.nuls.api.model.po.AssetInfo;
import io.nuls.api.model.po.ChainConfigInfo;
import io.nuls.api.model.po.ChainInfo;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.api.constant.DBTableConstant.TX_RELATION_SHARDING_COUNT;

@Component
public class MongoDBTableServiceImpl implements DBTableService {

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
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ContractService contractService;

    public List<ChainInfo> getChainList() {
        return chainService.getChainInfoList();
    }

    public void initCache() {
        chainService.initCache();
        accountService.initCache();
        ledgerService.initCache();
        aliasService.initCache();
        agentService.initCache();
//        transactionService.initCache();
        contractService.initCache();
    }

    public void addDefaultChainCache() {
        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setChainId(ApiContext.defaultChainId);
        chainInfo.setChainName(ApiContext.defaultChainName);
        AssetInfo assetInfo = new AssetInfo(ApiContext.defaultChainId, ApiContext.defaultAssetId, ApiContext.defaultSymbol, ApiContext.defaultDecimals);
        chainInfo.setDefaultAsset(assetInfo);
        chainInfo.getAssets().add(assetInfo);

        ChainConfigInfo configInfo = new ChainConfigInfo(chainInfo.getChainId(), ApiContext.agentChainId, ApiContext.agentAssetId, ApiContext.awardAssetId);
        addChainCache(chainInfo, configInfo);
    }

    public void addChainCache(ChainInfo chainInfo, ChainConfigInfo configInfo) {
        Result<Map> result = WalletRpcHandler.getConsensusConfig(chainInfo.getChainId());
        if (result.isFailed()) {
            throw new RuntimeException("find consensus config error");
        }
        Map map = result.getData();
        String seeds = (String) map.get("seedNodes");
        List<String> seedNodes = new ArrayList<>();
        for (String seed : seeds.split(",")) {
            seedNodes.add(seed);
        }

        initTables(chainInfo.getChainId());
        initTablesIndex(chainInfo.getChainId());

        chainInfo.setSeeds(seedNodes);
        chainService.addCacheChain(chainInfo, configInfo);
//        transactionService.addCache(chainInfo.getChainId());
    }

    public void initTables(int chainId) {
        //mongoDBService.createCollection(DBTableConstant.CHAIN_INFO_TABLE + chainId);

        mongoDBService.createCollection(DBTableConstant.SYNC_INFO_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.BLOCK_HEADER_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.BLOCK_HEX_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.ACCOUNT_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.AGENT_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.ALIAS_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.DEPOSIT_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.TX_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.COINDATA_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.PUNISH_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.ROUND_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.ROUND_ITEM_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.ACCOUNT_TOKEN_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.CONTRACT_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.CONTRACT_TX_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.TOKEN_TRANSFER_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.CONTRACT_RESULT_TABLE + chainId);
        mongoDBService.createCollection(DBTableConstant.STATISTICAL_TABLE + chainId);

        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            mongoDBService.createCollection(DBTableConstant.TX_RELATION_TABLE + chainId + "_" + i);
        }
    }

    private void initTablesIndex(int chainId) {
        //交易关系表
        for (int i = 0; i < TX_RELATION_SHARDING_COUNT; i++) {
            mongoDBService.createIndex(DBTableConstant.TX_RELATION_TABLE + chainId + "_" + i, Indexes.ascending("address"));
            mongoDBService.createIndex(DBTableConstant.TX_RELATION_TABLE + chainId + "_" + i, Indexes.ascending("address", "type"));
            mongoDBService.createIndex(DBTableConstant.TX_RELATION_TABLE + chainId + "_" + i, Indexes.ascending("txHash"));
            mongoDBService.createIndex(DBTableConstant.TX_RELATION_TABLE + chainId + "_" + i, Indexes.descending("createTime"));
        }
        //账户信息表
        mongoDBService.createIndex(DBTableConstant.ACCOUNT_TABLE + chainId, Indexes.descending("totalBalance"));
        mongoDBService.createIndex(DBTableConstant.ACCOUNT_LEDGER_TABLE + chainId, Indexes.descending("address"));
        //交易表
        mongoDBService.createIndex(DBTableConstant.TX_TABLE + chainId, Indexes.descending("height"));
        //block 表
        mongoDBService.createIndex(DBTableConstant.BLOCK_HEADER_TABLE + chainId, Indexes.ascending("hash"));
        //委托记录表
        mongoDBService.createIndex(DBTableConstant.DEPOSIT_TABLE + chainId, Indexes.descending("createTime"));
        //智能合约表
        mongoDBService.createIndex(DBTableConstant.CONTRACT_TABLE + chainId, Indexes.descending("createTime"));
        //账户token表
        mongoDBService.createIndex(DBTableConstant.ACCOUNT_TOKEN_TABLE + chainId, Indexes.descending("balance"));
        mongoDBService.createIndex(DBTableConstant.ACCOUNT_TOKEN_TABLE + chainId, Indexes.ascending("address"));
        mongoDBService.createIndex(DBTableConstant.ACCOUNT_TOKEN_TABLE + chainId, Indexes.ascending("contractAddress"));
        //token交易记录表
        mongoDBService.createIndex(DBTableConstant.TOKEN_TRANSFER_TABLE + chainId, Indexes.descending("time"));
        mongoDBService.createIndex(DBTableConstant.TOKEN_TRANSFER_TABLE + chainId, Indexes.descending("contractAddress","fromAddress"));
        mongoDBService.createIndex(DBTableConstant.TOKEN_TRANSFER_TABLE + chainId, Indexes.descending("contractAddress","toAddress"));
        //跨链交易表索引
        mongoDBService.createIndex(DBTableConstant.CROSS_TX_RELATION_TABLE + chainId, Indexes.ascending("address"));
    }

}
