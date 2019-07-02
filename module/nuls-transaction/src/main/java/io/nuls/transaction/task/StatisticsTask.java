package io.nuls.transaction.task;

import java.util.concurrent.atomic.AtomicInteger;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * Test
 *
 * @author: Charlie
 * @date: 2018/11/28
 */
public class StatisticsTask implements Runnable {

    public static AtomicInteger countRc = new AtomicInteger(0);
    public static AtomicInteger txNetListTotal = new AtomicInteger(0);
    public static AtomicInteger packageTxs = new AtomicInteger(0);
    public static AtomicInteger addOrphanCount = new AtomicInteger(0);
    public static AtomicInteger processExitsLedgerTx = new AtomicInteger(0);
    public static AtomicInteger orphanTxTotal = new AtomicInteger(0);
    public static AtomicInteger confirmedTx = new AtomicInteger(0);
    public static int clearUnconfirmedTx;

    @Override
    public void run() {
        LOG.debug("");
        LOG.debug("累计接收完整新交易:{}", countRc.get());
        LOG.debug("处理网络交易总数:{}", txNetListTotal.get());
        LOG.debug("加入孤儿交易队列总数:{}", addOrphanCount.get());
        LOG.debug("发送给共识打包交易总数:{}", packageTxs.get());
        LOG.debug("清理机制清理交易总数:{}", clearUnconfirmedTx);
        LOG.debug("已确认交易总数:{}", confirmedTx.get());
        LOG.debug("");
    }
}
