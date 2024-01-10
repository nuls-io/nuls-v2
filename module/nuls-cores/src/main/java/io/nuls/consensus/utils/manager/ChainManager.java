package io.nuls.consensus.utils.manager;

import io.nuls.base.protocol.ProtocolLoader;
import io.nuls.common.CommonContext;
import io.nuls.common.ConfigBean;
import io.nuls.common.NulsCoresConfig;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.economic.base.service.EconomicService;
import io.nuls.consensus.economic.nuls.constant.ParamConstant;
import io.nuls.consensus.economic.nuls.model.bo.ConsensusConfigInfo;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.dto.CmdRegisterDto;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.consensus.utils.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.service.RocksDBService;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chain management,Responsible for initializing each chain,working,start-up,Parameter maintenance, etc
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author tag
 * 2018/12/4
 */
@Component
public class ChainManager {
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private DepositManager depositManager;
    @Autowired
    private PunishManager punishManager;
    @Autowired
    private RoundManager roundManager;
    @Autowired
    private SchedulerManager schedulerManager;
    @Autowired
    private NulsCoresConfig config;
    @Autowired
    private EconomicService economicService;

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * initialization
     * Initialization chain
     * */
    public void initChain() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            Log.info("Chain initialization failed！");
            return;
        }
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()){
            Chain chain = new Chain();
            int chainId = entry.getKey();
            ConfigBean configBean = entry.getValue();
            chain.setConfig(configBean);
            /*
             * Initialize Chain Log Object
             * Initialization Chain Log Objects
             * */
            initLogger(chain);
            /*
            Initialize Chain Database Table
            Initialize linked database tables
            */
            initTable(chain);
            chainMap.put(chainId, chain);
            //ProtocolLoader.load(chainId);
            Map<String,Object> param = new HashMap<>(4);
            double deflationRatio = DoubleUtils.sub(ConsensusConstant.VALUE_OF_ONE_HUNDRED, config.getDeflationRatio());
            param.put(ParamConstant.CONSENUS_CONFIG, new ConsensusConfigInfo(chainId,configBean.getAssetId(),configBean.getPackingInterval(),
                    configBean.getInflationAmount(),configBean.getTotalInflationAmount(),configBean.getInitTime(),deflationRatio,configBean.getDeflationTimeInterval(),configBean.getAwardAssetId()));
            economicService.registerConfig(param);
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
            CmdRegisterDto createAgentDto = new CmdRegisterDto("cs_createContractAgent", 0, List.of("packingAddress", "deposit", "commissionRate"), 1);
            CmdRegisterDto depositDto = new CmdRegisterDto("cs_contractDeposit", 0, List.of("agentHash", "deposit"), 1);
            CmdRegisterDto stopAgentDto = new CmdRegisterDto("cs_stopContractAgent", 0, List.of(), 1);
            CmdRegisterDto cancelDepositDto = new CmdRegisterDto("cs_contractWithdraw", 0, List.of("joinAgentHash"), 1);
            CmdRegisterDto searchAgentInfo = new CmdRegisterDto("cs_getContractAgentInfo", 1, List.of("agentHash"), 1);
            CmdRegisterDto searchDepositInfo = new CmdRegisterDto("cs_getContractDepositInfo", 1, List.of("joinAgentHash"), 1);
            cmdRegisterDtoList.add(cancelDepositDto);
            cmdRegisterDtoList.add(createAgentDto);
            cmdRegisterDtoList.add(stopAgentDto);
            cmdRegisterDtoList.add(depositDto);
            cmdRegisterDtoList.add(searchAgentInfo);
            cmdRegisterDtoList.add(searchDepositInfo);
            CallMethodUtils.registerContractTx(chainId, cmdRegisterDtoList);
        }
    }

    /**
     * Load chain cache data and start the chain
     * Load the chain to cache data and start the chain
     * */
    public void runChain(){
        for (Chain chain:chainMap.values()) {
            /*
            Load chain cache data
            Load chain caching entity
            */
            initCache(chain);

            /*
            Create and initiate in chain tasks
            Create and start in-chain tasks
            */
            schedulerManager.createChainScheduler(chain);
        }
    }

    /**
     * Stop a chain
     * stop a chain
     *
     * @param chainId chainID/chain id
     */
    public void stopChain(int chainId) {

    }

    /**
     * Delete a chain
     * delete a chain
     */
    public void deleteChain(int chainId) {

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
            Map<Integer, ConfigBean> configMap = CommonContext.CONFIG_BEAN_MAP;
            /*
            If the system is running for the first time and there is no storage chain information in the local database, it is necessary to read the main chain configuration information from the configuration file
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if (configMap == null || configMap.size() == 0) {
                ConfigBean configBean = config;
                configBean.setBlockReward(configBean.getInflationAmount().divide(ConsensusConstant.YEAR_MILLISECOND.divide(BigInteger.valueOf(configBean.getPackingInterval()))));
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
            Create consensus node table
            Create consensus node tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_AGENT + chainId);

            /*
            Create consensus information table
            Create consensus information tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT + chainId);

            /*
            Create a red and yellow card information table
            Creating Red and Yellow Card Information Table
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH + chainId);
            /*
            Create a low-level random number table
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_RANDOM_SEEDS + chainId);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                chain.getLogger().error(e.getMessage());
            } else {
                chain.getLogger().error(e.getMessage());
            }
        }
    }

    private void initLogger(Chain chain) {
        /*
         * Consensus module log file object creation,If a chain has multiple types of log files, you can add them here
         * Creation of Log File Object in Consensus Module,If there are multiple log files in a chain, you can add them here
         * */
        LoggerUtil.initLogger(chain);
    }

    /**
     * Initialize chain cache data
     * staypocUnder the consensus mechanism, due to the existence of round information, node information, and red and yellow card information for node punishment,
     * Therefore, it is necessary to cache relevant data during initialization to calculate the latest round information, as well as the credit values of each node
     * Initialize chain caching entity
     *
     * @param chain chain info
     */
    private void initCache(Chain chain) {
        try {
            CallMethodUtils.loadBlockHeader(chain);
            agentManager.loadAgents(chain);
            depositManager.loadDeposits(chain);
            punishManager.loadPunishes(chain);
            if(chain.getBlockHeaderList().size()>1){
                roundManager.initRound(chain);
            }
            chain.setCacheLoaded(true);
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
    }

    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }

}
