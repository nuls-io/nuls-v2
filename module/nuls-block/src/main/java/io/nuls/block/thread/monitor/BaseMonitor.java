package io.nuls.block.thread.monitor;

import io.nuls.block.constant.StatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.core.log.logback.NulsLogger;

/**
 * 定时调度任务基类
 *
 * @author captain
 * @version 1.0
 * @date 2019/4/23 10:58
 */
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMonitor implements Runnable {

    /**
     * 可执行业务逻辑的状态列表(默认只包含{@link StatusEnum#RUNNING})
     */
    protected List<StatusEnum> runningStatusEnumList = new ArrayList<>();

    /**
     * 线程标志
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
            NulsLogger commonLog = context.getCommonLog();
            StatusEnum status = context.getStatus();
            try {
                //判断该链的运行状态,只有正常运行时才运行定时监控线程
                if (runningStatusEnumList.contains(status)) {
                    process(chainId, context, commonLog);
                } else {
                    commonLog.debug("skip process, status is " + status + ", chainId-" + chainId);
                }
            } catch (Exception e) {
                context.setStatus(status);
                commonLog.error("chainId-" + chainId + ", " + symbol + " running fail", e);
            }
        }
    }

    /**
     * 具体业务处理方法
     *
     * @param chainId
     * @param context
     * @param commonLog
     */
    protected abstract void process(int chainId, ChainContext context, NulsLogger commonLog);
}
