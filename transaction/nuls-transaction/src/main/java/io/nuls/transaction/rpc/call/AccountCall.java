package io.nuls.transaction.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/05
 */
public class AccountCall {

    public static String getPrikey(String address, String password) throws NulsException {
        //todo 查私钥;
        int chainId = AddressTool.getChainIdByAddress(address);
        return "";
    }

    public static MultiSigAccount getMultiSigAccount(byte[] multiSignAddress) throws NulsException {
        String address = AddressTool.getStringAddressByBytes(multiSignAddress);
        //todo 获取多签账户
        return new MultiSigAccount();
    }

}
