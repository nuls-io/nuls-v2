package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.*;
import io.nuls.crosschain.base.model.bo.AssetInfo;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.base.utils.enumeration.ChainInfoChangeType;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.*;
import io.nuls.crosschain.nuls.servive.MainNetService;
import io.nuls.crosschain.nuls.srorage.CtxStatusService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.LoggerUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import io.nuls.crosschain.nuls.utils.manager.LocalVerifierManager;
import io.nuls.crosschain.nuls.utils.thread.CrossTxHandler;
import io.nuls.crosschain.nuls.utils.validator.CrossTxValidator;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.core.constant.CommonCodeConstanst.DATA_PARSE_ERROR;
import static io.nuls.core.constant.CommonCodeConstanst.PARAMETER_ERROR;
import static io.nuls.core.constant.CommonCodeConstanst.SERIALIZE_ERROR;
import static io.nuls.core.constant.CommonCodeConstanst.SUCCESS;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.*;
import static io.nuls.crosschain.nuls.constant.ParamConstant.CHAIN_ID;
import static io.nuls.crosschain.nuls.constant.ParamConstant.TX_HASH;


/**
 * 主网跨链模块特有方法
 *
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

    @Autowired
    private CrossTxValidator txValidator;

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
        if (registeredChainMessage == null || registeredChainMessage.getChainInfoList().isEmpty()) {
            registeredChainMessage = new RegisteredChainMessage();
            List<ChainInfo> chainInfoList = new ArrayList<>();
            registeredChainMessage.setChainInfoList(chainInfoList);
        }
        registeredChainMessage.addChainInfo(chainInfo);
        registeredCrossChainService.save(registeredChainMessage);
        chainManager.setRegisteredCrossChainList(registeredChainMessage.getChainInfoList());
        if(chain.getVerifierList() == null || chain.getVerifierList().isEmpty()){
            chain.getLogger().info("The first time the primary network has chain registration, cross chain initialization and local verification list");
            boolean result = LocalVerifierManager.initLocalVerifier(chain, (List<String>) ConsensusCall.getPackerInfo(chain).get(ParamConstant.PARAM_PACK_ADDRESS_LIST));
            if(!result){
                return Result.getFailed(DB_SAVE_ERROR);
            }
        }
        chain.getLogger().info("有新链注册跨链，chainID:{},初始验证人列表：{}", chainInfo.getChainId(), chainInfo.getVerifierList().toString());
        //创建验证人初始化交易
        try {
            int syncStatus = BlockCall.getBlockStatus(chain);
            chain.getCrossTxThreadPool().execute(new CrossTxHandler(chain, TxUtil.createVerifierInitTx(chain.getVerifierList(), chainInfo.getRegisterTime(), chainInfo.getChainId()),syncStatus));

            if(registeredChainMessage.haveOtherChain(chainInfo.getChainId(), chain.getChainId())){
                chain.getLogger().info("将新注册的链信息广播给已注册的链");
                chain.getCrossTxThreadPool().execute(new CrossTxHandler(chain, TxUtil.createCrossChainChangeTx(chainInfo,chainInfo.getRegisterTime(),chainInfo.getChainId(), ChainInfoChangeType.NEW_REGISTER_CHAIN.getType()),syncStatus));

            }
        } catch (IOException e) {
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
        Chain chain = chainManager.getChainMap().get(nulsCrossChainConfig.getMainChainId());
        int chainId = (int) params.get(ParamConstant.CHAIN_ID);
        int assetId = (int) params.get(ParamConstant.ASSET_ID);
        String symbol = (String) params.get(ParamConstant.SYMBOL);
        String assetName = (String) params.get(ParamConstant.ASSET_NAME);
        boolean usable = (boolean) params.get(ParamConstant.USABLE);
        int decimalPlaces = (int) params.get(ParamConstant.DECIMAL_PLACES);
        long time = (int) params.get(ParamConstant.PARAM_TIME);
        AssetInfo assetInfo = new AssetInfo(assetId, symbol, assetName, usable, decimalPlaces);
        chainManager.getChainInfo(chainId).getAssetInfoList().add(assetInfo);
        ChainInfo chainInfo = chainManager.getChainInfo(chainId);
        //本地数据库保存最新的资产信息
        RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
        registeredChainMessage.setChainInfoList(chainManager.getRegisteredCrossChainList());
        registeredCrossChainService.save(registeredChainMessage);
        try {
            int syncStatus = BlockCall.getBlockStatus(chain);
            chain.getLogger().info("新跨链资产注册，chainId:{},assetId:{}",chainId,assetId);
            chain.getCrossTxThreadPool().execute(new CrossTxHandler(chain, TxUtil.createCrossChainChangeTx(chainInfo,time,chainInfo.getChainId(), ChainInfoChangeType.REGISTERED_CHAIN_CHANGE.getType()),syncStatus));
        }catch (IOException e){
            chain.getLogger().error(e);
            return Result.getFailed(DATA_PARSE_ERROR);
        }
        return Result.getSuccess(SUCCESS);
    }

    @Override
    public Result cancelCrossChain(Map<String, Object> params) {
        if (params == null || params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.ASSET_ID) == null) {
            LoggerUtil.commonLog.error("参数错误");
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int) params.get(ParamConstant.CHAIN_ID);
        int assetId = (int) params.get(ParamConstant.ASSET_ID);
        long time = (int) params.get(ParamConstant.PARAM_TIME);
        RegisteredChainMessage registeredChainMessage = registeredCrossChainService.get();
        Chain chain = chainManager.getChainMap().get(nulsCrossChainConfig.getMainChainId());
        int syncStatus = BlockCall.getBlockStatus(chain);
        boolean chainInvalid = true;
        ChainInfo realChainInfo = null;
        chain.getLogger().info("跨链资产注销，chainId:{},assetId:{}",chainId,assetId);
        if (assetId == 0) {
            registeredChainMessage.getChainInfoList().removeIf(chainInfo -> chainInfo.getChainId() == chainId);
        } else {
            for (ChainInfo chainInfo : registeredChainMessage.getChainInfoList()) {
                if (chainInfo.getChainId() == chainId) {
                    realChainInfo = chainInfo;
                    for (AssetInfo assetInfo : chainInfo.getAssetInfoList()) {
                        if (assetInfo.getAssetId() == assetId) {
                            assetInfo.setUsable(false);
                        }
                        if(assetInfo.isUsable()){
                            chainInvalid = false;
                        }
                    }
                    break;
                }
            }
            if(chainInvalid){
                registeredChainMessage.getChainInfoList().removeIf(chainInfo -> chainInfo.getChainId() == chainId);
            }
        }
        try {
            if(chainInvalid){
                chain.getLogger().info("注销链，chainId:{}",chainId);
                chain.getCrossTxThreadPool().execute(new CrossTxHandler(chain, TxUtil.createCrossChainChangeTx(time,chainId, ChainInfoChangeType.REGISTERED_CHAIN_CHANGE.getType()),syncStatus));
            }else{
                chain.getCrossTxThreadPool().execute(new CrossTxHandler(chain, TxUtil.createCrossChainChangeTx(realChainInfo,time,chainId, ChainInfoChangeType.REGISTERED_CHAIN_CHANGE.getType()),syncStatus));
            }
        }catch (IOException e){
            chain.getLogger().error(e);
            return Result.getFailed(DATA_PARSE_ERROR);
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
        if (params == null || params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.ASSET_IDS) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(ParamConstant.CHAIN_ID);
        GetCirculationMessage getCirculationMessage = new GetCirculationMessage();
        getCirculationMessage.setAssetIds((String) params.get(ParamConstant.ASSET_IDS));
        NetWorkCall.broadcast(chainId, getCirculationMessage, CommandConstant.GET_CIRCULLAT_MESSAGE, true);
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
        String fromAddress = (String) params.get(ParamConstant.FROM);
        String toAddress = (String) params.get(ParamConstant.TO);
        BigInteger amount = new BigInteger((String) params.get(ParamConstant.VALUE));
        String contractAddress = (String) params.get("contractAddress");
        String contractToken = (String) params.get("contractNonce");
        String contractBalance = (String) params.get("contractBalance");
        long blockTime = Long.valueOf(params.get("blockTime").toString());
        Transaction tx = new Transaction(TxType.CONTRACT_TOKEN_CROSS_TRANSFER);
        tx.setTime(blockTime);
        tx.setTxData(ByteUtils.intToBytes(TxType.CONTRACT_TOKEN_CROSS_TRANSFER));
        CoinData coinData = new CoinData();
        List<CoinFrom> coinFromList = new ArrayList<>();
        CoinFrom coinFrom = new CoinFrom(AddressTool.getAddress(fromAddress), chainId, assetId, amount, NulsCrossChainConstant.CROSS_TOKEN_NONCE, NulsCrossChainConstant.CORSS_TX_LOCKED);
        coinFromList.add(coinFrom);
        List<CoinTo> coinToList = new ArrayList<>();
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(toAddress), chainId, assetId, amount);
        coinToList.add(coinTo);
        coinData.setFrom(coinFromList);
        coinData.setTo(coinToList);

        Map<String, Object> result = new HashMap<>(2);
        try {
            int txSize = tx.size();
            txSize += P2PHKSignature.SERIALIZE_LENGTH;
            txSize += coinData.size();
            //计算手续费
            BigInteger targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
            CoinFrom feeFrom = new CoinFrom(AddressTool.getAddress(contractAddress), nulsCrossChainConfig.getMainChainId(), nulsCrossChainConfig.getMainAssetId(), targetFee, HexUtil.decode(contractToken), (byte) 0);
            txSize += feeFrom.size();
            targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
            feeFrom.setAmount(targetFee);
            BigInteger available = new BigInteger(contractBalance);
            if (BigIntegerUtils.isLessThan(available, targetFee)) {
                chain.getLogger().warn("手续费不足");
                return Result.getFailed(INSUFFICIENT_FEE);
            }
            coinData.addFrom(feeFrom);
            tx.setCoinData(coinData.serialize());
            if(!txValidator.validateTx(chain, tx, null)){
                chain.getLogger().error("Transaction validation failed");
                return Result.getFailed(COINDATA_VERIFY_FAIL);
            }
            result.put(TX_HASH, tx.getHash().toHex());
            result.put(ParamConstant.TX, RPCUtil.encode(tx.serialize()));
            return Result.getSuccess(SUCCESS).setData(result);
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(SERIALIZE_ERROR);
        }catch (NulsException e){
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }
}

