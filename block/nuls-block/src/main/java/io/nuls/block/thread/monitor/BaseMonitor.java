package io.nuls.block.thread.monitor;

import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.tools.log.logback.NulsLogger;

public abstract class BaseMonitor implements Runnable {

    private String symbol;

    BaseMonitor() {
        this.symbol = this.getClass().getName();
    }

    @Override
    public void run() {
        for (Integer chainId : ContextManager.chainIds) {
            ChainContext context = ContextManager.getContext(chainId);
            NulsLogger commonLog = context.getCommonLog();
            RunningStatusEnum status = context.getStatus();
            try {
                //判断该链的运行状态,只有正常运行时才运行定时监控线程
                if (status.equals(RunningStatusEnum.RUNNING)) {
                    process(chainId, context, commonLog);
                } else {
                    commonLog.debug("skip process, status is " + status + ", chainId-" + chainId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                context.setStatus(status);
                commonLog.error("chainId-" + chainId + ", " + symbol + " running fail");
            }
        }
    }

    protected abstract void process(int chainId, ChainContext context, NulsLogger commonLog);
}
