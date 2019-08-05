package io.nuls.provider.rpctools;

import io.nuls.base.api.provider.Result;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.model.dto.ContractTokenInfoDto;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.nuls.provider.api.constant.CommandConstant.*;

/**
 * @author: PierreLuo
 * @date: 2019-06-30
 */
@Component
public class ContractTools implements CallRpc {

    public Result<ContractTokenInfoDto> getTokenBalance(int chainId, String contractAddress, String address) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress);
        params.put("address", address);
        try {
            return  callRpc(ModuleE.SC.abbr, TOKEN_BALANCE, params,(Function<Map<String,Object>, Result<ContractTokenInfoDto>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(MapUtils.mapToBean(res, new ContractTokenInfoDto()));
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> getContractInfo(int chainId, String contractAddress) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress);
        try {
            return  callRpc(ModuleE.SC.abbr, CONTRACT_INFO, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> getContractResult(int chainId, String hash) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("hash", hash);
        try {
            return  callRpc(ModuleE.SC.abbr, CONTRACT_RESULT, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> getContractConstructor(int chainId, String contractCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractCode", contractCode);
        try {
            return  callRpc(ModuleE.SC.abbr, CONSTRUCTOR, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return new Result();
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> validateContractCreate(int chainId, Object sender, Object gasLimit, Object price, Object contractCode, Object args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("gasLimit", gasLimit);
        params.put("price", price);
        params.put("contractCode", contractCode);
        params.put("args", args);
        Map map = new HashMap(4);
        try {
            return callRpc(ModuleE.SC.abbr, VALIDATE_CREATE, params,(Function<Map<String,Object>, Result<Map>>) res->{
                map.put("success", true);
                return new Result(map);
            });
        } catch (NulsRuntimeException e) {
            map.put("success", false);
            map.put("code", e.getCode());
            map.put("msg", e.getMessage());
            return new Result(map);
        }
    }

    public Result<Map> validateContractCall(int chainId, Object sender, Object value, Object gasLimit, Object price,
                                            Object contractAddress, Object methodName, Object methodDesc, Object args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("gasLimit", gasLimit);
        params.put("price", price);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        Map map = new HashMap(4);
        try {
            return callRpc(ModuleE.SC.abbr, VALIDATE_CALL, params,(Function<Map<String,Object>, Result<Map>>) res->{
                map.put("success", true);
                return new Result(map);
            });
        } catch (NulsRuntimeException e) {
            map.put("success", false);
            map.put("code", e.getCode());
            map.put("msg", e.getMessage());
            return new Result(map);
        }
    }

    public Result<Map> validateContractDelete(int chainId, Object sender, Object contractAddress) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("contractAddress", contractAddress);
        Map map = new HashMap(4);
        try {
            return callRpc(ModuleE.SC.abbr, VALIDATE_DELETE, params,(Function<Map<String,Object>, Result<Map>>) res->{
                map.put("success", true);
                return new Result(map);
            });
        } catch (NulsRuntimeException e) {
            map.put("success", false);
            map.put("code", e.getCode());
            map.put("msg", e.getMessage());
            return new Result(map);
        }
    }

    public Result<Map> imputedContractCreateGas(int chainId, Object sender, Object contractCode, Object args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("contractCode", contractCode);
        params.put("args", args);
        try {
            return callRpc(ModuleE.SC.abbr, IMPUTED_CREATE_GAS, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return null;
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> imputedContractCallGas(int chainId, Object sender, Object value,
                                                 Object contractAddress, Object methodName, Object methodDesc, Object args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("sender", sender);
        params.put("value", value);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        try {
            return callRpc(ModuleE.SC.abbr, IMPUTED_CALL_GAS, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return null;
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map> invokeView(int chainId, Object contractAddress, Object methodName, Object methodDesc, Object args) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.CHAIN_ID, chainId);
        params.put("contractAddress", contractAddress);
        params.put("methodName", methodName);
        params.put("methodDesc", methodDesc);
        params.put("args", args);
        try {
            return callRpc(ModuleE.SC.abbr, INVOKE_VIEW, params,(Function<Map<String,Object>, Result<Map>>) res->{
                if(res == null){
                    return null;
                }
                return new Result(res);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

}
