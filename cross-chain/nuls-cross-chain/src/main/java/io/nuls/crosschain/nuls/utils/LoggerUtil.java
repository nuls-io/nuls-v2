package io.nuls.crosschain.nuls.utils;

import ch.qos.logback.classic.Level;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainConstant.*;

/**
 * 日志管理类
 * Log Management Class
 * @author tag
 * 2019/4/10
 */
public class LoggerUtil {
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
        NulsLogger messageLogger = LoggerBuilder.getLogger(String.valueOf(chainId), MESSAGE_LOG_NAME, Level.DEBUG);
        NulsLogger rpcLogger = LoggerBuilder.getLogger(String.valueOf(chainId), RPC_LOG_NAME, Level.DEBUG);
        NulsLogger basicLogger = LoggerBuilder.getLogger(String.valueOf(chainId), BASIC_LOG_NAME, Level.DEBUG);
        chain.setMessageLog(messageLogger);
        chain.setRpcLogger(rpcLogger);
        chain.setBasicLog(basicLogger);
    }
}
