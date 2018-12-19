package io.nuls.poc.utils.manager;

import io.nuls.base.data.Address;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.BlockRoundData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.config.ConfigItem;
import io.nuls.poc.storage.ConfigService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链管理类,负责各条链的初始化,运行,启动,参数维护等
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author tag
 * 2018/12/4
 * */
@Component
public class ChainManager {
    @Autowired
    private ConfigService configService;
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

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * 初始化并启动链
     * Initialize and start the chain
     * */
    public void runChain(){
        Map<Integer, ConfigBean> configMap = configChain();
        if(configMap == null || configMap.size() == 0){
            return;
        }
        /*
        根据配置信息创建初始化链
        Initialize chains based on configuration information
        */
        for (Map.Entry<Integer,ConfigBean> entry:configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            chain.setConfig(entry.getValue());

            /*
            * 初始化链日志对象
            * Initialization Chain Log Objects
            * */
            initLogger(chain);

            /*
            初始化链数据库表
            Initialize linked database tables
            */
            initTable(chain);

            /*
            加载链缓存数据
            Load chain caching data
            */
            initCache(chain);

            /*
            创建并启动链内任务
            Create and start in-chain tasks
            */
            schedulerManager.createChainScheduler(chain);

            chainMap.put(chainId,chain);
        }
    }

    /**
     * 停止一条链
     * stop a chain
     *
     * @param chainId 链ID/chain id
     * */
    public void stopChain(int chainId){

    }

    /**
     * 删除一条链
     * delete a chain
     * */
    public void deleteChain(int chainId){

    }


    /**
     * 读取配置文件创建并初始化链
     * Read the configuration file to create and initialize the chain
     * */
    private Map<Integer, ConfigBean> configChain(){
        try {
            /*
            读取数据库链信息配置
            Read database chain information configuration
             */
            configService.delete(1);
            Map<Integer, ConfigBean> configMap = configService.getList();
            /*
            如果系统是第一次运行，则本地数据库没有存储链信息，此时需要从配置文件读取主链配置信息
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if(configMap == null || configMap.size() == 0){
                String configJson = IoUtils.read(ConsensusConstant.CONFIG_FILE_PATH);
                List<ConfigItem> configItemList = JSONUtils.json2list(configJson,ConfigItem.class);
                ConfigBean configBean = ConfigManager.initManager(configItemList);
                if(configBean == null){
                    return null;
                }
                configMap.put(configBean.getChainId(),configBean);
            }
            return configMap;
        }catch(Exception e){
            Log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 初始化链相关表
     * Initialization chain correlation table
     * @param chain   chain info
     * */
    private void initTable(Chain chain){
        NulsLogger logger = chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME);
        int chainId = chain.getConfig().getChainId();
        try {
            /*
            创建共识节点表
            Create consensus node tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_AGENT+chainId);

            /*
            创建共识信息表
            Create consensus information tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT+chainId);

            /*
            创建红黄牌信息表
            Creating Red and Yellow Card Information Table
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH+chainId);
        }catch (Exception e){
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                logger.error(e.getMessage());
            }else{
                logger.error(e.getMessage());
            }
        }
    }

    private void initLogger(Chain chain){
        /*
        * 共识模块日志文件对象创建,如果一条链有多类日志文件，可在此添加
        * Creation of Log File Object in Consensus Module，If there are multiple log files in a chain, you can add them here
        * */
        NulsLogger consensusLogger = LoggerBuilder.getLogger(String.valueOf(chain.getConfig().getChainId()),ConsensusConstant.CONSENSUS_LOGGER_NAME);
        NulsLogger rpcLogger = LoggerBuilder.getLogger(String.valueOf(chain.getConfig().getChainId()),ConsensusConstant.BASIC_LOGGER_NAME);
        chain.getLoggerMap().put(ConsensusConstant.CONSENSUS_LOGGER_NAME,consensusLogger);
        chain.getLoggerMap().put(ConsensusConstant.BASIC_LOGGER_NAME,rpcLogger);
    }

    /**
     * 初始化链缓存数据
     * Initialize chain caching data
     *
     * @param chain  chain info
     * */
    private void initCache(Chain chain){
        try {
            /*todo
            缓存最近x轮区块头数据
            Cache the latest X rounds of block header data
            */
            int length = 1000;
            int roundIndex = 1;
            List<BlockHeader>blockHeaderList = new ArrayList<>();
            Address packingAddress = new Address(1,(byte)1, SerializeUtils.sha256hash160("y5WhgP1iu2Qwt5CiaPTV4Fegfgqmd".getBytes()));
            Address packingAddress1 = new Address(1,(byte)1,SerializeUtils.sha256hash160("a5WhgP1iu2Qwt5CiaPTV4Fegfgqmd".getBytes()));
            for (int index = 0;index < length;index++) {
                BlockHeader blockHeader = new BlockHeader();
                blockHeader.setHeight(index+1);
                blockHeader.setPreHash(NulsDigestData.calcDigestData("00000000000".getBytes()));
                blockHeader.setTime(TimeService.currentTimeMillis());
                blockHeader.setTxCount(1);
                blockHeader.setHash(new NulsDigestData());
                blockHeader.setMerkleHash(NulsDigestData.calcDigestData(new byte[20]));

                // add a round data
                BlockRoundData roundData = new BlockRoundData();
                roundData.setConsensusMemberCount(5);
                roundData.setPackingIndexOfRound(5);
                if((index+1)%10 == 0){
                    roundIndex++;
                    blockHeader.setPackingAddress(packingAddress1.getAddressBytes());
                }
                if((index+1)%10 == 6){
                    blockHeader.setPackingAddress(packingAddress.getAddressBytes());
                }
                roundData.setRoundIndex(roundIndex);
                roundData.setRoundStartTime(TimeService.currentTimeMillis());
                try {
                    blockHeader.setExtend(roundData.serialize());
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
                blockHeaderList.add(blockHeader);
            }
            chain.setBlockHeaderList(blockHeaderList);
            chain.setNewestHeader(blockHeaderList.get(blockHeaderList.size()-1));
            agentManager.loadAgents(chain);
            depositManager.loadDeposits(chain);
            punishManager.loadPunishes(chain);
            roundManager.initRound(chain);
        }catch (Exception e){
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
        }
    }

    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }
}
