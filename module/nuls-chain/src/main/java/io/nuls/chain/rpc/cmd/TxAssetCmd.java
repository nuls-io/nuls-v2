package io.nuls.chain.rpc.cmd;

import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.tx.AddAssetToChainTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.ValidateService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * 资产的创建与注销的单笔交易验证。
 * @author lan
 * @date 2018/11/21
 */
@Component
public class TxAssetCmd extends BaseChainCmd {

    @Autowired
    private AssetService assetService;
    @Autowired
    private ChainService chainService;
    @Autowired
    private ValidateService validateService;

    @CmdAnnotation(cmd = "cm_assetRegValidator", version = 1.0, description = "assetRegValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response assetRegValidator(Map params) {
        try {
            String txHex = String.valueOf(params.get("tx"));
            Asset asset = buildAssetWithTxChain(txHex, new AddAssetToChainTransaction());
            ChainEventResult chainEventResult = validateService.assetAddValidator(asset);
            if(chainEventResult.isSuccess()){
                return success();
            }else{
                return failed(chainEventResult.getErrorCode());
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    /**
     * 资产注销校验
     */
    @CmdAnnotation(cmd = "cm_assetDisableValidator", version = 1.0, description = "assetDisableValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response assetDisableValidator(Map params) {
        String txHex = String.valueOf(params.get("tx"));
        Asset asset = buildAssetWithTxChain(txHex, new AddAssetToChainTransaction());
        try {
            ChainEventResult chainEventResult = validateService.assetDisableValidator(asset);
            if(chainEventResult.isSuccess()){
                return success();
            }else{
                return failed(chainEventResult.getErrorCode());
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(e.getMessage());
        }
    }


}
