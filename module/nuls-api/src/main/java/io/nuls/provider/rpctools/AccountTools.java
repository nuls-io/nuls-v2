package io.nuls.provider.rpctools;

import com.google.common.primitives.UnsignedBytes;
import io.nuls.base.data.Address;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.model.dto.AccountBlockDTO;
import io.nuls.provider.rpctools.vo.Account;
import io.nuls.v2.error.AccountErrorCode;
import io.nuls.v2.util.AccountTool;
import org.bouncycastle.util.encoders.Hex;

import java.util.*;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 14:06
 * @Description: Account module tool class
 */
@Component
public class AccountTools implements CallRpc {

    /**
     * Obtain account information
     * @param chainId
     * @param address
     * @return
     */
    public Account getAccountByAddress(int chainId,String address) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("chainId", chainId);
        param.put("address", address);
        return callRpc(ModuleE.AC.name, "ac_getAccountByAddress", param, (Function<Map<String, Object>, Account>) res -> {
                    if (res == null) {
                        return null;
                    }
                    return MapUtils.mapToBean(res, new Account());
                }
        );
    }


    /**
     * Account verification
     * account validate
     *
     * @param chainId
     * @param address
     * @param password
     * @return validate result
     */
    public boolean accountValid(int chainId, String address, String password) throws NulsException {
        return getAddressInfo(chainId,address,password,"valid");
    }


    /**
     * Obtain account private key
     * account validate
     *
     * @param chainId
     * @param address
     * @param password
     * @return validate result
     */
    public String getAddressPriKey(int chainId, String address, String password) throws NulsException {
        return getAddressInfo(chainId,address,password,"priKey");
    }

    private <T> T getAddressInfo(int chainId, String address, String password,String key) throws NulsException {
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put(Constants.CHAIN_ID, chainId);
        callParams.put("address", address);
        callParams.put("password", password);
        return callRpc(ModuleE.AC.abbr, "ac_getPriKeyByAddress", callParams, (Function<Map<String, Object>, T>) res -> (T) res.get(key));
    }


    public boolean isBlockAccount(int chainId, String address) {
        try {
            if (StringUtils.isBlank(address)) {
                return false;
            }
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            boolean isBlock = callRpc(ModuleE.AC.abbr, "ac_isBlockAccount", params, (Function<Map<String, Object>, Boolean>) res -> {
                if (res == null) {
                    return false;
                }
                return (boolean) res.get("value");

            });
            return isBlock;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    public AccountBlockDTO getBlockAccountInfo(int chainId, String address) {
        try {
            if (StringUtils.isBlank(address)) {
                return null;
            }
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            AccountBlockDTO dto = callRpc(ModuleE.AC.abbr, "ac_getBlockAccountInfo", params, (Function<Map<String, Object>, AccountBlockDTO>) res -> {
                if (res == null) {
                    return null;
                }
                AccountBlockDTO result = new AccountBlockDTO();
                result.setAddress((String) res.get("address"));
                Object obj0 = res.get("types");
                if (obj0 != null) {
                    result.setTypes((List<Integer>) obj0);
                }
                Object obj1 = res.get("contracts");
                if (obj1 != null) {
                    result.setContracts((List<String>) obj1);
                }
                return result;
            });
            return dto;
        } catch (Exception e) {
            io.nuls.provider.utils.Log.error(e);
            return null;
        }
    }

    public Map getAllContractCallAccount(int chainId) {
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            Map result = callRpc(ModuleE.AC.abbr, "ac_getAllContractCallAccount", params, (Function<Map<String, Object>, Map>) res -> {
                if (res == null) {
                    return null;
                }
                return res;
            });
            return result;
        } catch (Exception e) {
            io.nuls.provider.utils.Log.error(e);
            return null;
        }
    }

    public MultiSigAccount createMultiSigAccount(int chainId, List<String> pubKeys, int minSigns) throws NulsException {
        //Verify if the public key is duplicated
        Set<String> pubkeySet = new HashSet<>(pubKeys);
        if(pubkeySet.size() < pubKeys.size()){
            throw new NulsException(AccountErrorCode.PUBKEY_REPEAT);
        }
        //Public key sorting, Generate multiple account addresses in a fixed order
        pubKeys = new ArrayList<String>(pubKeys);
        Collections.sort(pubKeys, new Comparator<String>() {
            private Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();
            @Override
            public int compare(String k1, String k2) {
                return comparator.compare(Hex.decode(k1), Hex.decode(k2));
            }
        });
        Address address = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(AccountTool.createMultiSigAccountOriginBytes(chainId, minSigns, pubKeys)));

        MultiSigAccount multiSigAccount = new MultiSigAccount();
        multiSigAccount.setChainId(chainId);
        multiSigAccount.setAddress(address);
        multiSigAccount.setM((byte) minSigns);

        List<byte[]> list = new ArrayList<>();
        for (String pubKey : pubKeys) {
            list.add(HexUtil.decode(pubKey));
        }
        multiSigAccount.setPubKeyList(list);
        return multiSigAccount;
    }

}
