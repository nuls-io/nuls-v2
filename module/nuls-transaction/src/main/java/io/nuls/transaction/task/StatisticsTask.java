package io.nuls.transaction.task;

import io.nuls.transaction.message.handler.BroadcastTxMessageHandler;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * Test
 *
 * @author: Charlie
 * @date: 2018/11/28
 */
public class StatisticsTask implements Runnable {

    @Override
    public void run() {

        LOG.debug("累计接收完整新交易:{}", BroadcastTxMessageHandler.countRc.get());

        LOG.debug("网络交易加入待打包队列总数:{}", NetTxProcessTask.netTxToPackablePoolCount.get());
    }


}