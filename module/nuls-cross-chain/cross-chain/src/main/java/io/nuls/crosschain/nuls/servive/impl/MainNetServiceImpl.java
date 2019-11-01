package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.*;
import io.nuls.crosschain.base.model.bo.AssetInfo;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.rpc.call.AccountCall;
import io.nuls.crosschain.nuls.rpc.call.ChainManagerCall;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.servive.MainNetService;
import io.nuls.crosschain.nuls.srorage.CtxStatusService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.LoggerUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.core.constant.CommonCodeConstanst.*;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.CHAIN_NOT_EXIST;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.CROSS_CHAIN_NETWORK_UNAVAILABLE;
import static io.nuls.crosschain.nuls.constant.ParamConstant.CHAIN_ID;
import static io.nuls.crosschain.nuls.constant.ParamConstant.TX_HASH;


/**
 * 主网跨链模块特有方法
 * @author tag
 * @date 2019/4/23
 */
@Component
public class MainNetServiceImpl implements MainNetService {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private NulsCrossChainConfig nulsCrossChainConfig;

    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;

    @Autowired
    private CtxStatusService ctxStatusService;

    @Override
    @SuppressWarnings("unchecked")
    public Result registerCrossChain(Map<String, Object> params) {
        if (params == null) {
            LoggerUtil.commonLog.error("参数错误");
            return Result.getFailed(PARAMETER_ERROR);
        }
        ChainInfo chainInfo = JSONUtils.map2pojo(params, ChainInfo.class);
        Chain chain = chainManager.getChainMap().get(nulsCrossChainConfig.getMainChainId());
        RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
        if(registeredChainMessage == null){
            registeredChainMessage = new RegisteredChainMessage();
        }
        if(registeredChainMessage.getChainInfoList() == null){
            List<ChainInfo> chainInfoList = new ArrayList<>();
            registeredChainMessage.setChainInfoList(chainInfoList);
        }
        registeredChainMessage.getChainInfoList().add(chainInfo);
        registeredCrossChainService.save(registeredChainMessage);
        chainManager.setRegisteredCrossChainList(registeredChainMessage.getChainInfoList());
        LoggerUtil.commonLog.info("有新链注册跨链，chainID:{},初始验证人列表：{}",chainInfo.getChainId(),chainInfo.getVerifierList().toString());
        //创建验证人初始化交易
        try {
            Transaction verifierInitTx = TxUtil.createVerifierInitTx((List<String>) ConsensusCall.getPackerInfo(chain).get(ParamConstant.PARAM_PACK_ADDRESS_LIST), chainInfo.getRegisterTime(), chainInfo.getChainId());
            TxUtil.handleNewCtx(verifierInitTx, chain, null);
        }catch (IOException e){
            chain.getLogger().error(e);
            return Result.getFailed(DATA_PARSE_ERROR);
        }
        return Result.getSuccess(SUCCESS);
    }

    @Override
    public Result registerAssert(Map<String, Object> params) {
        if (params == null) {
            LoggerUtil.commonLog.error("参数错误");
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int)params.get(ParamConstant.CHAIN_ID);
        int assetId = (int)params.get(ParamConstant.ASSET_ID);
        String symbol = (String)params.get(ParamConstant.SYMBOL);
        String assetName = (String)params.get(ParamConstant.ASSET_NAME);
        boolean usable = (boolean)params.get(ParamConstant.USABLE);
        int decimalPlaces = (int)params.get(ParamConstant.DECIMAL_PLACES);
        AssetInfo assetInfo = new AssetInfo(assetId,symbol,assetName,usable,decimalPlaces);
        chainManager.getChainInfo(chainId).getAssetInfoList().add(assetInfo);
        return Result.getSuccess(SUCCESS);
    }

