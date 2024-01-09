package io.nuls.block.thread.monitor;

import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.core.log.logback.NulsLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Timed scheduling task base class
 *
 * @author captain
 * @version 1.0
 * @date 2019/4/23 10:58
 */

public abstract class BaseMonitor implements Runnable {

    /**
     * List of executable business logic states(Default only includes{@link StatusEnum#RUNNING})
     */
    protected List<StatusEnum> runningStatusEnumList = new ArrayList<>();

    /**
     * Thread Flag
     */
    protected String symbol;

    BaseMonitor() {
        this.symbol = this.getClass().getName();
        this.runningStatusEnumList.add(StatusEnum.RUNNING);
    }

    public BaseMonitor(List<StatusEnum> runningStatusEnumList) {
        this();
        this.runningStatusEnumList.addAll(runningStatusEnumList);
    }

    @Override
    public void run() {
        for (Integer chainId : ContextManager.CHAIN_ID_LIST) {
            ChainContext context = ContextManager.getContext(chainId);
            NulsLogger logger = context.getLogger();
            StatusEnum status = context.getStatus();
            try {
                //Determine the operational status of the chain,Only run scheduled monitoring threads during normal operation
                if (runningStatusEnumList.contains(status)) {
                    process(chainId, context, logger);
//                } else {
//                    logger.debug("skip process, status is " + status);
                }
            } catch (Exception e) {
                context.setStatus(status);
                logger.error(symbol + " running fail", e);
            }
        }
    }

    /**
     * Specific business processing methods
     *
     * @param chainId
     * @param context
     * @param commonLog
     */
    protected abstract void process(int chainId, ChainContext context, NulsLogger commonLog);
}
