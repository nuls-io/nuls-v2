package io.nuls.chain.rpc.cmd;


import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.tx.DestroyAssetAndChainTransaction;
import io.nuls.chain.model.tx.RegisterChainAndAssetTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.rpc.call.RpcService;
import io.nuls.chain.service.ValidateService;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

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
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response chainRegValidator(Map params) {
        try {
            String txHex = String.valueOf(params.get("tx"));
            BlockChain blockChain = buildChainWithTxData(txHex, new RegisterChainAndAssetTransaction(), false);
            ChainEventResult chainEventResult = validateService.chainAddValidator(blockChain);
            if(chainEventResult.isSuccess()){
                return success();
            }else{
                return failed(chainEventResult.getErrorCode());
            }

        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainDestroyValidator", version = 1.0, description = "chainDestroyValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response chainDestroyValidator(Map params) {
        String txHex = String.valueOf(params.get("tx"));
        BlockChain blockChain = buildChainWithTxData(txHex, new DestroyAssetAndChainTransaction(), true);
        try {
            ChainEventResult chainEventResult = validateService.chainDisableValidator(blockChain);
            if(chainEventResult.isSuccess()){
                return success();
            }else{
                return failed(chainEventResult.getErrorCode());
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION, e.getMessage());
        }
    }
}
