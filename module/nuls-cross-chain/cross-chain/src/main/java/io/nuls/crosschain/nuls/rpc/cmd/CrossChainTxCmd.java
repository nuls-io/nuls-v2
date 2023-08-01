package io.nuls.crosschain.nuls.rpc.cmd;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.GetConfirmedTxByHashReq;
import io.nuls.base.api.provider.transaction.facade.GetTxByHashReq;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
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
import io.nuls.crosschain.base.model.bo.AssetInfo;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.message.CrossTxRehandleMsgHandler;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.srorage.ConvertCtxService;
import io.nuls.crosschain.nuls.srorage.CtxStatusService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: zhoulijun
 * @Time: 2020/7/14 11:14
 * @Description: 功能描述
 */
@Component
public class CrossChainTxCmd extends BaseCmd {

    @Autowired
    private ConvertCtxService convertCtxService;

    @Autowired
    private CtxStatusService ctxStatusService;

    @Autowired
    NulsCrossChainConfig config;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    CrossTxRehandleMsgHandler crossTxRehandleMsgHandler;
    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;

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


    @CmdAnnotation(cmd = "cc_asset", version = 1.0,
            description = "跨链资产查询")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产链ID"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产ID")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = AssetInfo.class)
    )
    public Response getAsset(Map params) {
        Map<String, Object> rtMap;
        try {
            int chainId = Integer.parseInt(params.get("chainId").toString());
            int assetId = Integer.parseInt(params.get("assetId").toString());
            RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
            //获取资产所在的链，如果没有，表示链没有注册，不能进行跨链转账
            Optional<ChainInfo> chainInfo = registeredChainMessage.getChainInfoList().stream().filter(d->d.getChainId()==chainId).findFirst();
            if(chainInfo.isEmpty()){
                return failed("not a cross chain asset");
            }
            //获取链注册的资产列表
            List<AssetInfo> assetInfoList = chainInfo.get().getAssetInfoList();
            //如果当前需要跨链的资产不在资产列表里，也不能进行跨链转账
            Optional<AssetInfo> assetInfo = assetInfoList.stream().filter(d->d.getAssetId()==assetId).findFirst();
            if(assetInfo.isEmpty()){
                return failed("not a cross chain asset");
            }
            return success(assetInfo.get());
        } catch (Exception e) {
            chainManager.getChainMap().get(config.getChainId()).getLogger().error(e.getMessage(), e);
            return failed(e.getMessage());
        }
    }

}
