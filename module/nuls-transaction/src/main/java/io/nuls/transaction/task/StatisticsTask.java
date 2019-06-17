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

    /**
     * 接收网络新交易
     */
    public static AtomicInteger countRc = new AtomicInteger(0);
    //统计 合计发给共识打包的交易数
    public static AtomicInteger packageTxs = new AtomicInteger(0);
    public static AtomicInteger netTxToPackablePoolCount = new AtomicInteger(0);
    public static AtomicInteger netTxSuccess = new AtomicInteger(0);
    public static AtomicInteger txNetListTotal = new AtomicInteger(0);
    public static AtomicInteger addUnverifiedQueue = new AtomicInteger(0);

    public static AtomicInteger addOrphanCount = new AtomicInteger(0);
    public static AtomicInteger exitsTx = new AtomicInteger(0);
    public static AtomicInteger processExitsTx = new AtomicInteger(0);
    public static AtomicInteger processExitsLedgerTx = new AtomicInteger(0);
    //处理孤儿交易时 该交易已存在
    public static AtomicInteger orphanTxConfirmed = new AtomicInteger(0);
    public static AtomicInteger orphanTxFailed = new AtomicInteger(0);
    public static AtomicInteger orphanTxTotal = new AtomicInteger(0);

    public static AtomicInteger confirmedTx = new AtomicInteger(0);
    public static AtomicInteger packingLedgerFail = new AtomicInteger(0);
    public static AtomicInteger packingLedgerOrphan = new AtomicInteger(0);

    @Override
    public void run() {
        LOG.debug("");

        LOG.debug("累计接收完整新交易:{}", countRc.get());
        LOG.debug("接收时已存在交易总数:{}", exitsTx.get());
        LOG.debug("接收网络交易总数:{}", addUnverifiedQueue.get());
        LOG.debug("处理网络交易总数:{}", txNetListTotal.get());
        LOG.debug("处理网络交易时已存在交易总数:{}", processExitsTx.get());

        LOG.debug("");

        LOG.debug("网络交易直接验证成功总数:{}", netTxSuccess.get());
        LOG.debug("账本处理网络交易时已存在交易总数:{}", processExitsLedgerTx.get());
        LOG.debug("加入孤儿交易队列总数:{}", addOrphanCount.get());
        LOG.debug("");
        LOG.debug("处理孤儿交易时已存在总数，-orphanTxConfirmed:{}", orphanTxConfirmed.get());
        LOG.debug("处理孤儿交易时验证失败总数，-orphanTxFailed:{}", orphanTxFailed.get());
        LOG.debug("已处理孤儿交易总数(意味着从孤儿队列移除)，-orphanTxFailed:{}", orphanTxFailed.get());
        LOG.debug("");

        LOG.debug("网络交易加入待打包队列总数:{}", netTxToPackablePoolCount.get());
        LOG.debug("打包账本孤儿总数:{}", packingLedgerOrphan.get());
        LOG.debug("打包账本失败总数:{}", packingLedgerFail.get());
        LOG.debug("发送给共识打包交易总数:{}", packageTxs.get());

        LOG.debug("");
        LOG.debug("已确认交易总数:{}", confirmedTx.get());


    }
}
