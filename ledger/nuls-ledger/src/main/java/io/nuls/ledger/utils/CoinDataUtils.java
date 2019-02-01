package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.ledger.model.UnconfirmedTx;
import io.nuls.tools.exception.NulsException;

import java.util.Map;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * Created by ljs on 2018/12/30.
 */
public class CoinDataUtils {
    /**
     * parseCoinData
     *
     * @param stream
     * @return
     */
    public static CoinData parseCoinData(byte[] stream) {
        if(null == stream){
            return null;
        }
        CoinData coinData = new CoinData();
        try {
            coinData.parse(new NulsByteBuffer(stream));
        } catch (NulsException e) {
            logger.error("coinData parse error", e);
        }
        return coinData;
    }

    public  static void  calTxFromAmount(Map<String,UnconfirmedTx> map, CoinFrom coinFrom,String txHash, String accountKey){
        UnconfirmedTx unconfirmedTx = null;
        if(null == map.get(accountKey)){
            unconfirmedTx = new UnconfirmedTx( AddressTool.getStringAddressByBytes(coinFrom.getAddress()),coinFrom.getAssetsChainId(),coinFrom.getAssetsId());
            unconfirmedTx.setTxHash(txHash);
        }else{
            unconfirmedTx = map.get(accountKey);
        }
//        unconfirmedTx.getSpendAmount().add(coinFrom.getAmount());
        unconfirmedTx.setSpendAmount(unconfirmedTx.getSpendAmount().add(coinFrom.getAmount()));
        map.put(accountKey,unconfirmedTx);
    }
    public  static void  calTxToAmount(Map<String,UnconfirmedTx> map, CoinTo coinTo,String txHash,String accountKey){
        UnconfirmedTx unconfirmedTx = null;
        if(null == map.get(accountKey)){
            unconfirmedTx = new UnconfirmedTx( AddressTool.getStringAddressByBytes(coinTo.getAddress()),coinTo.getAssetsChainId(),coinTo.getAssetsId());
            unconfirmedTx.setTxHash(txHash);
        }else{
            unconfirmedTx = map.get(accountKey);
        }
        unconfirmedTx.setEarnAmount(unconfirmedTx.getEarnAmount().add(coinTo.getAmount()));
        map.put(accountKey,unconfirmedTx);

    }
}
