package io.nuls.api.provider.account;

import io.nuls.api.provider.BaseReq;
import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.account.facade.*;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.parse.MapUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:41
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
@Slf4j
public class AccountServiceForRpc extends BaseRpcService implements AccountService {

    @Override
    public Result<String> createAccount(CreateAccountReq req) {
        return call("ac_createAccount",req,res->{
            List<String> list = (List<String>) res.get("list");
            return success(list);
        });
    }

    @Override
    public Result<String> backupAccount(BackupAccountReq req) {
        return callReturnString("ac_exportAccountKeyStore",req,"path");
    }

    @Override
    public Result<String> importAccountByPrivateKey(ImportAccountByPrivateKeyReq req) {
        return callReturnString("ac_importAccountByPriKey",req,"address");
    }

    @Override
    public Result<String> importAccountByKeyStore(ImportAccountByKeyStoreReq req) {
        return callReturnString("ac_importAccountByKeystore",req,"address");
    }

    @Override
    public Result<Boolean> updatePassword(UpdatePasswordReq req) {
        return call("ac_updatePassword",req,res->{
            Boolean data = (Boolean) res.get("value");
            return success(data);
        });
    }

    @Override
    public Result<AccountInfo> getAccountByAddress(GetAccountByAddressReq req) {
        return call("ac_getAccountByAddress",req,res->{
            AccountInfo accountInfo = MapUtils.mapToBean(res,new AccountInfo());
            return success(accountInfo);
        });
    }

    @Override
    public Result<AccountInfo> getAccountList() {
        return call("ac_getAccountList",new BaseReq(),res->{
            try {
                List<AccountInfo> list = MapUtils.mapsToObjects((List<Map<String, Object>>) res.get("list"),AccountInfo.class);
                return success(list);
            } catch (InstantiationException e) {
                log.error("getAccountList fail",e);
                return fail(ERROR_CODE);
            } catch (IllegalAccessException e) {
                log.error("getAccountList fail",e);
                return fail(ERROR_CODE);
            }
        });
    }

    @Override
    public Result<Boolean> removeAccount(RemoveAccountReq req) {
        return call("ac_removeAccount",req,res->{
            Boolean data = (Boolean) res.get("value");
            return success(data);
        });
    }

    @Override
    public Result<String> getAccountPrivateKey(GetAccountPrivateKeyByAddressReq req) {
        return callReturnString("ac_getPriKeyByAddress",req,"priKey");
    }

    @Override
    public Result<String> setAccountAlias(SetAccountAliasReq req) {
        return callReturnString("ac_setAlias",req,"txHash");
    }


    private <T> Result<T> call(String method, Object req, Function<Map,Result> res){
        return callRpc(ModuleE.AC.abbr,method,req,res);
    }

    private Result<String> callReturnString(String method,Object req, String fieldName){
        return call(method,req,res->{
            String data = (String) res.get(fieldName);
            return success(data);
        });
    }

}
