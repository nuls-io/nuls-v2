package io.nuls.base.api.provider.account;

import io.nuls.base.api.provider.BaseReq;
import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.*;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.log.Log;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.model.ModuleE;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:41
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class AccountServiceForRpc extends BaseRpcService implements AccountService {

    @Override
    public Result<String> createAccount(CreateAccountReq req) {
        return _call("ac_createAccount", req, res -> {
            List<String> list = (List<String>) res.get("list");
            return success(list);
        });
    }

    @Override
    public Result<String> backupAccount(BackupAccountReq req) {
        return callReturnString("ac_exportAccountKeyStore", req, "path");
    }

    @Override
    public Result<String> getAccountKeyStore(KeyStoreReq req) {
        return callReturnString("ac_exportKeyStoreJson", req, "keystore");
    }

    @Override
    public Result<String> importAccountByPrivateKey(ImportAccountByPrivateKeyReq req) {
        return callReturnString("ac_importAccountByPriKey", req, "address");
    }

    @Override
    public Result<String> importAccountByKeyStore(ImportAccountByKeyStoreReq req) {
        return callReturnString("ac_importAccountByKeystore", req, "address");
    }

    @Override
    public Result<Boolean> updatePassword(UpdatePasswordReq req) {
        return _call("ac_updatePassword", req, res -> {
            Boolean data = (Boolean) res.get("value");
            return success(data);
        });
    }

    @Override
    public Result<AccountInfo> getAccountByAddress(GetAccountByAddressReq req) {
        return _call("ac_getAccountByAddress", req, res -> {
            if (res == null) {
                return fail(RPC_ERROR_CODE, "account not found");
            }
            AccountInfo accountInfo = MapUtils.mapToBean(res, new AccountInfo());
            return success(accountInfo);
        });
    }

    @Override
    public Result<AccountInfo> getAccountList() {
        return _call("ac_getAccountList", new BaseReq(), res -> {
            try {
                List<AccountInfo> list = MapUtils.mapsToObjects((List<Map<String, Object>>) res.get("list"), AccountInfo.class);
                return success(list);
            } catch (Exception e) {
                Log.error("getAccountList fail", e);
                return fail(CommonCodeConstanst.FAILED);
            }
        });
    }

    @Override
    public Result<Boolean> removeAccount(RemoveAccountReq req) {
        return _call("ac_removeAccount", req, res -> {
            Boolean data = (Boolean) res.get("value");
            return success(data);
        });
    }

    @Override
    public Result<String> getAccountPrivateKey(GetAccountPrivateKeyByAddressReq req) {
        return callReturnString("ac_getPriKeyByAddress", req, "priKey");
    }

    @Override
    public Result<String> setAccountAlias(SetAccountAliasReq req) {
        return callReturnString("ac_setAlias", req, "txHash");
    }

    @Override
    public Result<String> createMultiSignAccount(generateMultiSignAccountReq req) {
        return callReturnString("ac_createMultiSigAccount", req, "address");
    }

    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> res) {
        return callRpc(ModuleE.AC.abbr, method, req, res);
    }


    private <T> Result<T> _call(String method, Object req, Function<Map, Result> callback) {
        return call(method, req, callback);
    }


}
