package io.nuls.transaction.rpc.call;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class ChainCall {


    /**
     * 验证跨链交易coinData
     * @param coinDataHex
     * @return boolean
     * @throws NulsException
     */
    public static boolean verifyCtxCoinData(String coinDataHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("coinDatas", coinDataHex);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.CM.abbr,"cm_assetCirculateValidator",  params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 主网接收到一个友链跨链交易, 对转出者链进行账目金额扣除
     * @param coinDataHex
     * @return
     * @throws NulsException
     */
    public static boolean ctxChainLedgerCommit(String coinDataHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("coinDatas", coinDataHex);
            //todo cmd待确认
            HashMap result = (HashMap) TransactionCall.request(ModuleE.CM.abbr,"cm_assetCirculateValidator", params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 主网发送出一个跨链交易至友链, 对接收者链进行账目金额增加
     * @param coinDataHex
     * @return
     * @throws NulsException
     */
    public static boolean ctxChainLedgerRollback(String coinDataHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("coinDatas", coinDataHex);
            //todo cmd待确认
            HashMap result = (HashMap) TransactionCall.request(ModuleE.CM.abbr,"cm_assetCirculateCommit", params);
            return (int) result.get("value") == 1;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
