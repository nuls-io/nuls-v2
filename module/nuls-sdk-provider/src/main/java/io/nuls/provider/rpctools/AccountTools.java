package io.nuls.provider.rpctools;

import com.google.common.primitives.UnsignedBytes;
import io.nuls.base.data.Address;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.rpctools.vo.Account;
import io.nuls.v2.error.AccountErrorCode;
import io.nuls.v2.util.AccountTool;
import org.bouncycastle.util.encoders.Hex;

import java.util.*;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 14:06
 * @Description: 账户模块工具类
 */
@Component
public class AccountTools implements CallRpc {

    /**
     * 获取账户信息
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
     * 账户验证
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
     * 获取账户私钥
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


    public MultiSigAccount createMultiSigAccount(int chainId, List<String> pubKeys, int minSigns) throws NulsException {
        //验证公钥是否重复
        Set<String> pubkeySet = new HashSet<>(pubKeys);
        if(pubkeySet.size() < pubKeys.size()){
            throw new NulsException(AccountErrorCode.PUBKEY_REPEAT);
        }
        //公钥排序, 按固定的顺序来生成多签账户地址
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
