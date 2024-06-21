package io.nuls.crosschain.utils.manager;

import io.nuls.base.data.BlockHeader;
import io.nuls.common.ConfigBean;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.base.service.ResetLocalVerifierService;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.CmdRegisterDto;
import io.nuls.crosschain.rpc.call.BlockCall;
import io.nuls.crosschain.rpc.call.SmartContractCall;
import io.nuls.crosschain.srorage.CtxStatusService;
import io.nuls.crosschain.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.utils.LoggerUtil;
import io.nuls.crosschain.utils.thread.handler.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chain management,Responsible for initializing each chain,working,start-up,Parameter maintenance, etc
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author tag
 * 2019/4/10
 */
@Component
public class ChainManager {
    @Autowired
    private NulsCoresConfig config;
    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;


    @Autowired
    CtxStatusService ctxStatusService;

    @Autowired
    private static ResetLocalVerifierService resetLocalVerifierService;

    /**
     * Chain cache
     * Chain cache
     */
    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * Caching registered cross chain chain information
     */
    private List<ChainInfo> registeredCrossChainList = new ArrayList<>();

    /**
     * Cache the latest block header of each chain
     * */
    private Map<Integer, BlockHeader> chainHeaderMap = new ConcurrentHashMap<>();

    private boolean crossNetUseAble = false;

