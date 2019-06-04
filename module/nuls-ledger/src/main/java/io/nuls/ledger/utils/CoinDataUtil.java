package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.core.log.Log;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.TxUnconfirmed;
import io.nuls.core.exception.NulsException;

import java.util.Map;

/**
 * Created by ljs on 2018/12/30.
 *
 * @author lanjinsheng
 */
public class CoinDataUtil {
    /**
     * parseCoinData
     *
     * @param stream
     * @return
     */
    public static CoinData parseCoinData(byte[] stream) {
        if (null == stream) {
            return null;
        }
        CoinData coinData = new CoinData();
        try {
            coinData.parse(new NulsByteBuffer(stream));
        } catch (NulsException e) {
            Log.error("coinData parse error", e);
        }
        return coinData;
    }

    public static void calTxFromAmount(int chainId, Map<String, TxUnconfirmed> map, CoinFrom coinFrom, byte[] txNonce, String accountKey) {
        TxUnconfirmed txUnconfirmed = null;
        if (null == map.get(accountKey)) {
            txUnconfirmed = new TxUnconfirmed(AddressTool.getStringAddressByBytes(coinFrom.getAddress()),coinFrom.getAssetsChainId(),
                    coinFrom.getAssetsId(),coinFrom.getNonce(),txNonce,coinFrom.getAmount());
            map.put(accountKey,txUnconfirmed);
        } else {
            txUnconfirmed = map.get(accountKey);
            System.arraycopy(txNonce, 0, txUnconfirmed.getNonce(), 0, LedgerConstant.NONCE_LENGHT);
            System.arraycopy(coinFrom.getNonce(), 0, txUnconfirmed.getFromNonce(), 0, LedgerConstant.NONCE_LENGHT);
            txUnconfirmed.setAmount(txUnconfirmed.getAmount().add(coinFrom.getAmount()));
        }
    }

}
