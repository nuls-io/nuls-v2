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
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
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
 * */
public class CallMethodUtils {

    /**
     * 账户验证
     * account validate
     *
     * @param chainId
     * @param address
     * @param password
     * @return  validate result
     * */
    public static HashMap accountValid(int chainId, String address, String password)throws NulsException {
        try {
            Map<String,Object> callParams = new HashMap<>(4);
            callParams.put("chainId",chainId);
            callParams.put("address",address);
            callParams.put("password",password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr,"ac_getPriKeyByAddress", callParams);
            if(!cmdResp.isSuccess()){
                throw new NulsException(ConsensusErrorCode.ACCOUNT_NOT_EXIST);
            }
            HashMap callResult = (HashMap)((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
            if(callResult == null || callResult.size() == 0 || !(boolean)callResult.get(ConsensusConstant.VALID_RESULT)){
                throw new NulsException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            return callResult;
        }catch (Exception e){
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
     * */
    public static void transactionSignature(int chainId, String address, String password, String priKey, Transaction tx)throws NulsException {
        try {
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            if(!StringUtils.isBlank(priKey)){
                p2PHKSignature = SignatureUtil.createSignatureByPriKey(tx,priKey);
            }
            else{
                Map<String,Object> callParams = new HashMap<>(4);
                callParams.put("chainId",chainId);
                callParams.put("address",address);
                callParams.put("password",password);
                callParams.put("dataHex", HexUtil.encode(tx.getHash().getDigestBytes()));
                Response signResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr,"ac_signDigest", callParams);
                if(!signResp.isSuccess()){
                    throw new NulsException(ConsensusErrorCode.TX_SIGNTURE_ERROR);
                }
                HashMap signResult = (HashMap)((HashMap) signResp.getResponseData()).get("ac_signDigest");
                p2PHKSignature.parse(HexUtil.decode((String)signResult.get("signatureHex")),0);
            }
            TransactionSignature signature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            p2PHKSignatures.add(p2PHKSignature);
            signature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(signature.serialize());
        }catch (NulsException e){
            throw e;
        }catch (Exception e){
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
     * */
    public static void blockSignature(int chainId, String address, BlockHeader header)throws NulsException{
        try {
            Map<String,Object> callParams = new HashMap<>(4);
            callParams.put("chainId",chainId);
            callParams.put("address",address);
            callParams.put("password",null);
            callParams.put("dataHex", HexUtil.encode(header.getHash().getDigestBytes()));
            Response signResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr,"ac_signBlockDigest", callParams);
            if(!signResp.isSuccess()){
                throw new NulsException(ConsensusErrorCode.TX_SIGNTURE_ERROR);
            }
            HashMap signResult = (HashMap)((HashMap) signResp.getResponseData()).get("ac_signBlockDigest");
            BlockSignature blockSignature = new BlockSignature();
            blockSignature.parse(HexUtil.decode((String)signResult.get("signatureHex")),0);
            header.setBlockSignature(blockSignature);
        }catch (NulsException e){
            throw e;
        } catch (Exception e){
            throw new NulsException(e);
        }
    }

    /**
     * 获取网络节点连接数
     * @param chainId  chain ID
     * @param isCross  是否获取跨链节点连接数/Whether to Get the Number of Connections across Chains
     * @return  int    连接节点数/Number of Connecting Nodes
     * */
    public static int getAvailableNodeAmount(int chainId,boolean isCross)throws NulsException{
        Map<String,Object> callParams = new HashMap<>(4);
        callParams.put("chainId",chainId);
        callParams.put("isCross",isCross);
        try{
            Response callResp = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr,"nw_getChainConnectAmount", callParams);
            if(!callResp.isSuccess()){
                throw new NulsException(ConsensusErrorCode.INTERFACE_CALL_FAILED);
            }
            HashMap callResult = (HashMap)((HashMap) callResp.getResponseData()).get("nw_getChainConnectAmount");
            return (Integer) callResult.get("connectAmount");
        }catch (Exception e){
            throw new NulsException(e);
        }
    }

    /**
     * 将打包的新区块发送给区块管理模块
     * @param chainId  chain ID
     * @param block    new block Info
     * @return         Successful Sending
     * */
    @SuppressWarnings("unchecked")
    public static boolean receivePackingBlock(int chainId,String block)throws NulsException{
        Map<String,Object> params = new HashMap(4);
        params.put("chainId",chainId);
        params.put("block", block);
        try {
            Response callResp = CmdDispatcher.requestAndResponse(ModuleE.BL.abbr,"receivePackingBlock", params);
            return callResp.isSuccess();
        }catch (Exception e){
            throw new NulsException(e);
        }
    }

    /**
     * 获取可用余额和nonce
     * Get the available balance and nonce
     * @param chain
     * @param address
     * */
    @SuppressWarnings("unchecked")
    public Map<String,Object> getBalanceAndNonce(Chain chain,String address)throws NulsException{
        Map<String,Object> params = new HashMap(4);
        params.put("chainId",chain.getConfig().getChainId());
        params.put("assetChainId", chain.getConfig().getChainId());
        params.put("address", address);
        params.put("assetId", chain.getConfig().getAssetsId());
        try {
            Response callResp = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr,"getBalanceNonce", params);
            if(!callResp.isSuccess()){
                return null;
            }
            return (HashMap)((HashMap) callResp.getResponseData()).get("getBalanceNonce");
        }catch (Exception e){
            throw new NulsException(e);
        }
    }

    /**
     * 获取账户锁定金额和可用余额
     * Acquire account lock-in amount and available balance
     * @param chain
     * @param address
     * */
    @SuppressWarnings("unchecked")
    public Map<String,Object> getBalance(Chain chain,String address)throws NulsException{
        Map<String,Object> params = new HashMap(4);
        params.put("chainId",chain.getConfig().getChainId());
        params.put("assetChainId", chain.getConfig().getChainId());
        params.put("address", address);
        params.put("assetId", chain.getConfig().getAssetsId());
        try {
            Response callResp = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr,"getBalance", params);
            if(!callResp.isSuccess()){
                return null;
            }
            return (HashMap)((HashMap) callResp.getResponseData()).get("getBalance");
        }catch (Exception e){
            throw new NulsException(e);
        }
    }

    /**
     * 获取当前网络时间
     * Get the current network time
     * */
    public static long currentTime() {
        try {
            Response response = CmdDispatcher.requestAndResponse(ModuleE.NW.abbr, "nw_currentTimeMillis", null);
            Map responseData = (Map) response.getResponseData();
            Map result = (Map) responseData.get("nw_currentTimeMillis");
            return (Long) result.get("currentTimeMillis");
        } catch (Exception e) {
            Log.error("get nw_currentTimeMillis fail");
        }
        return System.currentTimeMillis();
    }
}
