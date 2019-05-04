package io.nuls.api.task;

import io.nuls.api.db.TransactionService;
import io.nuls.api.model.po.db.TxHexInfo;
import io.nuls.core.rpc.util.TimeUtils;
import io.nuls.core.model.DateUtils;

import java.util.List;

public class UnconfirmTxTask implements Runnable {

    private int chainId;

    private TransactionService transactionService;

    public UnconfirmTxTask(int chainId) {
        this.chainId = chainId;

    }

    @Override
    public void run() {
        List<TxHexInfo> txHexInfoList = transactionService.getUnConfirmList(chainId);
        long currentTime = TimeUtils.getCurrentTimeMillis();
        for (int i = txHexInfoList.size() - 1; i >= 0; i--) {
            TxHexInfo txHexInfo = txHexInfoList.get(i);
            if (txHexInfo.getTime() + DateUtils.TEN_MINUTE_TIME < currentTime) {
                transactionService.deleteUnConfirmTx(chainId, txHexInfo.getTxHash());
                txHexInfoList.remove(i);
            }
        }
    }
}
