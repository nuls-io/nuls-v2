package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.ledger.model.UnconfirmedTx;
import io.nuls.ledger.model.po.UnconfirmedNonce;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.List;
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

    public static List<UnconfirmedNonce> getConfirmedNonce(String nonce,List<UnconfirmedNonce> unconfirmedNonces) {
        if (unconfirmedNonces.size() > 0) {
            int clearIndex = 0;
            boolean hadClear = false;
            for(UnconfirmedNonce unconfirmedNonce : unconfirmedNonces){
                clearIndex++;
                if(unconfirmedNonce.getNonce().equalsIgnoreCase(nonce)){
                    hadClear=true;
                    break;
                }
            }
            int size = unconfirmedNonces.size();
            //从第list的index=i-1起进行清空
            if(hadClear) {
                LoggerUtil.logger.debug("remove clearIndex = {}",clearIndex);
                List<UnconfirmedNonce> leftUnconfirmedNonces = unconfirmedNonces.subList(clearIndex,size);
                return leftUnconfirmedNonces;
            }else {
                //分叉了，清空之前的未提交nonce
                LoggerUtil.logger.debug("remove all");
                return new ArrayList<>();
            }

        } else {
            return unconfirmedNonces;
        }
    }
}
