package io.nuls.crosschain.nuls.utils;

import ch.qos.logback.classic.Level;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.crosschain.base.constant.CrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainConstant.*;

/**
 * 日志管理类
 * Log Management Class
 * @author tag
 * 2019/4/10
 */
public class LoggerUtil {
    private static  String FOLDER_PREFIX = ModuleE.Constant.CROSS_CHAIN;

    /**
     * 跨链模块公用日志类
     * Cross-Chain Module Common Log Class
     * */
    public static NulsLogger commonLog = LoggerBuilder.getLogger(COMMON_LOG_NAME);

    /**
     * 初始化某条链的日志信息
     * Initialize log information for a chain
     * @param chain chain info
     * */
    public static void initLogger(Chain chain) {
        int chainId = chain.getConfig().getChainId();
        String bootFolder = CrossChainConstant.CHAIN + "-" + chainId;
        NulsLogger messageLogger = LoggerBuilder.getLogger(bootFolder, MESSAGE_LOG_NAME, Level.DEBUG);
        NulsLogger rpcLogger = LoggerBuilder.getLogger(bootFolder, RPC_LOG_NAME, Level.DEBUG);
        NulsLogger basicLogger = LoggerBuilder.getLogger(bootFolder, BASIC_LOG_NAME, Level.DEBUG);
        chain.setMessageLog(messageLogger);
        chain.setRpcLogger(rpcLogger);
        chain.setBasicLog(basicLogger);
    }
}
