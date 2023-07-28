package io.nuls.crosschain.rpc.cmd;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.GetConfirmedTxByHashReq;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.CrossTxRehandleMessage;
import io.nuls.common.NulsCoresConfig;
import io.nuls.crosschain.message.CrossTxRehandleMsgHandler;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.rpc.call.NetWorkCall;
import io.nuls.crosschain.srorage.ConvertCtxService;
import io.nuls.crosschain.srorage.CtxStatusService;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.io.IOException;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2020/7/14 11:14
 * @Description: 功能描述
 */
@Component
@NulsCoresCmd(module = ModuleE.CC)
public class CrossChainTxCmd extends BaseCmd {

    @Autowired
    private ConvertCtxService convertCtxService;

    @Autowired
    private CtxStatusService ctxStatusService;

    @Autowired
    NulsCoresConfig config;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    CrossTxRehandleMsgHandler crossTxRehandleMsgHandler;

    TransferService transferService = ServiceManager.get(TransferService.class);

    /**
     * 区块模块高度变化通知跨链模块
     * */
    @CmdAnnotation(cmd = "getCrossChainTxInfoForConverterTable", version = 1.0, description = "通过交易hash在跨链模块查询交易详情")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "交易hash")
    @ResponseData(description = "")
    public io.nuls.core.rpc.model.message.Response getCrossChainTxInfoForConverterTable(Map<String,Object> params) throws IOException {
        Transaction transaction = convertCtxService.get(new NulsHash(HexUtil.decode((String) params.get("txHash"))),config.getChainId());
        return success(HexUtil.encode(transaction.serialize()));
    }

    @CmdAnnotation(cmd = "getCrossChainTxInfoForCtxStatusPO", version = 1.0, description = "通过交易hash在跨链模块查询交易详情")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "交易hash")
    @ResponseData(description = "")
    public Response getCrossChainTxInfoForCtxStatusPO(Map<String,Object> params) throws IOException {
        CtxStatusPO transaction = ctxStatusService.get(new NulsHash(HexUtil.decode((String) params.get("txHash"))),config.getChainId());
        if(transaction == null || transaction.getTx() == null){
            return failed("not found tx");
        }
        return success(HexUtil.encode(transaction.getTx().serialize()));
    }


    @CmdAnnotation(cmd = CommandConstant.CROSS_TX_REHANDLE_MESSAGE, version = 1.0, description = "通过交易hash在跨链模块查询交易详情")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID")
    @Parameter(parameterName = "ctxHash", parameterType = "String", parameterDes = "交易hash")
    @Parameter(parameterName = "blockHeight", requestType = @TypeDescriptor(value = long.class),  parameterDes = "当前区块高度")
    @ResponseData(description = "")
    public Response crossTxRehandle(Map<String,Object> params) throws IOException {
//        CtxStatusPO transaction = ctxStatusService.get(new NulsHash(HexUtil.decode((String) params.get("ctxHash"))),config.getChainId());
//        if(transaction == null || transaction.getTx() == null){
//            return failed("not found ctx");
//        }
        String ctxHash = (String) params.get("ctxHash");
        Result<Transaction> tx = transferService.getConfirmedTxByHash(new GetConfirmedTxByHashReq(ctxHash));
        if(tx.isFailed()){
            return failed(tx.getMessage());
        }
        Transaction transaction = tx.getData();
        if(transaction.getType() != TxType.CROSS_CHAIN && transaction.getType() != TxType.CONTRACT_TOKEN_CROSS_TRANSFER){
            return failed("not a cross chain tx");
        }
        long height = Long.parseLong(params.get("blockHeight").toString());
        int chainId = (int)params.get("chainId");
        CrossTxRehandleMessage crossTxRehandleMessage = new CrossTxRehandleMessage();
        crossTxRehandleMessage.setCtxHash(transaction.getHash());
        crossTxRehandleMessage.setBlockHeight(height);
        crossTxRehandleMsgHandler.process(chainId,crossTxRehandleMessage);
        boolean res = NetWorkCall.broadcast(chainId,crossTxRehandleMessage,CommandConstant.CROSS_TX_REHANDLE_MESSAGE,false);

        if(res){
            return success(Map.of("msg","broadcast success"));
        }else{
            return success(Map.of("msg","broadcast fail"));
        }
    }



}
