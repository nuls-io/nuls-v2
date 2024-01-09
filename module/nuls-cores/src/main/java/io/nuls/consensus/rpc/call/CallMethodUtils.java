package io.nuls.consensus.rpc.call;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.*;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.dto.CmdRegisterDto;
import io.nuls.consensus.utils.compare.BlockHeaderComparator;

import java.util.*;

/**
 * 公共远程方法调用工具类
 * Common Remote Method Call Tool Class
 *
 * @author tag
 * 2018/12/26
 */
public class CallMethodUtils {
    public static final long MIN_PACK_SURPLUS_TIME = 2000;
    public static final long TIME_OUT = 1000;

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
            callParams.put(Constants.CHAIN_ID, chainId);
            callParams.put("address", address);
            callParams.put("password", password);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getPriKeyByAddress", callParams);
            if (!cmdResp.isSuccess()) {
                throw new NulsException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            HashMap callResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
            if (callResult == null || callResult.size() == 0) {
                throw new NulsException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            return callResult;
        } catch (NulsException e) {
            throw e;
        }catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 创建多签账户
     * Create multi sign account
     *
     * @param chainId
     * @param
     * @return validate result
     */
    public static String createMultiSignAccount(int chainId, MultiSignTxSignature signTxSignature) throws NulsException {
        try {
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put(Constants.CHAIN_ID, chainId);
            callParams.put("minSigns", signTxSignature.getM());
            List<String> pubKeys = new ArrayList<>();
            for (byte[] pubKey : signTxSignature.getPubKeyList()){
                pubKeys.add(HexUtil.encode(pubKey));
            }
            callParams.put("pubKeys", pubKeys);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createMultiSignAccount", callParams);
            if (!cmdResp.isSuccess()) {
                throw new NulsException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            HashMap callResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createMultiSignAccount");
            if (callResult == null || callResult.size() == 0) {
                throw new NulsException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            return (String) callResult.get("address");
        } catch (NulsException e) {
            throw e;
        }catch (Exception e) {
            throw new NulsException(ConsensusErrorCode.INTERFACE_CALL_FAILED);
        }
    }

    /**
     * 查询多签账户信息
     * Query for multi-signature account information
     *
     * @param chainId
     * @param address
     * @return validate result
     */
    public static MultiSigAccount getMultiSignAccount(int chainId, String address) throws NulsException {
        try {
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put(Constants.CHAIN_ID, chainId);
            callParams.put("address", address);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getMultiSignAccount", callParams);
            if (!cmdResp.isSuccess()) {
                throw new NulsException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            HashMap callResult = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getMultiSignAccount");
            if (callResult == null || callResult.size() == 0) {
                throw new NulsException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            MultiSigAccount multiSigAccount = new MultiSigAccount();
            multiSigAccount.parse(RPCUtil.decode((String) callResult.get("value")),0);
            return multiSigAccount;
        } catch (NulsException e) {
            throw e;
        }catch (Exception e) {
            throw new NulsException(ConsensusErrorCode.INTERFACE_CALL_FAILED);
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
                callParams.put(Constants.CHAIN_ID, chainId);
                callParams.put("address", address);
                callParams.put("password", password);
                callParams.put("data", RPCUtil.encode(tx.getHash().getBytes()));
                Response signResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", callParams);
                if (!signResp.isSuccess()) {
                    throw new NulsException(ConsensusErrorCode.TX_SIGNTURE_ERROR);
                }
                HashMap signResult = (HashMap) ((HashMap) signResp.getResponseData()).get("ac_signDigest");
                p2PHKSignature.parse(RPCUtil.decode((String) signResult.get("signature")), 0);
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
     * @param chain
     * @param address
     * @param header
     */
    public static void blockSignature(Chain chain, String address, BlockHeader header) throws NulsException {
        try {
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
            callParams.put("address", address);
            callParams.put("password", chain.getConfig().getPassword());
            callParams.put("data", RPCUtil.encode(header.getHash().getBytes()));
            Response signResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signBlockDigest", callParams);
            if (!signResp.isSuccess()) {
                throw new NulsException(ConsensusErrorCode.TX_SIGNTURE_ERROR);
            }
            HashMap signResult = (HashMap) ((HashMap) signResp.getResponseData()).get("ac_signBlockDigest");
            BlockSignature blockSignature = new BlockSignature();
            blockSignature.parse(RPCUtil.decode((String) signResult.get("signature")), 0);
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
     * @param timeOut 接口超时时间
     * @return Successful Sending
     */
    @SuppressWarnings("unchecked")
    public static void  receivePackingBlock(int chainId, String block,long timeOut) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("block", block);
        try {
            ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "receivePackingBlock", params,timeOut);
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
        callParams.put(Constants.CHAIN_ID, chainId);
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
    public static Map<String, Object> getBalanceAndNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("assetChainId", assetChainId);
        params.put("address", address);
        params.put("assetId", assetId);
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
    public static Map<String, Object> getBalance(Chain chain, String address,int assetChainId,int assetId) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("assetChainId", assetChainId);
        params.put("address", address);
        params.put("assetId", assetId);
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
     * 获取打包交易
     * Getting Packaged Transactions
     *
     * @param chain chain info
     */
    @SuppressWarnings("unchecked")
    public static Map<String,Object> getPackingTxList(Chain chain, long blockTime, String packingAddress) {
        try {
            long realTime = blockTime * 1000;
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
            long currentTime = NulsDateUtils.getCurrentTimeMillis();
            long surplusTime = realTime - currentTime;
            if(surplusTime <= MIN_PACK_SURPLUS_TIME){
                return null;
            }
            params.put("endTimestamp", realTime - TIME_OUT);
            params.put("maxTxDataSize", chain.getConfig().getBlockMaxSize());
            params.put("blockTime", blockTime);
            params.put("packingAddress", packingAddress);
            BlockExtendsData preExtendsData = chain.getNewestHeader().getExtendsData();
            byte[] preStateRoot = preExtendsData.getStateRoot();
            params.put("preStateRoot", RPCUtil.encode(preStateRoot));
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_packableTxs", params,surplusTime-TIME_OUT);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Packaging transaction acquisition failure!");
                return null;
            }
            return (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_packableTxs");

        } catch (Exception e) {
            chain.getLogger().error(e);
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
            params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
            params.put("txHash", txHash);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_getConfirmedTx", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Acquisition transaction failed！");
                return null;
            }
            Map responseData = (Map) cmdResp.getResponseData();
            Map realData = (Map) responseData.get("tx_getConfirmedTx");
            if(realData.get("tx") == null){
                return null;
            }
            String txHex = (String) realData.get("tx");
            Transaction tx = new Transaction();
            if (!StringUtils.isBlank(txHex)) {
                tx.parse(RPCUtil.decode(txHex), 0);
            }else{
                return null;
            }
            return tx;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return null;
        }
    }


    /**
     * 将新创建的交易发送给交易管理模块
     * The newly created transaction is sent to the transaction management module
     *
     * @param chain chain info
     * @param tx transaction hex
     */
    @SuppressWarnings("unchecked")
    public static void sendTx(Chain chain, String tx) throws NulsException{
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("tx", tx);
        try {
            /*boolean ledgerValidResult = commitUnconfirmedTx(chain,tx);
            if(!ledgerValidResult){
                throw new NulsException(ConsensusErrorCode.FAILED);
            }*/
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Transaction failed to send!");
                //rollBackUnconfirmTx(chain,tx);
                throw new NulsException(ConsensusErrorCode.FAILED);
            }
        }catch (NulsException e){
            throw e;
        }catch (Exception e) {
            chain.getLogger().error(e);
        }
    }

    /**
     * 共识状态修改通知交易模块
     * Consensus status modification notification transaction module
     *
     * @param chain   chain info
     * @param packing packing state
     */
    @SuppressWarnings("unchecked")
    public static void sendState(Chain chain, boolean packing) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
            params.put("packaging", packing);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_cs_state", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Packing state failed to send!");
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
    }

    /**
     * 批量验证交易
     *
     * @param chainId      链Id/chain id
     * @param transactions
     * @return
     */
    public static Response verify(int chainId, List<Transaction> transactions, BlockHeader header, BlockHeader lastHeader, NulsLogger logger) {
        try {
            Map<String, Object> params = new HashMap<>(2);
            params.put(Constants.CHAIN_ID, chainId);
            List<String> txList = new ArrayList<>();
            for (Transaction transaction : transactions) {
                txList.add(RPCUtil.encode(transaction.serialize()));
            }
            params.put("txList", txList);
            BlockExtendsData lastData = lastHeader.getExtendsData();
            params.put("preStateRoot", RPCUtil.encode(lastData.getStateRoot()));
            params.put("blockHeader", RPCUtil.encode(header.serialize()));
            return ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_batchVerify", params, 10 * 60 * 1000);
        } catch (Exception e) {
            logger.error("", e);
            return null;
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

    /**
     * 查询本地加密账户
     * Search for Locally Encrypted Accounts
     */
    @SuppressWarnings("unchecked")
    public static List<byte[]> getEncryptedAddressList(Chain chain) {
        List<byte[]> packingAddressList = new ArrayList<>();
        try {
            Map<String, Object> params = new HashMap<>(2);
            params.put(ConsensusConstant.PARAM_CHAIN_ID, chain.getConfig().getChainId());
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getEncryptedAddressList", params);
            List<String> accountAddressList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getEncryptedAddressList")).get("list");
            if (accountAddressList != null && accountAddressList.size() > 0) {
                for (String address : accountAddressList) {
                    packingAddressList.add(AddressTool.getAddress(address));
                }
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
        return packingAddressList;
    }

    /**
     * 查询账户别名
     * Query account alias
     * */
    public static String getAlias(Chain chain,String address){
        String alias = null ;
        try {
            Map<String, Object> params = new HashMap<>(2);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
            params.put("address", address);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getAliasByAddress", params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getAliasByAddress");
            String paramAlias = "alias";
            if(result.get(paramAlias) != null){
                alias = (String) result.get("alias");
            }
        }catch (Exception e){
            chain.getLogger().error(e);
        }
        return alias;
    }

    /**
     * 初始化链区块头数据，缓存指定数量的区块头
     * Initialize chain block header entity to cache a specified number of block headers
     *
     * @param chain chain info
     */
    @SuppressWarnings("unchecked")
    public static void loadBlockHeader(Chain chain)throws Exception{
        Map params = new HashMap(ConsensusConstant.INIT_CAPACITY);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("round", ConsensusConstant.INIT_BLOCK_HEADER_COUNT);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "getLatestRoundBlockHeaders", params);
        Map<String, Object> responseData;
        List<String> blockHeaderHexs = new ArrayList<>();
        if (response.isSuccess()) {
            responseData = (Map<String, Object>) response.getResponseData();
            Map result = (Map) responseData.get("getLatestRoundBlockHeaders");
            blockHeaderHexs = (List<String>) result.get("value");
        }
        while (!response.isSuccess() && blockHeaderHexs.size() == 0) {
            response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "getLatestRoundBlockHeaders", params);
            if (response.isSuccess()) {
                responseData = (Map<String, Object>) response.getResponseData();
                Map result = (Map) responseData.get("getLatestRoundBlockHeaders");
                blockHeaderHexs = (List<String>) result.get("value");
                break;
            }
            Log.debug("---------------------------区块加载失败！");
            Thread.sleep(1000);
        }
        List<BlockHeader> blockHeaders = new ArrayList<>();
        for (String blockHeaderHex : blockHeaderHexs) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(RPCUtil.decode(blockHeaderHex), 0);
            blockHeaders.add(blockHeader);
        }
        blockHeaders.sort(new BlockHeaderComparator());
        chain.setBlockHeaderList(blockHeaders);
        chain.setNewestHeader(blockHeaders.get(blockHeaders.size() - 1));
        Log.debug("---------------------------区块加载成功！");
    }


    /**
     * 初始化链区块头数据，缓存指定数量的区块头
     * Initialize chain block header entity to cache a specified number of block headers
     *
     * @param chain chain info
     */
    @SuppressWarnings("unchecked")
    public static void getRoundBlockHeaders(Chain chain,long roundCount,long startHeight)throws Exception{
        Map params = new HashMap(ConsensusConstant.INIT_CAPACITY);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("round", roundCount);
        params.put("height", startHeight);
        Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "getRoundBlockHeaders", params);
        Map<String, Object> responseData;
        List<String> blockHeaderHexs = new ArrayList<>();
        if (response.isSuccess()) {
            responseData = (Map<String, Object>) response.getResponseData();
            Map result = (Map) responseData.get("getRoundBlockHeaders");
            blockHeaderHexs = (List<String>) result.get("value");
        }
        int tryCount = 0;
        while (!response.isSuccess() && blockHeaderHexs.size() == 0 && tryCount < ConsensusConstant.RPC_CALL_TRY_COUNT) {
            response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "getRoundBlockHeaders", params);
            if (response.isSuccess()) {
                responseData = (Map<String, Object>) response.getResponseData();
                Map result = (Map) responseData.get("getRoundBlockHeaders");
                blockHeaderHexs = (List<String>) result.get("value");
                break;
            }
            tryCount++;
            Log.debug("---------------------------回滚区块轮次变化从新加载区块失败！");
            Thread.sleep(1000);
        }
        List<BlockHeader> blockHeaders = new ArrayList<>();
        for (String blockHeaderHex : blockHeaderHexs) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(RPCUtil.decode(blockHeaderHex), 0);
            blockHeaders.add(blockHeader);
        }
        blockHeaders.sort(new BlockHeaderComparator());
        chain.getBlockHeaderList().addAll(0, blockHeaders);
        Log.debug("---------------------------回滚区块轮次变化从新加载区块成功！");
    }