    /**
     * initialization
     * Initialization chain
     */
    public void initChain() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            Log.info("Chain initialization failed！");
            return;
        }
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            ConfigBean configBean = entry.getValue();
            if (chainId == config.getMainChainId() && configBean.getAssetId() == config.getMainAssetId()) {
                config.setMainNet(true);
                chain.setMainChain(true);
            }
            chain.setConfig(configBean);
            /*
             * Initialize Chain Log Object
             * Initialization Chain Log Objects
             * */
            LoggerUtil.initLogger(chain);

            /*
            Initialize Chain Database Table
            Initialize linked database tables
            */
            initTable(chain);
            chainMap.put(chainId, chain);
        }

        if(!config.isMainNet()){
            RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
            if(registeredChainMessage != null){
                registeredCrossChainList = registeredChainMessage.getChainInfoList();
                crossNetUseAble = true;
            }else{
                ChainInfo mainChainInfo = new ChainInfo();
                mainChainInfo.setVerifierList(new HashSet<>(Arrays.asList(config.getVerifiers().split(NulsCrossChainConstant.VERIFIER_SPLIT))));
                mainChainInfo.setMaxSignatureCount(config.getMaxSignatureCount());
                mainChainInfo.setSignatureByzantineRatio(config.getMainByzantineRatio());
                mainChainInfo.setChainId(config.getMainChainId());
                registeredCrossChainList.add(mainChainInfo);
            }
        }
    }

    /**
     * Registering smart contract transactions
     * */
    public void registerContractTx(){
        for (Chain chain:chainMap.values()) {
            /*
             * Registering smart contract transactions
             * Chain Trading Registration
             * */
            int chainId = chain.getConfig().getChainId();
            List<CmdRegisterDto> cmdRegisterDtoList = new ArrayList<>();
            CmdRegisterDto tokenOutCrossChain = new CmdRegisterDto("cc_tokenOutCrossChain", 0, List.of("from", "to", "value"), 1);
            cmdRegisterDtoList.add(tokenOutCrossChain);
            SmartContractCall.registerContractTx(chainId, cmdRegisterDtoList);
        }
    }

    /**
     * Load chain cache data and start the chain
     * Load the chain to cache data and start the chain
     */
    @SuppressWarnings("unchecked")
    public void runChain() {
        for (Chain chain : chainMap.values()) {
            //Load local validator list
            LocalVerifierManager.loadLocalVerifier(chain);
            //Initialize block module synchronization status
            chain.setSyncStatus(BlockCall.getBlockStatus(chain));
            chain.getThreadPool().execute(new HashMessageHandler(chain));
            chain.getThreadPool().execute(new OtherCtxMessageHandler(chain));
            chain.getThreadPool().execute(new GetCtxStateHandler(chain));
            chain.getThreadPool().execute(new SignMessageByzantineHandler(chain));
            chain.getThreadPool().execute(new SaveCtxFullSignHandler(chain,ctxStatusService,resetLocalVerifierService));
            int syncStatus = BlockCall.getBlockStatus(chain);
            chain.getLogger().info("The current status of the node is:{}",syncStatus);
            chain.setSyncStatus(syncStatus);
        }
        if(config.isMainNet()){
            crossNetUseAble = true;
        }
    }

    /**
     * Read configuration file to create and initialize chain
     * Read the configuration file to create and initialize the chain
     */
    private Map<Integer, ConfigBean> configChain() {
        try {
            /*
            Read database chain information configuration
            Read database chain information configuration
             */
            Map<Integer, ConfigBean> configMap = new HashMap<>();
            /*
            If the system is running for the first time and there is no storage chain information in the local database, it is necessary to read the main chain configuration information from the configuration file
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if (configMap == null || configMap.size() == 0) {
                ConfigBean configBean = config;
                if(config.getVerifiers() != null){
                    configBean.setVerifierSet(new HashSet<>(Arrays.asList(config.getVerifiers().split(NulsCrossChainConstant.VERIFIER_SPLIT))));
                }else{
                    configBean.setVerifierSet(new HashSet<>());
                }
                configMap.put(configBean.getChainId(), configBean);
            }
            return configMap;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    /**
     * Initialize Chain Related Tables
     * Initialization chain correlation table
     *
     * @param chain chain info
     */
    private void initTable(Chain chain) {
        int chainId = chain.getConfig().getChainId();
        try {
            /*
            Newly created cross chain transactions,Used to save locally created and verified cross chain transactions
            New Cross-Chain Transactions
            key:Transactions under this chain agreementHash
            value:Cross chain transactions and status of this chain protocol
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CTX_STATUS + chainId);

            /*
            Save verified cross chain transactions
            key:Cross chain transactions under this chain protocolhash
            value：Main network protocol cross chain transactions
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONVERT_CTX + chainId);

            /*
            Packaged Cross Chain Transactions
            New Cross-Chain Transactions
            key:Main network protocol cross chain transactionshash
            value:This Chain ProtocolHASH
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONVERT_HASH_CTX + chainId);

            /*
            Cross chain transactions completed by Byzantium
            New Cross-Chain Transactions
            key:Main network protocol cross chain transactionshash
            value:Main network protocol cross chain transactions(Initiate main network protocol signature for chain signature)
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_OTHER_COMMITED_CTX + chainId);

            /*
            High level trading to be broadcasted
            Processing completed cross-chain transactions (broadCasted to other chains)
            key:height
            value:List<LocalHash>
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_SEND_HEIGHT + chainId);

            /*
            Processing completed cross chain transactions（Broadcasted to other chains）
            Processing completed cross-chain transactions (broadCasted to other chains)
            key:height
            value:List<LocalHash>
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_SENDED_HEIGHT + chainId);

            /*
            Save and process successful cross chain transaction records
            Keep records of successful cross-chain transactions processed
            key：Cross chain transactionsHash
            value:Whether the processing was successful or not
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CTX_STATE+ chainId);

            /*
            Verifier change message for broadcast failure
            Keep records of successful cross-chain transactions processed
            key:height
            value:List<chainId>
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_BROAD_FAILED+ chainId);

            /*
            Verifier change message for broadcast failure
            Keep records of successful cross-chain transactions processed
            key:height
            value:List<chainId>
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CROSS_CHANGE_FAILED+ chainId);

            /*
            After resetting the validator list of this chain for transactions, the list of validators before the change will be stored in this table
             */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_OLD_LOCAL_VERIFIER + chainId);

        } catch (Exception e) {
            LoggerUtil.commonLog.error(e.getMessage());
        }
    }

    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }

    public List<ChainInfo> getRegisteredCrossChainList() {
        return registeredCrossChainList;
    }

    public void setRegisteredCrossChainList(List<ChainInfo> registeredCrossChainList) {
        this.registeredCrossChainList = registeredCrossChainList;
    }

    public boolean isCrossNetUseAble() {
        return crossNetUseAble;
    }

    public void setCrossNetUseAble(boolean crossNetUseAble) {
        this.crossNetUseAble = crossNetUseAble;
    }

    public Map<Integer, BlockHeader> getChainHeaderMap() {
        return chainHeaderMap;
    }

    public void setChainHeaderMap(Map<Integer, BlockHeader> chainHeaderMap) {
        this.chainHeaderMap = chainHeaderMap;
    }

    public ChainInfo getChainInfo(int fromChainId){
        for (ChainInfo chainInfo:registeredCrossChainList) {
            if(chainInfo.getChainId() == fromChainId){
                return chainInfo;
            }
        }
        return null;
    }
    public List<Map<String,Object>> getPrefixList(){
        List<Map<String,Object>> chainPrefixList = new ArrayList<>();
        for (ChainInfo chainInfo:registeredCrossChainList) {
            Map<String,Object> prefixMap = new HashMap<>(2);
            prefixMap.put("chainId", chainInfo.getChainId());
            prefixMap.put("addressPrefix", chainInfo.getAddressPrefix());
            chainPrefixList.add(prefixMap);
        }
        return chainPrefixList;
    }
}
