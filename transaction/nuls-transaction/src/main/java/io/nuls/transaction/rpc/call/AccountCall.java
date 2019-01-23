package io.nuls.transaction.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.utils.TxUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class AccountCall {

    /**
     * 查询地址私钥
     * Query address private key
     */
    public static String getPrikey(String address, String password) throws NulsException {
        try {
            int chainId = AddressTool.getChainIdByAddress(address);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
            return (String) result.get("priKey");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 查询多签账户
     * Query multi-sign account
     *
     * @param multiSignAddress
     * @return
     */
    public static MultiSigAccount getMultiSigAccount(byte[] multiSignAddress) throws NulsException {
        try {
            MultiSigAccount multiSigAccount = null;
            String address = AddressTool.getStringAddressByBytes(multiSignAddress);
            int chainId = AddressTool.getChainIdByAddress(address);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("address", address);

            Object result = TransactionCall.request(ModuleE.AC.abbr, "ac_getMultiSigAccount", params);
            multiSigAccount = JSONUtils.json2pojo(JSONUtils.obj2json(result), MultiSigAccount.class);
            return multiSigAccount;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 通过账户模块对数据进行签名
     * @param address
     * @param password
     * @param dataHex 待签名的数据
     * @return P2PHKSignature
     * @throws NulsException
     */
    public static P2PHKSignature signDigest(String address, String password, String dataHex) throws NulsException {
        try {
            int chainId = AddressTool.getChainIdByAddress(address);
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("address", address);
            params.put("password", password);
            params.put("dataHex", dataHex);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.AC.abbr, "ac_signDigest", params);
            String signatureHex = (String)result.get("signatureHex");
            P2PHKSignature signature = TxUtil.getInstance(signatureHex, P2PHKSignature.class);
            return signature;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

}
