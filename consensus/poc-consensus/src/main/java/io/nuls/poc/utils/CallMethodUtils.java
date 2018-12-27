package io.nuls.poc.utils;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;

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
            Map<String,Object> callParams = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
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
                Map<String,Object> callParams = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
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
            Map<String,Object> callParams = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
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
}
