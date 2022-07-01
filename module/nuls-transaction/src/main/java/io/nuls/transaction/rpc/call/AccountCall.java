package io.nuls.transaction.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.dto.AccountBlockDTO;
import io.nuls.transaction.model.po.AccountBlockExtendPO;
import io.nuls.transaction.model.po.AccountBlockPO;
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
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            HashMap result = (HashMap) TransactionCall.requestAndResponse(ModuleE.AC.abbr, "ac_getMultiSignAccount", params);
            String mAccountStr = (String) result.get("value");
            return null == mAccountStr ? null : TxUtil.getInstanceRpcStr(mAccountStr, MultiSigAccount.class);
        } catch (RuntimeException e){
            Log.error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

    public static boolean isBlockAccount(int chainId, String address) {
        try {
            if (StringUtils.isBlank(address)) {
                return false;
            }
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            Map resultMap = (Map) TransactionCall.requestAndResponse(ModuleE.AC.abbr, "ac_isBlockAccount", params);
            boolean isBlock = (boolean) resultMap.get("value");
            return isBlock;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    public static AccountBlockDTO getBlockAccount(int chainId, String address) {
        try {
            if (StringUtils.isBlank(address)) {
                return null;
            }
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            Map resultMap = (Map) TransactionCall.requestAndResponse(ModuleE.AC.abbr, "ac_getBlockAccountBytes", params);
            String hex = (String) resultMap.get("value");
            if (StringUtils.isBlank(hex)) {
                return null;
            }
            AccountBlockPO po = new AccountBlockPO();
            po.parse(HexUtil.decode(hex), 0);
            AccountBlockDTO dto = new AccountBlockDTO();
            dto.setAddress(po.getAddress());
            if (po.getExtend() != null) {
                AccountBlockExtendPO extendPO = new AccountBlockExtendPO();
                extendPO.parse(po.getExtend(), 0);
                dto.setTypes(extendPO.getTypes());
                dto.setContracts(extendPO.getContracts());
                dto.setExtend(extendPO.getExtend());
            }
            return dto;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
}
