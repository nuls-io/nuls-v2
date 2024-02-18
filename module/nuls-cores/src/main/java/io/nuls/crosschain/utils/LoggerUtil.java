package io.nuls.crosschain.utils;

import io.nuls.core.rpc.model.ModuleE;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;

/**
 * Log management class
 * Log Management Class
 * @author tag
 * 2019/4/10
 */
public class LoggerUtil {
    private static  String FOLDER_PREFIX = ModuleE.Constant.CROSS_CHAIN;

    /**
     * Cross chain module common log class
     * Cross-Chain Module Common Log Class
     * */
    public static NulsLogger commonLog = LoggerBuilder.getLogger(FOLDER_PREFIX);

    /**
     * Initialize log information for a certain chain
     * Initialize log information for a chain
     * @param chain chain info
     * */
    public static void initLogger(Chain chain) {
        int chainId = chain.getConfig().getChainId();
        chain.setLogger(LoggerBuilder.getLogger(ModuleE.Constant.CROSS_CHAIN,chainId));
    }
}
