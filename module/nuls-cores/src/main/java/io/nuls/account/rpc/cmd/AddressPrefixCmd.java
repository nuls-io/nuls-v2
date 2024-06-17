package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.Preconditions;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/5
 */
@Component
@NulsCoresCmd(module = ModuleE.AC)
public class AddressPrefixCmd extends BaseCmd {

    @CmdAnnotation(cmd = "ac_getAllAddressPrefix", version = 1.0, description = "Get address prefixes for all chains")
    @ResponseData(name = "Return value", description = "Return aList", responseType = @TypeDescriptor(value = List.class,
            collectionElement = Map.class, mapKeys = {
            @Key(name = "chainId", valueType = Integer.class, description = "chainid"),
            @Key(name = "addressPrefix", valueType = String.class, description = "Address prefix")
    }))
    public Response getAllAddressPrefix(Map params) {
        List<Map<String, Object>> rtList = new ArrayList<>();
        try {
            Map<Integer, String> addressPreFixMap = AddressTool.getAddressPreFixMap();
            for (Map.Entry<Integer, String> entry : addressPreFixMap.entrySet()) {
                Map<String, Object> rtValue = new HashMap<>();
                rtValue.put("chainId", entry.getKey());
                rtValue.put("addressPrefix", entry.getValue());
                rtList.add(rtValue);
            }
            LoggerUtil.LOG.debug(JSONUtils.obj2json(rtList));
        } catch (Exception e) {
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(rtList);
    }

    @CmdAnnotation(cmd = "ac_getAddressPrefixByChainId", version = 1.0, description = "By chainidGet address prefix")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "chainId", valueType = Integer.class, description = "chainid"),
            @Key(name = "addressPrefix", valueType = String.class, description = "Address prefix")
    }))
    public Response getAddressPrefixByChainId(Map params) {
        Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
        Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
        if (chainIdObj == null) {
            throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
        }
        int chainId = Integer.valueOf(chainIdObj.toString());
        Map<String, Object> rtValue = new HashMap<>();
        try {
            Map<Integer, String> addressPreFixMap = new HashMap<>();
            rtValue.put("chainId", addressPreFixMap.get(chainId));
            rtValue.put("addressPrefix", chainId);
        } catch (Exception e) {
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(rtValue);
    }

    @CmdAnnotation(cmd = "ac_addAddressPrefix", version = 1.0, description = "Add address prefix,The chain management module will call this interface")
    @Parameters(value = {
            @Parameter(parameterName = "prefixList", requestType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "chainId", valueType = Integer.class, description = "chainid"),
                    @Key(name = "addressPrefix", valueType = String.class, description = "Address prefix")
            }), parameterDes = "Chain Address Prefix List")
    })
    @ResponseData(description = "No specific return value, successful without errors")
    public Response addAddressPrefix(Map params) {
        List<Map<String, Object>> prefixList = (List) params.get("prefixList");
        for (Map<String, Object> prefixMap : prefixList) {
            AddressTool.addPrefix(Integer.valueOf(prefixMap.get("chainId").toString()), String.valueOf(prefixMap.get("addressPrefix")));
            LoggerUtil.LOG.debug("chainId={},prefix={}", prefixMap.get("chainId"), prefixMap.get("addressPrefix"));
        }
        return success();
    }
}
