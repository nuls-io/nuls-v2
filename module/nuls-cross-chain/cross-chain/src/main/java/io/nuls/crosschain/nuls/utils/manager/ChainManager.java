package io.nuls.crosschain.nuls.utils.manager;

import io.nuls.base.data.BlockHeader;
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
import io.nuls.crosschain.nuls.model.bo.CmdRegisterDto;
import io.nuls.crosschain.nuls.model.bo.config.ConfigBean;
import io.nuls.crosschain.nuls.rpc.call.BlockCall;
import io.nuls.crosschain.nuls.rpc.call.SmartContractCall;
import io.nuls.crosschain.nuls.srorage.ConfigService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.LoggerUtil;
import io.nuls.crosschain.nuls.utils.thread.handler.*;
import io.nuls.crosschain.nuls.utils.thread.task.GetRegisteredChainTask;

import java.util.*;
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
     */
    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * 缓存已注册跨链的链信息
     */
    private List<ChainInfo> registeredCrossChainList = new ArrayList<>();

    /**
     * 缓存每条链最新区块头
     * */
    private Map<Integer, BlockHeader> chainHeaderMap = new ConcurrentHashMap<>();

    /**
     * 主网节点返回的已注册跨链交易列表信息
     */
    private List<RegisteredChainMessage> registeredChainMessageList = new ArrayList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadUtils.createScheduledThreadPool(2, new NulsThreadFactory("getRegisteredChainTask"));

    private boolean crossNetUseAble = false;

    /**
     * 初始化
     * Initialization chain
     */
    public void initChain() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            Log.info("链初始化失败！");
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

        if(!config.isMainNet()){
            RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
            if(registeredChainMessage != null){
                registeredCrossChainList = registeredChainMessage.getChainInfoList();
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
     * 注册智能合约交易
     * */
    public void registerContractTx(){
        for (Chain chain:chainMap.values()) {
            /*
             * 注册智能合约交易
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
     * 加载链缓存数据并启动链
     * Load the chain to cache data and start the chain
     */
    public void runChain() {
        for (Chain chain : chainMap.values()) {
            chain.getThreadPool().execute(new HashMessageHandler(chain));
            chain.getThreadPool().execute(new CtxMessageHandler(chain));
            chain.getThreadPool().execute(new SignMessageHandler(chain));
            chain.getThreadPool().execute(new OtherCtxMessageHandler(chain));
            chain.getThreadPool().execute(new GetCtxStateHandler(chain));
            chain.getThreadPool().execute(new SignMessageByzantineHandler(chain));
            chainHeaderMap.put(chain.getChainId(), BlockCall.getLatestBlockHeader(chain));

        }
        if(!config.isMainNet()){
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new GetRegisteredChainTask(this),  20L, 10 * 60L, TimeUnit.SECONDS );
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
                if(config.getVerifiers() != null){
                    configBean.setVerifierSet(new HashSet<>(Arrays.asList(config.getVerifiers().split(NulsCrossChainConstant.VERIFIER_SPLIT))));
                }else{
                    configBean.setVerifierSet(new HashSet<>());
                }
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
            value:本链协议跨链交易及状态
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CTX_STATUS + chainId);

            /*
            保存已验证通过的跨链交易
            key:本链协议跨链交易hash
            value：主网协议跨链交易
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONVERT_CTX + chainId);

            /*
            已打包的跨链交易
            New Cross-Chain Transactions
            key:主网协议跨链交易hash
            value:本链协议HASH
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CONVERT_HASH_CTX + chainId);

            /*
            已拜占庭完成的跨链交易
            New Cross-Chain Transactions
            key:主网协议跨链交易hash
            value:主网协议跨链交易(发起链签名的主网协议签名)
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_OTHER_COMMITED_CTX + chainId);

            /*
            待广播的高度交易
            Processing completed cross-chain transactions (broadCasted to other chains)
            key:高度
            value:List<LocalHash>
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_SEND_HEIGHT + chainId);

            /*
            处理完成的跨链交易（已广播给其他链）
            Processing completed cross-chain transactions (broadCasted to other chains)
            key:高度
            value:List<LocalHash>
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_SENDED_HEIGHT + chainId);

            /*
            保存处理在处理成功的跨链交易记录
            Keep records of successful cross-chain transactions processed
            key：跨链交易Hash
            value:处理成功与否
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_CTX_STATE+ chainId);

            /*
            广播失败的验证人变更消息
            Keep records of successful cross-chain transactions processed
            key:高度
            value:List<chainId>
            */
            RocksDBService.createTable(NulsCrossChainConstant.DB_NAME_BROAD_FAILED+ chainId);
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
