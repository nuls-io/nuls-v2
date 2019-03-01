package io.nuls.poc.utils;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.TxRegisterDetail;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共远程方法调用工具类
 * Common Remote Method Call Tool Class
 *
 * @author tag
 * 2018/12/26
 */
public class CallMethodUtils {

    /**
     * 账户验证
     * account validate
     *
     * @param chainId
     * @param address
     * @param password
     * @return validate result
     */
    public static HashMap accountValid(int chainId, String address, String password) throws NulsException {
        try {
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put("chainId", chainId);
            callParams.put("address", address);
            callParams.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", callParams);
            if (!cmdResp.isSuccess()) {
                throw new NulsException(ConsensusErrorCode.ACCOUNT_NOT_EXIST);
            }
            HashMap callResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
            if (callResult == null || callResult.size() == 0 || !(boolean) callResult.get(ConsensusConstant.VALID_RESULT)) {
                throw new NulsException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            return callResult;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    /**
     * 交易签名
     * transaction signature
     *
     * @param chainId
     * @param address
     * @param password
     * @param priKey
     * @param tx
     */
    public static void transactionSignature(int chainId, String address, String password, String priKey, Transaction tx) throws NulsException {
        try {
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            if (!StringUtils.isBlank(priKey)) {
                p2PHKSignature = SignatureUtil.createSignatureByPriKey(tx, priKey);
            } else {
                Map<String, Object> callParams = new HashMap<>(4);
                callParams.put("chainId", chainId);
                callParams.put("address", address);
                callParams.put("password", password);
                callParams.put("dataHex", HexUtil.encode(tx.getHash().getDigestBytes()));
                Response signResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", callParams);
                if (!signResp.isSuccess()) {
                    throw new NulsException(ConsensusErrorCode.TX_SIGNTURE_ERROR);
                }
                HashMap signResult = (HashMap) ((HashMap) signResp.getResponseData()).get("ac_signDigest");
                p2PHKSignature.parse(HexUtil.decode((String) signResult.get("signatureHex")), 0);
            }
            TransactionSignature signature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            p2PHKSignatures.add(p2PHKSignature);
            signature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(signature.serialize());
        } catch (NulsException e) {
            throw e;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 区块签名
     * block signature
     *
     * @param chainId
     * @param address
     * @param header
     */
    public static void blockSignature(int chainId, String address, BlockHeader header) throws NulsException {
        try {
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put("chainId", chainId);
            callParams.put("address", address);
            callParams.put("password", null);
            callParams.put("dataHex", HexUtil.encode(header.getHash().getDigestBytes()));
            Response signResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signBlockDigest", callParams);
            if (!signResp.isSuccess()) {
                throw new NulsException(ConsensusErrorCode.TX_SIGNTURE_ERROR);
            }
            HashMap signResult = (HashMap) ((HashMap) signResp.getResponseData()).get("ac_signBlockDigest");
            BlockSignature blockSignature = new BlockSignature();
            blockSignature.parse(HexUtil.decode((String) signResult.get("signatureHex")), 0);
            header.setBlockSignature(blockSignature);
        } catch (NulsException e) {
            throw e;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 将打包的新区块发送给区块管理模块
     *
     * @param chainId chain ID
     * @param block   new block Info
     * @return Successful Sending
     */
    @SuppressWarnings("unchecked")
    public static boolean receivePackingBlock(int chainId, String block) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put("chainId", chainId);
        params.put("block", block);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "receivePackingBlock", params);
            return callResp.isSuccess();
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 获取网络节点连接数
     *
     * @param chainId chain ID
     * @param isCross 是否获取跨链节点连接数/Whether to Get the Number of Connections across Chains
     * @return int    连接节点数/Number of Connecting Nodes
     */
    public static int getAvailableNodeAmount(int chainId, boolean isCross) throws NulsException {
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put("chainId", chainId);
        callParams.put("isCross", isCross);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_getChainConnectAmount", callParams);
            if (!callResp.isSuccess()) {
                throw new NulsException(ConsensusErrorCode.INTERFACE_CALL_FAILED);
            }
            HashMap callResult = (HashMap) ((HashMap) callResp.getResponseData()).get("nw_getChainConnectAmount");
            return (Integer) callResult.get("connectAmount");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 获取可用余额和nonce
     * Get the available balance and nonce
     *
     * @param chain
     * @param address
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getBalanceAndNonce(Chain chain, String address) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put("chainId", chain.getConfig().getChainId());
        params.put("assetChainId", chain.getConfig().getChainId());
        params.put("address", address);
        params.put("assetId", chain.getConfig().getAssetsId());
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
            if (!callResp.isSuccess()) {
                return null;
            }
            return (HashMap) ((HashMap) callResp.getResponseData()).get("getBalanceNonce");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 获取账户锁定金额和可用余额
     * Acquire account lock-in amount and available balance
     *
     * @param chain
     * @param address
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getBalance(Chain chain, String address) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put("chainId", chain.getConfig().getChainId());
        params.put("assetChainId", chain.getConfig().getChainId());
        params.put("address", address);
        params.put("assetId", chain.getConfig().getAssetsId());
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalance", params);
            if (!callResp.isSuccess()) {
                return null;
            }
            return (HashMap) ((HashMap) callResp.getResponseData()).get("getBalance");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 获取当前网络时间
     * Get the current network time
     */
    public static long currentTime() {
        try {
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_currentTimeMillis", null);
            Map responseData = (Map) response.getResponseData();
            Map result = (Map) responseData.get("nw_currentTimeMillis");
            return (Long) result.get("currentTimeMillis");
        } catch (Exception e) {
            Log.error("get nw_currentTimeMillis fail");
        }
        return System.currentTimeMillis();
    }

    /**
     * 交易注册
     *
     * @param chain                chain
     * @param txRegisterDetailList 注冊是交易信息/Registration is transaction information
     */
    @SuppressWarnings("unchecked")
    public static boolean registerTx(Chain chain, List<TxRegisterDetail> txRegisterDetailList) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put("chainId", chain.getConfig().getChainId());
            params.put("list", txRegisterDetailList);
            params.put("moduleCode", ModuleE.CS.abbr);
            params.put("moduleValidator", "cs_batchValid");
            params.put("commit", "cs_commit");
            params.put("rollback", "cs_rollback");
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_register", params);
            if (!cmdResp.isSuccess()) {
                chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error("chain ：" + chain.getConfig().getChainId() + " Failure of transaction registration");
                return false;
            }
            return true;
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
            return false;
        }
    }

    /**
     * 获取打包交易
     * Getting Packaged Transactions
     *
     * @param chain chain info
     */
    @SuppressWarnings("unchecked")
    public static List<Transaction> getPackingTxList(Chain chain) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put("chainId", chain.getConfig().getChainId());
            params.put("endTimestamp", currentTime() + ConsensusConstant.GET_TX_MAX_WAIT_TIME);
            params.put("maxTxDataSize", ConsensusConstant.PACK_TX_MAX_SIZE);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_packableTxs", params);
            if (!cmdResp.isSuccess()) {
                chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error("Packaging transaction acquisition failure!");
                return null;
            }
            HashMap signResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_packableTxs");
            List<String> txHexList = (List) signResult.get("list");
            List<Transaction> txList = new ArrayList<>();
            for (String txHex : txHexList) {
                Transaction tx = new Transaction();
                tx.parse(HexUtil.decode(txHex), 0);
                txList.add(tx);
            }
            return txList;
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
            return null;
        }
    }

    /**
     * 获取指定交易
     * Acquisition of transactions based on transactions Hash
     *
     * @param chain  chain info
     * @param txHash transaction hash
     */
    @SuppressWarnings("unchecked")
    public static Transaction getTransaction(Chain chain, String txHash) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put("chainId", chain.getConfig().getChainId());
            params.put("txHash", txHash);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTx", params);
            if (!cmdResp.isSuccess()) {
                chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error("Acquisition transaction failed！");
                return null;
            }
            Map responseData = (Map) cmdResp.getResponseData();
            Transaction tx = new Transaction();
            Map realData = (Map)responseData.get("tx_getConfirmedTx");
            String txHex  = (String)realData.get("txHex");
            if(!StringUtils.isBlank(txHex)){
                tx.parse(HexUtil.decode(txHex),0);
            }
            return tx;
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
            return null;
        }
    }


    /**
     * 将新创建的交易发送给交易管理模块
     * The newly created transaction is sent to the transaction management module
     *
     * @param chain chain info
     * @param txHex transaction hex
     */
    @SuppressWarnings("unchecked")
    public static void sendTx(Chain chain, String txHex) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put("chainId", chain.getConfig().getChainId());
            params.put("txHex", txHex);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            if (!cmdResp.isSuccess()) {
                chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error("Transaction failed to send!");
            }
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
        }
    }

    /**
     * 共识状态修改通知交易模块
     * Consensus status modification notification transaction module
     *
     * @param chain    chain info
     * @param packing  packing state
     */
    @SuppressWarnings("unchecked")
    public static void sendState(Chain chain, boolean packing) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put("chainId", chain.getConfig().getChainId());
            params.put("packaging", packing);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_cs_state", params);
            if (!cmdResp.isSuccess()) {
                chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error("Packing state failed to send!");
            }
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
        }
    }

    /**
     * 根据交易HASH获取NONCE（交易HASH后8位）
     * Obtain NONCE according to HASH (the last 8 digits of HASH)
     */
    public static String getNonce(String txHash) {
        return txHash.substring(txHash.length() - 8);
    }

    /**
     * 根据交易HASH获取NONCE（交易HASH后8位）
     * Obtain NONCE according to HASH (the last 8 digits of HASH)
     */
    public static byte[] getNonce(byte[] txHash) {
        byte[] targetArr = new byte[8];
        System.arraycopy(txHash, txHash.length - 8, targetArr, 0, 8);
        return targetArr;
    }
}