    /**
     * 验证交易CoinData
     * Verifying transactions CoinData
     *
     * @param chain chain info
     * @param tx transaction hex
     */
    @SuppressWarnings("unchecked")
    public static boolean commitUnconfirmedTx(Chain chain, String tx){
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("tx", tx);
        try {
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Ledger module verifies transaction failure!");
                return false;
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("commitUnconfirmedTx");
            int validateCode = (int)result.get("validateCode");
            if(validateCode == 1){
                return true;
            }else{
                chain.getLogger().info("Ledger module verifies transaction failure,error info:"+ result.get("validateDesc"));
                return false;
            }
        }catch (Exception e){
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 回滚交易在账本模块的记录
     * Rollback transactions recorded in the book module
     *
     * @param chain chain info
     * @param tx transaction hex
     */
    @SuppressWarnings("unchecked")
    public static boolean rollBackUnconfirmTx(Chain chain, String tx){
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("tx", tx);
        try {
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "rollBackUnconfirmTx", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Ledger module rollBack transaction failure!");
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("rollBackUnconfirmTx");
            int validateCode = (int)result.get("value");
            if(validateCode == 1){
                return true;
            }else{
                chain.getLogger().info("Ledger module rollBack transaction failure!");
                return false;
            }
        }catch (Exception e){
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 交易基础验证
     * Transaction Basis Verification
     * @param chain chain info
     * @param tx transaction hex
     * */
    @SuppressWarnings("unchecked")
    public static boolean transactionBasicValid(Chain chain,String tx){
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("tx", tx);
        try {
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_baseValidateTx", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Failure of transaction basic validation!");
                return false;
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("tx_baseValidateTx");
            return (boolean)result.get("value");
        }catch (Exception e){
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 获取主网节点版本
     * Acquire account lock-in amount and available balance
     *
     * @param chainId
     */
    @SuppressWarnings("unchecked")
    public static Map getVersion(int chainId) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.PU.abbr, "getVersion", params);
            if (!callResp.isSuccess()) {
                return null;
            }
            return (Map) ((Map) callResp.getResponseData()).get("getVersion");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 注册智能合约交易
     * Acquire account lock-in amount and available balance
     *
     * @param chainId
     */
    @SuppressWarnings("unchecked")
    public static boolean registerContractTx(int chainId,List<CmdRegisterDto> cmdRegisterDtoList) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("moduleCode", ModuleE.CS.abbr);
        params.put("cmdRegisterList", cmdRegisterDtoList);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, "sc_register_cmd_for_contract", params);
            return callResp.isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 触发CoinBase智能合约
     * Acquire account lock-in amount and available balance
     *
     * @param chainId
     */
    @SuppressWarnings("unchecked")
    public static String triggerContract(int chainId,String stateRoot,long height,String contractAddress,String coinBaseTx) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("stateRoot", stateRoot);
        params.put("blockHeight", height);
        params.put("contractAddress", contractAddress);
        params.put("tx", coinBaseTx);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, "sc_trigger_payable_for_consensus_contract", params);
            if (!callResp.isSuccess()) {
                return null;
            }
            HashMap result = (HashMap) ((HashMap) callResp.getResponseData()).get("sc_trigger_payable_for_consensus_contract");
            return (String) result.get("value");
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
}
