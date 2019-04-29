package io.nuls.transaction.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.BigIntegerUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyLedgerResult;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class LedgerCall {


    /**
     * 验证单个交易的CoinData(外部使用)
     * 有异常直接抛出
     *
     * @param chain
     * @param tx
     * @return
     */
    public static VerifyLedgerResult verifyCoinData(Chain chain, String tx) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("tx", tx);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.LG.abbr,"verifyCoinData", params);
            return VerifyLedgerResult.success((boolean)result.get("orphan"));
        } catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e.getErrorCode().getCode());
            return VerifyLedgerResult.fail(e.getErrorCode());
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return VerifyLedgerResult.fail(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }



    /**
     * 验证单个交易与未确认交易提交
     * @param chain
     * @param txStr
     */
    public static VerifyLedgerResult commitUnconfirmedTx(Chain chain, String txStr) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("tx", txStr);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
            return VerifyLedgerResult.success((boolean)result.get("orphan"));
        } catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e.getErrorCode().getCode());
            return VerifyLedgerResult.fail(e.getErrorCode());
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return VerifyLedgerResult.fail(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    /**
     * 批量验证CoinData时的单个发送(不用于单个交易的独立验证)
     * @param chain
     * @param tx
     * @return
     */
    public static VerifyLedgerResult verifyCoinDataPackaged(Chain chain, String tx) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("tx", tx);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.LG.abbr,"verifyCoinDataPackaged", params);
            return VerifyLedgerResult.success((boolean)result.get("orphan"));
        } catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e.getErrorCode().getCode());
            return VerifyLedgerResult.fail(e.getErrorCode());
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return VerifyLedgerResult.fail(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 验证区块中的交易CoinData
     * @param chain
     * @param txList
     * @param blockHeight
     * @return
     * @throws NulsException
     */
    public static boolean verifyBlockTxsCoinData(Chain chain, List<String> txList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txList", txList);
            params.put("blockHeight", blockHeight);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "blockValidate", params);
            return (boolean) result.get("value");
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 查询nonce值
     *
     * @param chain
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     * @throws NulsException
     */
    public static byte[] getNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("address", address);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.LG.abbr, "getNonce", params);
            String nonce = (String) result.get("nonce");
            return RPCUtil.decode(nonce);
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 查询账户特定资产的余额(只获取已确认的余额)
     * Check the balance of an account-specific asset
     */
    public static BigInteger getBalance(Chain chain, byte[] address, int assetChainId, int assetId) throws NulsException {
        try {
            String addressString = AddressTool.getStringAddressByBytes(address);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", addressString);
            Map result = (Map)TransactionCall.request(ModuleE.LG.abbr, "getBalance", params);
            Object available = result.get("available");
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 开始批量验证coindata的通知
     * @param chain
     * @return
     * @throws NulsException
     */
    public static boolean coinDataBatchNotify(Chain chain) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "batchValidateBegin", params);
            return (boolean) result.get("value");
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    /**
     * 提交已确认交易给账本
     * @param chain
     * @param txList
     */
    public static boolean commitTxsLedger(Chain chain, List<String> txList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txList", txList);
            params.put("blockHeight", blockHeight);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "commitBlockTxs", params);
            return (boolean) result.get("value");
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    /**
     * 调用账本修改未确认的交易状态
     * @param chain
     * @param txStr
     */
    public static boolean rollbackTxValidateStatus(Chain chain, String txStr) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("tx", txStr);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "rollbackTxValidateStatus", params);
            return (boolean) result.get("value");
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 调用账本回滚未确认的交易
     * @param chain
     * @param txStr
     */
    public static boolean rollBackUnconfirmTx(Chain chain, String txStr) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("tx", txStr);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "rollBackUnconfirmTx", params);
            return (boolean) result.get("value");
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 调用账本回滚已确认的交易
     * @param chain
     * @param txList
     */
    public static boolean rollbackTxsLedger(Chain chain, List<String> txList, Long blockHeight) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("txList", txList);
            params.put("blockHeight", blockHeight);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.LG.abbr, "rollBackBlockTxs", params);
            return (boolean) result.get("value");
        } catch (RuntimeException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }



}
