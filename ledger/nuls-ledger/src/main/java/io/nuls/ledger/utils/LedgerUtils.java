package io.nuls.ledger.utils;

import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lanjinsheng on 2019/01/02
 */
public class LedgerUtils {
    static final Logger logger = LoggerFactory.getLogger(LedgerUtils.class);

    /**
     * rockdb key
     *
     * @param address
     * @param assetId
     * @return
     */
    public static String getKey(String address, int chainId, int assetId) {
       return  address + "-" + chainId + "-" + assetId;

    }

    public static int getCoinDataType(int txType){
        //TODO:进行类型的判断
        if(txType == TransactionType.TX_TYPE_CANCEL_DEPOSIT.getValue()){
            return LedgerConstant.UNLOCK_TX;
        }
        if(txType == TransactionType.TX_TYPE_STOP_AGENT.getValue()){
            return LedgerConstant.UNLOCK_TX;
        }
        if(txType == TransactionType.TX_TYPE_DESTROY_ASSET_AND_CHAIN.getValue()){
            return LedgerConstant.UNLOCK_TX;
        }
        if(txType == TransactionType.TX_TYPE_REMOVE_ASSET_FROM_CHAIN.getValue()){
            return LedgerConstant.UNLOCK_TX;
        }
        return LedgerConstant.COMMONT_TX;
    }
}
