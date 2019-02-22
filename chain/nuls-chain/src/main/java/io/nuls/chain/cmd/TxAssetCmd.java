package io.nuls.chain.cmd;

import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.tx.AddAssetToChainTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.ValidateService;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.Map;

import static io.nuls.chain.util.LoggerUtil.Log;

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
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response assetRegValidator(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            Asset asset = buildAssetWithTxChain(txHex, new AddAssetToChainTransaction());
            ChainEventResult chainEventResult = validateService.assetAddValidator(asset);
            if(chainEventResult.isSuccess()){
                return success();
            }else{
                return failed(chainEventResult.getErrorCode());
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.UNKOWN_ERROR);
        }
    }


    /**
     * 资产注销校验
     */
    @CmdAnnotation(cmd = "cm_assetDisableValidator", version = 1.0, description = "assetDisableValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response assetDisableValidator(Map params) {
        String txHex = String.valueOf(params.get("txHex"));
        Asset asset = buildAssetWithTxChain(txHex, new AddAssetToChainTransaction());
        try {
            ChainEventResult chainEventResult = validateService.assetDisableValidator(asset);
            if(chainEventResult.isSuccess()){
                return success();
            }else{
                return failed(chainEventResult.getErrorCode());
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


}
