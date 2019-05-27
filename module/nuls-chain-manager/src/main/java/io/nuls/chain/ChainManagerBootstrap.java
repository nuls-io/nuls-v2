package io.nuls.chain;

import io.nuls.base.basic.AddressTool;
import io.nuls.chain.config.NulsChainConfig;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.rpc.call.impl.RpcServiceImpl;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.impl.ChainServiceImpl;
import io.nuls.chain.service.impl.CmTaskManager;
import io.nuls.chain.service.tx.ChainAssetCommitAdvice;
import io.nuls.chain.service.tx.ChainAssetRollbackAdvice;
import io.nuls.chain.storage.InitDB;
import io.nuls.chain.storage.impl.*;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.cmd.common.CommonAdvice;
import io.nuls.core.rpc.cmd.common.TransactionDispatcher;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.protocol.ProtocolGroupManager;
import io.nuls.core.rpc.protocol.ProtocolLoader;
import io.nuls.core.rpc.util.RegisterHelper;
import io.nuls.core.rpc.util.TimeUtils;

import java.io.File;
import java.math.BigInteger;

/**
 * 链管理模块启动类
 * Main class of BlockChain module
 *
 * @author tangyi
 * @date 2018/11/7
 */
@Component
public class ChainManagerBootstrap extends RpcModule {
    @Autowired
    private NulsChainConfig nulsChainConfig;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.nuls.chain", args);
    }


    /**
     * 读取resources/module.ini，初始化配置
     * Read resources/module.ini to initialize the configuration
     */
    private void initCfg() throws Exception {
        CmRuntimeInfo.nulsAssetId = nulsChainConfig.getMainAssetId();
        CmRuntimeInfo.nulsChainId = nulsChainConfig.getMainChainId();
        long decimal = (long) Math.pow(10, Integer.valueOf(nulsChainConfig.getDefaultDecimalPlaces()));
        BigInteger initNumber = BigIntegerUtils.stringToBigInteger(nulsChainConfig.getNulsAssetInitNumberMax()).multiply(
                BigInteger.valueOf(decimal));
        nulsChainConfig.setNulsAssetInitNumberMax(BigIntegerUtils.bigIntegerToString(initNumber));
        BigInteger assetDepositNuls = BigIntegerUtils.stringToBigInteger(nulsChainConfig.getAssetDepositNuls()).multiply(
                BigInteger.valueOf(decimal));
        nulsChainConfig.setAssetDepositNuls(BigIntegerUtils.bigIntegerToString(assetDepositNuls));
        BigInteger assetInitNumberMin = BigIntegerUtils.stringToBigInteger(nulsChainConfig.getAssetInitNumberMin()).multiply(
                BigInteger.valueOf(decimal));
        nulsChainConfig.setAssetInitNumberMin(BigIntegerUtils.bigIntegerToString(assetInitNumberMin));
        BigInteger assetInitNumberMax = BigIntegerUtils.stringToBigInteger(nulsChainConfig.getAssetInitNumberMax()).multiply(
                BigInteger.valueOf(decimal));
        nulsChainConfig.setAssetInitNumberMax(BigIntegerUtils.bigIntegerToString(assetInitNumberMax));
        nulsChainConfig.setNulsFeeMainNetPercent((int) (Double.valueOf(nulsChainConfig.getNulsFeeMainNetRate()) * 100));
        nulsChainConfig.setNulsFeeOtherNetPercent((int) (Double.valueOf(nulsChainConfig.getNulsFeeOtherNetRate()) * 100));
        CmConstants.BLACK_HOLE_ADDRESS = AddressTool.getAddressByPubKeyStr(nulsChainConfig.getBlackHolePublicKey(), CmRuntimeInfo.getMainIntChainId(), nulsChainConfig.getEncoding());
        LoggerUtil.defaultLogInit(CmRuntimeInfo.getMainIntChainId());
    }

    /**
     * 如果数据库中有相同的配置，则以数据库为准
     * If the database has the same configuration, use the database entity
     *
     * @throws Exception Any error will throw an exception
     */
    private void initWithDatabase() throws Exception {
        /* 打开数据库连接 (Open database connection) */
        RocksDBService.init(nulsChainConfig.getDataPath() + File.separator + ModuleE.CM.name);
        InitDB assetStorage = SpringLiteContext.getBean(AssetStorageImpl.class);
        assetStorage.initTableName();
        LoggerUtil.logger().info("assetStorage.init complete.....");
        InitDB blockHeightStorage = SpringLiteContext.getBean(BlockHeightStorageImpl.class);
        blockHeightStorage.initTableName();
        LoggerUtil.logger().info("blockHeightStorage.init complete.....");
        InitDB cacheDatasStorage = SpringLiteContext.getBean(CacheDatasStorageImpl.class);
        cacheDatasStorage.initTableName();
        LoggerUtil.logger().info("cacheDatasStorage.init complete.....");
        InitDB chainAssetStorage = SpringLiteContext.getBean(ChainAssetStorageImpl.class);
        chainAssetStorage.initTableName();
        LoggerUtil.logger().info("chainAssetStorage.init complete.....");
        InitDB chainStorage = SpringLiteContext.getBean(ChainStorageImpl.class);
        chainStorage.initTableName();
        LoggerUtil.logger().info("chainStorage.init complete.....");
        InitDB chainCirculateStorage = SpringLiteContext.getBean(ChainCirculateStorageImpl.class);
        chainCirculateStorage.initTableName();
        LoggerUtil.logger().info("chainCirculateStorage.init complete.....");
    }


    /**
     * 把Nuls2.0主网信息存入数据库中
     * Store the Nuls2.0 main network information into the database
     *
     * @throws Exception Any error will throw an exception
     */
    private void initMainChain() throws Exception {
        SpringLiteContext.getBean(ChainService.class).initMainChain();
    }

    private void initChainDatas() throws Exception {
        SpringLiteContext.getBean(CacheDataService.class).initBlockDatas();
        ChainServiceImpl chainService = SpringLiteContext.getBean(ChainServiceImpl.class);
        RpcServiceImpl rpcService = SpringLiteContext.getBean(RpcServiceImpl.class);
        long mainNetMagicNumber = rpcService.getMainNetMagicNumber();
        chainService.initRegChainDatas(mainNetMagicNumber);
        LoggerUtil.logger().info("initChainDatas complete....");
    }


    @Override
    public Module[] declareDependent() {
        return new Module[]{
                Module.build(ModuleE.TX),
                Module.build(ModuleE.LG),
                Module.build(ModuleE.NW),
                Module.build(ModuleE.AC)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CM.abbr, "1.0");
    }

    /**
     * 初始化模块信息，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        super.init();
        try {
            /* Read resources/module.ini to initialize the configuration */
            initCfg();
            LoggerUtil.logger().info("initCfg complete.....");
            /*storage info*/
            initWithDatabase();
            LoggerUtil.logger().info("initWithDatabase complete.....");
            /* 把Nuls2.0主网信息存入数据库中 (Store the Nuls2.0 main network information into the database) */
            initMainChain();
            LoggerUtil.logger().info("initMainChain complete.....");
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            LoggerUtil.logger().error("初始化异常退出....");
            System.exit(-1);
        }
    }

    @Override
    public boolean doStart() {
        try {
            /* 进行数据库数据初始化（避免异常关闭造成的事务不一致） */
            initChainDatas();
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            LoggerUtil.logger().error("启动异常退出....");
            System.exit(-1);
        }
        LoggerUtil.logger().info("doStart ok....");
        return true;
    }

    @Override
    public void onDependenciesReady(Module module) {
        try {
            TransactionDispatcher transactionDispatcher = SpringLiteContext.getBean(TransactionDispatcher.class);
            CommonAdvice commitAdvice = SpringLiteContext.getBean(ChainAssetCommitAdvice.class);
            CommonAdvice rollbackAdvice = SpringLiteContext.getBean(ChainAssetRollbackAdvice.class);
            transactionDispatcher.register(commitAdvice, rollbackAdvice);
            ProtocolLoader.load(CmRuntimeInfo.getMainIntChainId());
            /*注册交易处理器*/
            if (ModuleE.TX.abbr.equals(module.getName())) {
                int chainId = CmRuntimeInfo.getMainIntChainId();
                RegisterHelper.registerTx(chainId, ProtocolGroupManager.getCurrentProtocol(chainId));
                LoggerUtil.logger().info("regTxRpc complete.....");
            }
            if (ModuleE.PU.abbr.equals(module.getName())) {
                //注册相关交易
                RegisterHelper.registerProtocol(CmRuntimeInfo.getMainIntChainId());
                LoggerUtil.logger().info("register protocol ...");
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            System.exit(-1);

        }
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        CmTaskManager cmTaskManager = SpringLiteContext.getBean(CmTaskManager.class);
        cmTaskManager.start();
        TimeUtils.getInstance().start(5 * 60 * 1000);
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
