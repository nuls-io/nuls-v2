package io.nuls.api.provider.account;

import io.nuls.api.provider.BaseService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.account.facade.CreateAccountReq;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:41
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class AccountServiceForRpc extends BaseService implements AccountService {


    @Override
    public String hello() {
        return "hello world";
    }

    @Override
    public Result<String> createAccount(CreateAccountReq req) {
        try {
            Map<String, Object> params = MapUtils.beanToLinkedMap(req);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_createAccount", params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.AC.abbr, "ac_createAccount", cmdResp.getResponseComment());
                return fail(BaseService.ERROR_CODE,cmdResp.getResponseComment());
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_createAccount");
            List data = (List) result.get("list");
            return success(data);
        } catch (Exception eil) {
            Log.error("Calling remote interface failed. module:{} - interface:{}", ModuleE.AC.abbr, "ac_createAccount");
            return fail(BaseService.ERROR_CODE,"call remote failed");
        }
    }
}
