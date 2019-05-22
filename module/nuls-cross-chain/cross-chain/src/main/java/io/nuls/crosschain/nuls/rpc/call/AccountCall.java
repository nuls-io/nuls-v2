package io.nuls.crosschain.nuls.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.crosschain.nuls.utils.CommonUtil;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.exception.NulsException;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainConstant.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 与账户模块交互类
 * Interaction class with account module
 * @author tag
 * 2019/4/10
 */
public class AccountCall {
    /**
     * 查询地址私钥
     * Query address private key
     */
    public static String getPrikey(String address, String password) throws NulsException {
        try {
            int chainId = AddressTool.getChainIdByAddress(address);
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            HashMap result = (HashMap) CommonCall.request(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
            return (String) result.get("priKey");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
    /**
     * 查询地址是否加密
     * Is address Encrypted
     */
    public static boolean isEncrypted(String address) throws NulsException {
        try {
            int chainId = AddressTool.getChainIdByAddress(address);
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            HashMap result = (HashMap) CommonCall.request(ModuleE.AC.abbr, "ac_isEncrypted", params);
            return (boolean) result.get("value");
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
            String address = AddressTool.getStringAddressByBytes(multiSignAddress);
            int chainId = AddressTool.getChainIdByAddress(address);
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            HashMap result = (HashMap) CommonCall.request(ModuleE.AC.abbr, "ac_getMultiSigAccount", params);
            String mAccountStr = (String) result.get("value");
            return null == mAccountStr ? null : CommonUtil.getInstanceRpcStr(mAccountStr, MultiSigAccount.class);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 通过账户模块对数据进行签名
     * @param address
     * @param password
     * @param data 待签名的数据
     * @return P2PHKSignature
     * @throws NulsException
     */
    public static P2PHKSignature signDigest(String address, String password, byte[] data) throws NulsException {
        try {
            int chainId = AddressTool.getChainIdByAddress(address);
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", password);
            params.put("data", RPCUtil.encode(data));
            HashMap result = (HashMap) CommonCall.request(ModuleE.AC.abbr, "ac_signDigest", params);
            String signatureStr = (String)result.get("signature");
            P2PHKSignature signature = CommonUtil.getInstanceRpcStr(signatureStr, P2PHKSignature.class);
            return signature;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
