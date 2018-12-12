package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.bo.tx.TxRegisterDetail;
import io.nuls.account.util.annotation.ResisterTx;
import io.nuls.account.util.annotation.TxMethodType;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.ScanUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账本模块接口调用
 * @author: qinyifeng
 * @date: 2018/12/12
 */
public class LegerCmdCall {

    /**
     * 查询用户余额
     */
    public static BigInteger getBalance(int chainId, int assetId, String address) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("assetId", assetId);
            params.put("address", address);
            //Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "getBalance", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BigInteger("10");
    }

}
