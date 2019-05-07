package io.nuls.api.task;

import io.nuls.api.db.TransactionService;
import io.nuls.api.model.po.db.TxHexInfo;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rpc.util.TimeUtils;
import io.nuls.core.model.DateUtils;

import java.util.List;

public class UnConfirmTxTask implements Runnable {

    private int chainId;

    private TransactionService transactionService;

    public UnConfirmTxTask(int chainId) {
        this.chainId = chainId;
        transactionService = SpringLiteContext.getBean(TransactionService.class);
    }

    @Override
    public void run() {
        try {
            List<TxHexInfo> txHexInfoList = transactionService.getUnConfirmList(chainId);
            long currentTime = TimeUtils.getCurrentTimeMillis();
            for (int i = txHexInfoList.size() - 1; i >= 0; i--) {
                TxHexInfo txHexInfo = txHexInfoList.get(i);
                if (txHexInfo.getTime() + DateUtils.TEN_MINUTE_TIME < currentTime) {
                    transactionService.deleteUnConfirmTx(chainId, txHexInfo.getTxHash());
                    txHexInfoList.remove(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
