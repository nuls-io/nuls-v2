package io.nuls.poc.utils.manager;

import ch.qos.logback.classic.Level;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.config.ConfigItem;
import io.nuls.poc.model.bo.tx.TxRegisterDetail;
import io.nuls.poc.storage.ConfigService;
import io.nuls.poc.utils.CallMethodUtils;
import io.nuls.poc.utils.annotation.ResisterTx;
import io.nuls.poc.utils.enumeration.TxMethodType;
import io.nuls.poc.utils.enumeration.TxProperty;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.tools.parse.JSONUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链管理类,负责各条链的初始化,运行,启动,参数维护等
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author tag
 * 2018/12/4
 */
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
    @Autowired
    private BlockManager blockManager;

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * 初始化并启动链
     * Initialize and start the chain
     */
    public void runChain() {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            return;
        }
        List<TxRegisterDetail> txRegisterDetailList = getRegisterTxList();
        /*
        根据配置信息创建初始化链
        Initialize chains based on configuration information
        */
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            chain.setConfig(entry.getValue());

            /*
             * 初始化链日志对象
             * Initialization Chain Log Objects
             * */
            initLogger(chain);

            /*
             * 链交易注册
             * Chain Trading Registration
             * */
            while (true) {
                if (CallMethodUtils.registerTx(chain, txRegisterDetailList)) {
                    break;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

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

            chainMap.put(chainId, chain);
        }
    }

    /**
     * 停止一条链
     * stop a chain
     *
     * @param chainId 链ID/chain id
     */
    public void stopChain(int chainId) {

    }

    /**
     * 删除一条链
     * delete a chain
     */
    public void deleteChain(int chainId) {

    }


    /**
     * 读取配置文件创建并初始化链
     * Read the configuration file to create and initialize the chain
     */
    private Map<Integer, ConfigBean> configChain() {
        try {
            /*
            读取数据库链信息配置
            Read database chain information configuration
             */
            Map<Integer, ConfigBean> configMap = configService.getList();
            /*
            如果系统是第一次运行，则本地数据库没有存储链信息，此时需要从配置文件读取主链配置信息
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if (configMap == null || configMap.size() == 0) {
                String configJson = IoUtils.read(ConsensusConstant.CONFIG_FILE_PATH);
                List<ConfigItem> configItemList = JSONUtils.json2list(configJson, ConfigItem.class);
                ConfigBean configBean = ConfigManager.initManager(configItemList);
                if (configBean == null) {
                    return null;
                }
                configMap.put(configBean.getChainId(), configBean);
            }
            return configMap;
        } catch (Exception e) {
            Log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 初始化链相关表
     * Initialization chain correlation table
     *
     * @param chain chain info
     */
    private void initTable(Chain chain) {
        NulsLogger logger = chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME);
        int chainId = chain.getConfig().getChainId();
        try {
            /*
            创建共识节点表
            Create consensus node tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_AGENT + chainId);

            /*
            创建共识信息表
            Create consensus information tables
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_DEPOSIT + chainId);

            /*
            创建红黄牌信息表
            Creating Red and Yellow Card Information Table
            */
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSENSUS_PUNISH + chainId);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                logger.error(e.getMessage());
            } else {
                logger.error(e.getMessage());
            }
        }
    }

    private void initLogger(Chain chain) {
        /*
         * 共识模块日志文件对象创建,如果一条链有多类日志文件，可在此添加
         * Creation of Log File Object in Consensus Module，If there are multiple log files in a chain, you can add them here
         * */
        NulsLogger consensusLogger = LoggerBuilder.getLogger(String.valueOf(chain.getConfig().getChainId()), ConsensusConstant.CONSENSUS_LOGGER_NAME, Level.INFO);
        NulsLogger rpcLogger = LoggerBuilder.getLogger(String.valueOf(chain.getConfig().getChainId()), ConsensusConstant.BASIC_LOGGER_NAME, Level.INFO);
        chain.getLoggerMap().put(ConsensusConstant.CONSENSUS_LOGGER_NAME, consensusLogger);
        chain.getLoggerMap().put(ConsensusConstant.BASIC_LOGGER_NAME, rpcLogger);
    }

    /**
     * 初始化链缓存数据
     * 在poc的共识机制下，由于存在轮次信息，节点信息，以及节点被惩罚的红黄牌信息，
     * 因此需要在初始化的时候，缓存相关的数据，用于计算最新的轮次信息，以及各个节点的信用值等
     * Initialize chain caching data
     *
     * @param chain chain info
     */
    private void initCache(Chain chain) {
        try {
            blockManager.loadBlockHeader(chain);
            agentManager.loadAgents(chain);
            depositManager.loadDeposits(chain);
            punishManager.loadPunishes(chain);
            roundManager.initRound(chain);
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
        }
    }

    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }

    /**
     * 向交易模块注册交易
     * Register transactions with the transaction module
     */
    private static List<TxRegisterDetail> getRegisterTxList() {
        List<Class> classList = ScanUtil.scan(ConsensusConstant.RPC_PATH);
        if (classList == null || classList.size() == 0) {
            return null;
        }
        Map<Integer, TxRegisterDetail> registerDetailMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        for (Class clz : classList) {
            Method[] methods = clz.getMethods();
            for (Method method : methods) {
                ResisterTx annotation = getRegisterAnnotation(method);
                if (annotation != null) {
                    if (!registerDetailMap.containsKey(annotation.txType().txType)) {
                        registerDetailMap.put(annotation.txType().txType, new TxRegisterDetail(annotation.txType()));
                    }
                    if (annotation.methodType().equals(TxMethodType.VALID)) {
                        registerDetailMap.get(annotation.txType().txType).setValidator(annotation.methodName());
                    }
                }
            }
        }
        registerDetailMap.put(ConsensusConstant.TX_TYPE_COINBASE, new TxRegisterDetail(TxProperty.COIN_BASE));
        registerDetailMap.put(ConsensusConstant.TX_TYPE_RED_PUNISH, new TxRegisterDetail(TxProperty.RED_PUNISH));
        registerDetailMap.put(ConsensusConstant.TX_TYPE_YELLOW_PUNISH, new TxRegisterDetail(TxProperty.YELLOW_PUNISH));
        return new ArrayList<>(registerDetailMap.values());
    }

    private static ResisterTx getRegisterAnnotation(Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (ResisterTx.class.equals(annotation.annotationType())) {
                return (ResisterTx) annotation;
            }
        }
        return null;
    }
}
