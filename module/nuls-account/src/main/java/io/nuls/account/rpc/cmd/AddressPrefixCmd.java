package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.Preconditions;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
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
public class AddressPrefixCmd extends BaseCmd {

    @CmdAnnotation(cmd = "ac_getAllAddressPrefix", version = 1.0, description = "获取所有链的地址前缀")
    @ResponseData(name = "返回值", description = "返回一个List", responseType = @TypeDescriptor(value = List.class,
            collectionElement = Map.class, mapKeys = {
            @Key(name = "chainId", valueType = Integer.class, description = "链id"),
            @Key(name = "addressPrefix", valueType = String.class, description = "地址前缀")
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
        } catch (Exception e) {
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return success(rtList);
    }

    @CmdAnnotation(cmd = "ac_getAddressPrefixByChainId", version = 1.0, description = "通过链id获取地址前缀")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "chainId", valueType = Integer.class, description = "链id"),
            @Key(name = "addressPrefix", valueType = String.class, description = "地址前缀")
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

    @CmdAnnotation(cmd = "ac_addAddressPrefix", version = 1.0, description = "添加地址前缀,链管理模块会调用该接口")
    @Parameters(value = {
            @Parameter(parameterName = "prefixList", requestType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "chainId", valueType = Integer.class, description = "链id"),
                    @Key(name = "addressPrefix", valueType = String.class, description = "地址前缀")
            }), parameterDes = "链地址前缀列表")
    })
    @ResponseData(description = "无特定返回值，没有错误即成功")
    public Response addAddressPrefix(Map params) {
        List<Map<String, Object>> prefixList = (List) params.get("prefixList");
        for (Map<String, Object> prefixMap : prefixList) {
            AddressTool.addPrefix(Integer.valueOf(prefixMap.get("chainId").toString()), String.valueOf(prefixMap.get("addressPrefix")));
            LoggerUtil.LOG.debug("chainId={},prefix={}", prefixMap.get("chainId"), prefixMap.get("addressPrefix"));
        }
        return success();
    }
}
