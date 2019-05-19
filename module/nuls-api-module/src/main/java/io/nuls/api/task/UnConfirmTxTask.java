package io.nuls.api.task;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.db.TransactionService;
import io.nuls.api.model.po.db.TxHexInfo;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rpc.util.TimeUtils;

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
                if (txHexInfo.getTime()  < currentTime) {
                    Result result = WalletRpcHandler.broadcastTx(chainId, txHexInfo.getTxHex());
                    if(!result.isSuccess()) {
                        transactionService.deleteUnConfirmTx(chainId, txHexInfo.getTxHash());
                        txHexInfoList.remove(i);
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }
}
