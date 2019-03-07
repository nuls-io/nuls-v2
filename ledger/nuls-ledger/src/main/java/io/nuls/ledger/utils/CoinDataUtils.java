package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Coin;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.ledger.model.UnconfirmedTx;
import io.nuls.ledger.model.po.UnconfirmedAmount;
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
        if (null == stream) {
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

    public static void calTxFromAmount(Map<String, UnconfirmedTx> map, CoinFrom coinFrom, String txHash, String accountKey) {
        UnconfirmedTx unconfirmedTx = getUnconfirmedTx(map, coinFrom, txHash, accountKey);
        unconfirmedTx.setSpendAmount(unconfirmedTx.getSpendAmount().add(coinFrom.getAmount()));
        map.put(accountKey, unconfirmedTx);
    }

    public static void calTxToAmount(Map<String, UnconfirmedTx> map, CoinTo coinTo, String txHash, String accountKey) {
        UnconfirmedTx unconfirmedTx = getUnconfirmedTx(map, coinTo, txHash, accountKey);
        unconfirmedTx.setEarnAmount(unconfirmedTx.getEarnAmount().add(coinTo.getAmount()));
        map.put(accountKey, unconfirmedTx);

    }

    /**
     * 未确认交易解锁from的计算
     *
     * @param map
     * @param coinFrom
     * @param txHash
     * @param accountKey
     */
    public static void calTxFromUnlockedAmount(Map<String, UnconfirmedTx> map, CoinFrom coinFrom, String txHash, String accountKey) {
        UnconfirmedTx unconfirmedTx = getUnconfirmedTx(map, coinFrom, txHash, accountKey);
        unconfirmedTx.setFromUnLockedAmount(unconfirmedTx.getFromUnLockedAmount().add(coinFrom.getAmount()));
        map.put(accountKey, unconfirmedTx);
    }
    public static void calTxToLockedAmount(Map<String, UnconfirmedTx> map, CoinTo coinTo, String txHash, String accountKey) {
        UnconfirmedTx unconfirmedTx = getUnconfirmedTx(map, coinTo, txHash, accountKey);
        unconfirmedTx.setToLockedAmount(unconfirmedTx.getToLockedAmount().add(coinTo.getAmount()));
        map.put(accountKey, unconfirmedTx);

    }

    public static UnconfirmedTx getUnconfirmedTx(Map<String, UnconfirmedTx> map, Coin coin, String txHash, String accountKey) {
        UnconfirmedTx unconfirmedTx = null;
        if (null == map.get(accountKey)) {
            unconfirmedTx = new UnconfirmedTx(AddressTool.getStringAddressByBytes(coin.getAddress()), coin.getAssetsChainId(), coin.getAssetsId());
            unconfirmedTx.setTxHash(txHash);
        } else {
            unconfirmedTx = map.get(accountKey);
        }
        return unconfirmedTx;
    }

   public static List<UnconfirmedNonce> getUnconfirmedNonces(String nonce, List<UnconfirmedNonce> unconfirmedNonces) {
        if (unconfirmedNonces.size() > 0) {
            int clearIndex = 0;
            boolean hadClear = false;
            for (UnconfirmedNonce unconfirmedNonce : unconfirmedNonces) {
                clearIndex++;
                if (unconfirmedNonce.getNonce().equalsIgnoreCase(nonce)) {
                    hadClear = true;
                    break;
                }
            }
            int size = unconfirmedNonces.size();
            //从第list的index=i-1起进行清空
            if (hadClear) {
                LoggerUtil.logger.debug("remove clearIndex = {}", clearIndex);
                List<UnconfirmedNonce> leftUnconfirmedNonces = unconfirmedNonces.subList(clearIndex, size);
                return leftUnconfirmedNonces;
            } else {
                //分叉了，清空之前的未提交nonce
                LoggerUtil.logger.debug("remove all");
                return new ArrayList<>();
            }

        } else {
            return unconfirmedNonces;
        }
    }

    public static List<UnconfirmedAmount> getUnconfirmedAmounts(String txHash, List<UnconfirmedAmount> unconfirmedAmounts) {
        if (unconfirmedAmounts.size() > 0) {
            int clearIndex = 0;
            boolean hadClear = false;
            for (UnconfirmedAmount unconfirmedAmount : unconfirmedAmounts) {
                clearIndex++;
                if (unconfirmedAmount.getTxHash().equalsIgnoreCase(txHash)) {
                    hadClear = true;
                    break;
                }
            }
            int size = unconfirmedAmounts.size();
            //从第list的index=i-1起进行清空
            if (hadClear) {
                LoggerUtil.logger.debug("remove UnconfirmedAmount clearIndex = {}", clearIndex);
                List<UnconfirmedAmount> leftUnconfirmedAmounts = unconfirmedAmounts.subList(clearIndex, size);
                return leftUnconfirmedAmounts;
            } else {
                //分叉了，清空之前的未提交nonce
                LoggerUtil.logger.debug("remove all");
                return new ArrayList<>();
            }

        } else {
            return unconfirmedAmounts;
        }
    }
}
