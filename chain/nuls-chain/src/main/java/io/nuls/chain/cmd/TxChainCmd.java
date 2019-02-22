package io.nuls.chain.cmd;


import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.tx.DestroyAssetAndChainTransaction;
import io.nuls.chain.model.tx.RegisterChainAndAssetTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.chain.service.ValidateService;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.Map;

import static io.nuls.chain.util.LoggerUtil.Log;

/**
 * 跨链创建与注销的单笔交易验证
 * @author lan
 * @date 2018/11/21
 * @description
 */
@Component
public class TxChainCmd extends BaseChainCmd {

    @Autowired
    private ChainService chainService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private RpcService rpcService;
    @Autowired
    private ValidateService validateService;
    @CmdAnnotation(cmd = "cm_chainRegValidator", version = 1.0, description = "chainRegValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txHex", parameterType = "String")
    public Response chainRegValidator(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            BlockChain blockChain = buildChainWithTxData(txHex, new RegisterChainAndAssetTransaction(), false);
            ChainEventResult chainEventResult = validateService.chainAddValidator(blockChain);
            if(chainEventResult.isSuccess()){
                return success();
            }else{
                return failed(chainEventResult.getErrorCode());
            }

        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainDestroyValidator", version = 1.0, description = "chainDestroyValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyValidator(Map params) {
        String txHex = String.valueOf(params.get("txHex"));
        BlockChain blockChain = buildChainWithTxData(txHex, new DestroyAssetAndChainTransaction(), true);
        try {
            ChainEventResult chainEventResult = validateService.chainDisableValidator(blockChain);
            if(chainEventResult.isSuccess()){
                return success();
            }else{
                return failed(chainEventResult.getErrorCode());
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(CmErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }
}
