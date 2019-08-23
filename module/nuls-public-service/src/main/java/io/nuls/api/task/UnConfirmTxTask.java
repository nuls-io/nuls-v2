package io.nuls.api.task;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.db.TransactionService;
import io.nuls.api.model.po.TxHexInfo;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.basic.Result;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.DateUtils;
import io.nuls.core.rpc.util.NulsDateUtils;

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
            long currentTime = NulsDateUtils.getCurrentTimeMillis();
            for (int i = txHexInfoList.size() - 1; i >= 0; i--) {
                TxHexInfo txHexInfo = txHexInfoList.get(i);
                int txType = extractTxTypeFromTx(txHexInfo.getTxHex());
                if (txType == 10) {
                    continue;
                }
                if (txHexInfo.getTime() < currentTime && txHexInfo.getTime() + DateUtils.TEN_MINUTE_TIME > currentTime) {
                    Result result = WalletRpcHandler.validateTx(chainId, txHexInfo.getTxHex());
                    if (!result.isSuccess()) {
                        transactionService.deleteUnConfirmTx(chainId, txHexInfo.getTxHash());
                        txHexInfoList.remove(i);
                    } else {
                        WalletRpcHandler.broadcastTx(chainId, txHexInfo.getTxHex());
                    }
                } else if (txHexInfo.getTime() + DateUtils.TEN_MINUTE_TIME < currentTime) {
                    transactionService.deleteUnConfirmTx(chainId, txHexInfo.getTxHash());
                }
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }

    private int extractTxTypeFromTx(String txString) throws NulsException {
        String txTypeHexString = txString.substring(0, 4);
        NulsByteBuffer byteBuffer = new NulsByteBuffer(RPCUtil.decode(txTypeHexString));
        return byteBuffer.readUint16();
    }

}