    @Override
    public Result cancelCrossChain(Map<String, Object> params) {
        if (params == null || params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.ASSET_ID) == null) {
            LoggerUtil.commonLog.error("参数错误");
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int)params.get(ParamConstant.CHAIN_ID);
        int assetId = (int)params.get(ParamConstant.ASSET_ID);
        RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
        if(assetId == 0){
            registeredChainMessage.getChainInfoList().removeIf(chainInfo -> chainInfo.getChainId() == chainId);
        }else{
            for (ChainInfo chainInfo:registeredChainMessage.getChainInfoList()) {
                if(chainInfo.getChainId() == chainId){
                    for (AssetInfo assetInfo:chainInfo.getAssetInfoList()) {
                        if (assetInfo.getAssetId() == assetId){
                            assetInfo.setUsable(false);
                        }
                    }
                }
            }
        }
        registeredCrossChainService.save(registeredChainMessage);
        chainManager.setRegisteredCrossChainList(registeredChainMessage.getChainInfoList());
        return Result.getSuccess(SUCCESS);
    }

    @Override
    public Result crossChainRegisterChange(Map<String, Object> params) {
        if (params == null || params.get(ParamConstant.CHAIN_ID) == null) {
            LoggerUtil.commonLog.error("参数错误");
            return Result.getFailed(PARAMETER_ERROR);
        }
        if (!nulsCrossChainConfig.isMainNet()) {
            LoggerUtil.commonLog.error("本链不是主网");
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int) params.get(CHAIN_ID);

        if (chainId != nulsCrossChainConfig.getMainChainId()) {
            LoggerUtil.commonLog.error("本链不是主网");
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            LoggerUtil.commonLog.error("链不存在");
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        try {
            chainManager.setRegisteredCrossChainList(ChainManagerCall.getRegisteredChainInfo().getChainInfoList());
        } catch (Exception e) {
            chain.getLogger().error("跨链注册信息更新失败");
            chain.getLogger().error(e);
        }
        return Result.getSuccess(SUCCESS);
    }

    @Override
    public void getCrossChainList(int chainId, String nodeId, GetRegisteredChainMessage message) {
        try {
            int handleChainId = chainId;
            if (nulsCrossChainConfig.isMainNet()) {
                handleChainId = nulsCrossChainConfig.getMainChainId();
            }
            Chain chain = chainManager.getChainMap().get(handleChainId);
            chain.getLogger().info("收到友链节点{}查询已注册链列表消息！",nodeId);
            RegisteredChainMessage registeredChainMessage = ChainManagerCall.getRegisteredChainInfo();
            chain.getLogger().info("当前已注册跨链的链数量为:{}\n\n",registeredChainMessage.getChainInfoList().size());
            NetWorkCall.sendToNode(chainId, registeredChainMessage, nodeId, CommandConstant.REGISTERED_CHAIN_MESSAGE);
        }catch (Exception e){
            LoggerUtil.commonLog.error(e);
        }
    }

    @Override
    public void receiveCirculation(int chainId, String nodeId, CirculationMessage messageBody) {
        Chain chain = chainManager.getChainMap().get(nulsCrossChainConfig.getMainChainId());
        chain.getLogger().info("接收到友链:{}节点:{}发送的资产该链最新资产流通量信息\n\n", chainId, nodeId);
        try {
            ChainManagerCall.sendCirculation(chainId, messageBody);
        } catch (NulsException e) {
            chain.getLogger().error(e);
        }
    }


    @Override
    public Result getFriendChainCirculation(Map<String, Object> params) {
        if(params == null || params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.ASSET_IDS) == null){
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (Integer)params.get(ParamConstant.CHAIN_ID);
        GetCirculationMessage getCirculationMessage = new GetCirculationMessage();
        getCirculationMessage.setAssetIds((String)params.get(ParamConstant.ASSET_IDS));
        NetWorkCall.broadcast(chainId, getCirculationMessage, CommandConstant.GET_CIRCULLAT_MESSAGE,true);
        return Result.getSuccess(SUCCESS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result tokenOutCrossChain(Map<String, Object> params) {
        int chainId = Integer.valueOf(params.get(ParamConstant.CHAIN_ID).toString());
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        if (!chainManager.isCrossNetUseAble()) {
            chain.getLogger().info("跨链网络组网异常！");
            return Result.getFailed(CROSS_CHAIN_NETWORK_UNAVAILABLE);
        }
        int assetId = Integer.valueOf(params.get(ParamConstant.ASSET_ID).toString());
        String fromAddress = (String)params.get(ParamConstant.FROM);
        String toAddress = (String)params.get(ParamConstant.TO);
        BigInteger amount = new BigInteger((String)params.get(ParamConstant.VALUE));

        Transaction tx = new Transaction(TxType.CONTRACT_TOKEN_CROSS_TRANSFER);
        tx.setTime(NulsDateUtils.getCurrentTimeSeconds());

        CoinData coinData = new CoinData();
        List<CoinFrom> coinFromList = new ArrayList<>();
        CoinFrom coinFrom = new CoinFrom(AddressTool.getAddress(fromAddress),chainId,assetId,amount,NulsCrossChainConstant.CROSS_TOKEN_NONCE, NulsCrossChainConstant.CORSS_TX_LOCKED);
        coinFromList.add(coinFrom);
        List<CoinTo> coinToList = new ArrayList<>();
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(toAddress), chainId, assetId, amount);
        coinToList.add(coinTo);
        coinData.setFrom(coinFromList);
        coinData.setTo(coinToList);

        Map<String, Object> result = new HashMap<>(2);
        try {
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            CtxStatusPO ctxStatusPO = new CtxStatusPO(tx, TxStatusEnum.UNCONFIRM.getStatus());
            ctxStatusService.save(tx.getHash(), ctxStatusPO, chainId);
            BroadCtxSignMessage message = new BroadCtxSignMessage();
            message.setLocalHash(tx.getHash());
            message.setSignature(null);
            Map packerInfo = ConsensusCall.getPackerInfo(chain);
            if (packerInfo != null ) {
                String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
                String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);
                if(StringUtils.isNotBlank(address)){
                    List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
                    TransactionSignature transactionSignature = new TransactionSignature();
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, tx.getHash().getBytes());
                    p2PHKSignatures.add(p2PHKSignature);
                    transactionSignature.setP2PHKSignatures(p2PHKSignatures);
                    tx.setTransactionSignature(transactionSignature.serialize());
                    message.setSignature(p2PHKSignature.serialize());
                }
            }
            NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
            result.put(TX_HASH, tx.getHash().toHex());
            result.put(ParamConstant.TX, RPCUtil.encode(tx.serialize()));
            return Result.getSuccess(SUCCESS).setData(result);
        }catch (IOException e){
            Log.error(e);
            return Result.getFailed(SERIALIZE_ERROR);
        }catch (NulsException e){
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }
}

