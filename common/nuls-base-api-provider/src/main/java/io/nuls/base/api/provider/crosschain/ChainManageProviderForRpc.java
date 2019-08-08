package io.nuls.base.api.provider.crosschain;

import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.*;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.model.ModuleE;

import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 16:08
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class ChainManageProviderForRpc extends BaseRpcService implements ChainManageProvider {

    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> callback) {
        return callRpc(ModuleE.CM.abbr,method,req,callback);
    }

    @Override
    public Result<CrossChainRegisterInfo> getCrossChainInfo(GetCrossChainInfoReq req) {
        return _call("cm_chain",req,res->{
            if(res == null){
                return fail(RPC_ERROR_CODE,"chain not found");
            }
            CrossChainRegisterInfo crossChainRegisterInfo = MapUtils.mapToBean(res,new CrossChainRegisterInfo());
            return success(crossChainRegisterInfo);
        });
    }
    @Override
    public  Result<CrossAssetRegisterInfo> getCrossAssetInfo(GetCrossAssetInfoReq req){
        return _call("cm_asset",req,res->{
            if(res == null){
                return fail(RPC_ERROR_CODE,"asset not found");
            }
            CrossAssetRegisterInfo crossAssetRegisterInfo = MapUtils.mapToBean(res,new CrossAssetRegisterInfo());
            return success(crossAssetRegisterInfo);
        });
    }

    @Override
    public Result<Map> registerChain(RegisterChainReq req) {
        return callResutlMap("cm_chainReg",req);
    }
    @Override
    public Result<Map> updateChain(RegisterChainReq req) {
        return callResutlMap("cm_chainActive",req);
    }
    @Override
    public Result<String> disableCrossAsset(DisableAssetReq req) {
        return callReturnString("cm_assetDisable",req,"txHash");
    }

    @Override
    public Result<String> addCrossAsset(AddCrossAssetReq req) {
        return callReturnString("cm_assetReg",req,"txHash");
    }

    private <T> Result<T> _call(String method, Object req, Function<Map, Result> callback){
        return call(method,req,callback);
    }

}
