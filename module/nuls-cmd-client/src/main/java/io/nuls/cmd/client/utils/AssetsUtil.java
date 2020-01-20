package io.nuls.cmd.client.utils;

import io.nuls.cmd.client.processor.ErrorCodeConstants;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产工具类
 * @author lanjinsheng
 * @description
 * @date 2019/11/07
 **/
public class AssetsUtil {
    public static Map<String, Integer> ASSETS_DECIMALS = new HashMap<>();

    static String getAssetKey(int chainId, int assetId) {
        return chainId + "-" + assetId;
    }

    public static Integer getCrossAssetDecimal(int chainId, int assetId) {
        String key = getAssetKey(chainId, assetId);
        if (null == ASSETS_DECIMALS.get(key)) {
            initRegisteredChainInfo();
        }
        return ASSETS_DECIMALS.get(key);
    }

    public static Result initRegisteredChainInfo() {
        try {
            Map<String, Object> map = (Map) request(ModuleE.CC.abbr, "getRegisteredChainInfoList", null, null);
            List<Map<String, Object>> resultList = (List<Map<String, Object>>) map.get("list");
            for (Map<String, Object> resultMap : resultList) {
                List<Map<String, Object>> assetList = (List<Map<String, Object>>) resultMap.get("assetInfoList");
                if (assetList != null) {
                    for (Map<String, Object> assetMap : assetList) {
                        int chainId = (Integer) resultMap.get("chainId");
                        int assetId = (Integer) assetMap.get("assetId");
                        ASSETS_DECIMALS.put(getAssetKey(chainId, assetId), (Integer) assetMap.get("decimalPlaces"));
                    }
                }
            }
            return Result.getSuccess(null).setData(map);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode());
        }
    }

    public static Object request(String moduleCode, String cmd, Map params, Long timeout) throws NulsException {
        try {
            Response response;
            try {
                if (null == timeout) {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
                } else {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params, timeout);
                }
            } catch (Exception e) {
                LoggerUtil.logger.error(e);
                throw new NulsException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION, e.getMessage());
            }
            if (!response.isSuccess()) {
                String comment = response.getResponseComment();
                if (StringUtils.isBlank(comment)) {
                    comment = "";
                }

                String errorCode = response.getResponseErrorCode();
                LoggerUtil.logger.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, errorCode, response.getResponseComment());
                if (response.getResponseStatus() == Response.FAIL) {
                    //business error
                    if (StringUtils.isBlank(errorCode)) {
                        throw new NulsException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION, comment);
                    }
                    throw new NulsException(ErrorCode.init(errorCode), comment);
                } else {
                    if (StringUtils.isNotBlank(comment)) {
                        throw new NulsException(CommonCodeConstanst.FAILED, comment);
                    }
                    throw new NulsException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION, "unknown error");
                }
            }
            Map data = (Map) response.getResponseData();
            return data.get(cmd);
        } catch (Exception e) {
            LoggerUtil.logger.error(e);
            if (e instanceof NulsException) {
                throw (NulsException) e;
            }
            throw new NulsException(ErrorCodeConstants.SYSTEM_ERR, e.getMessage());
        }
    }
}
