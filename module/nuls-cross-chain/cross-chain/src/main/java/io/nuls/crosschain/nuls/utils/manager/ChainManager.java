package io.nuls.crosschain.nuls.utils.manager;

import io.nuls.base.protocol.ProtocolLoader;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.config.ConfigBean;
import io.nuls.crosschain.nuls.srorage.ConfigService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.LoggerUtil;
import io.nuls.crosschain.nuls.utils.thread.handler.CtxMessageHandler;
import io.nuls.crosschain.nuls.utils.thread.handler.HashMessageHandler;
import io.nuls.crosschain.nuls.utils.thread.handler.OtherCtxMessageHandler;
import io.nuls.crosschain.nuls.utils.thread.handler.SignMessageHandler;
import io.nuls.crosschain.nuls.utils.thread.task.GetRegisteredChainTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 链管理类,负责各条链的初始化,运行,启动,参数维护等
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author tag
 * 2019/4/10
 */
@Component
public class ChainManager {
    @Autowired
    private ConfigService configService;
    @Autowired
    private NulsCrossChainConfig config;
    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;
    /**
     * 链缓存
     * Chain cache
     * */
    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * 缓存已注册跨链的链信息
     * */
    private List<ChainInfo> registeredCrossChainList = new ArrayList<>();

    /**
     * 主网节点返回的已注册跨链交易列表信息
     * */
    private List<RegisteredChainMessage> registeredChainMessageList = new ArrayList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(2,new NulsThreadFactory("getRegisteredChainTask"));

    private boolean crossNetUseAble = false;

    /**
     * 初始化
     * Initialization chain
     * */
    public void initChain() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            Log.info("链初始化失败！");
            return;
        }
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()){
            Chain chain = new Chain();
            int chainId = entry.getKey();
            ConfigBean configBean = entry.getValue();
            if(chainId == config.getMainChainId() && configBean.getAssetId() == config.getMainAssetId()){
                config.setMainNet(true);
                chain.setMainChain(true);
            }
            chain.setConfig(configBean);
            /*
             * 初始化链日志对象
             * Initialization Chain Log Objects
             * */
            LoggerUtil.initLogger(chain);

            /*
            初始化链数据库表
            Initialize linked database tables
            */
            initTable(chain);

            chainMap.put(chainId, chain);
            ProtocolLoader.load(chainId);
        }
    }

    /**
     * 加载链缓存数据并启动链
     * Load the chain to cache data and start the chain
     * */
    public void runChain(){
        for (Chain chain:chainMap.values()) {
            chain.getThreadPool().execute(new HashMessageHandler(chain));
            chain.getThreadPool().execute(new CtxMessageHandler(chain));
            chain.getThreadPool().execute(new SignMessageHandler(chain));
            chain.getThreadPool().execute(new OtherCtxMessageHandler(chain));
        }
        if(!config.isMainNet()){
            RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
            if(registeredChainMessage != null){
                registeredCrossChainList = registeredChainMessage.getChainInfoList();
            }
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new GetRegisteredChainTask(this), 2 * 60L, 10 * 60L, TimeUnit.SECONDS );
        }else{
            crossNetUseAble = true;
        }
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
                ConfigBean configBean = config;
                boolean saveSuccess = configService.save(configBean,configBean.getChainId());
                if(saveSuccess){
                    configMap.put(configBean.getChainId(), configBean);
                }
            }
            return configMap;
        } catch (Exception e) {
            Log.error(e);
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
        int chainId = chain.getConfig().getChainId();
        try {
            /*
            新创建的跨链交易,用于保存本地新创建的和验证通过的跨链交易
            New Cross-Chain Transactions
            key:本链协议的交易Hash
            value:发起链或主链保存主网协议跨链交易/接收链保存接收链协议跨链交易
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_NEW_CTX + chainId);

            /*
            已打包但未广播给其他链的跨链交易
            Cross-chain transactions saved but not broadcast to other chains
            key:本链协议的交易Hash
            value:发起链或主链保存主网协议跨链交易/接收链保存接收链协议跨链交易
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_COMMITED_CTX + chainId);

            /*
            保存接收到的原始交易Hash和转换为本链协议的跨链交易Hash
            Save Received New Cross-Chain Transactions
            key:otherHash
            value:localHash
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONVERT_TO_CTX + chainId);

            /*
            保存本链交易已转换为其他链交易的Hash对应关系
            Save Received New Cross-Chain Transactions
            key:otherHash
            value:localHash
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONVERT_FROM_CTX + chainId);

            /*
            处理完成的跨链交易（已广播给其他链）
            Processing completed cross-chain transactions (broadCasted to other chains)
            key:本链协议的交易Hash
            value:发起链或主链保存主网协议跨链交易/接收链保存接收链协议跨链交易
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_SEND_HEIGHT + chainId);

            /*
            广播给其他链节点的区块高度和广播的跨链交易Hash列表
            Block Height Broadcast to Other Chain Nodes and Cross-Chain Transaction Hash List Broadcast
            key:block_Height
            value:List<需发送的跨链交易Hash>
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_COMPLETED_CTX + chainId);

            /*
            保存处理在处理成功的跨链交易记录
            Keep records of successful cross-chain transactions processed
            key：跨链交易Hash
            value:处理成功与否
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CTX_STATE+ chainId);
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

    public List<RegisteredChainMessage> getRegisteredChainMessageList() {
        return registeredChainMessageList;
    }

    public void setRegisteredChainMessageList(List<RegisteredChainMessage> registeredChainMessageList) {
        this.registeredChainMessageList = registeredChainMessageList;
    }

    public boolean isCrossNetUseAble() {
        return crossNetUseAble;
    }

    public void setCrossNetUseAble(boolean crossNetUseAble) {
        this.crossNetUseAble = crossNetUseAble;
    }
}
