package io.nuls.crosschain.nuls.utils;

import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.AccountCall;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.srorage.ConvertCtxService;
import io.nuls.crosschain.nuls.srorage.ConvertHashService;
import io.nuls.crosschain.nuls.srorage.NewCtxService;
import io.nuls.crosschain.nuls.utils.manager.CoinDataManager;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 交易工具类
 * Transaction Tool Class
 *
 * @author tag
 * 2019/4/15
 */
@Component
public class TxUtil {
    @Autowired
    private static NulsCrossChainConfig config;
    @Autowired
    private static NewCtxService newCtxService;
    @Autowired
    private static ConvertCtxService convertCtxService;
    /**
     * 友链协议跨链交易转主网协议跨链交易
     * Friendly Chain Protocol Cross-Chain Transaction to Main Network Protocol Cross-Chain Transaction
     */
    public static Transaction friendConvertToMain(Chain chain, Transaction friendCtx, Map<String, String> signedAddressMap, int ctxType) throws NulsException, IOException {
        Transaction mainCtx = new Transaction(ctxType);
        mainCtx.setRemark(friendCtx.getRemark());
        mainCtx.setTime(friendCtx.getTime());
        mainCtx.setTxData(friendCtx.getHash().getBytes());
        //还原并重新结算CoinData
        CoinData realCoinData = friendCtx.getCoinDataInstance();
        restoreCoinData(realCoinData);
        mainCtx.setCoinData(realCoinData.serialize());

        //如果是新建跨链交易则直接用账户信息签名，否则从原始签名中获取签名
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        if (signedAddressMap != null && !signedAddressMap.isEmpty()) {
            for (Map.Entry<String, String> entry : signedAddressMap.entrySet()) {
                P2PHKSignature p2PHKSignature = AccountCall.signDigest(entry.getKey(), entry.getValue(), mainCtx.getHash().getBytes());
                p2PHKSignatures.add(p2PHKSignature);
            }
        } else {
            TransactionSignature originalSignature = new TransactionSignature();
            originalSignature.parse(friendCtx.getTransactionSignature(), 0);
            int signCount = realCoinData.getFromAddressCount();
            int size = originalSignature.getP2PHKSignatures().size();
            for (int index = signCount - 1;index < size;index++ ){
                p2PHKSignatures.add(originalSignature.getP2PHKSignatures().get(index));
            }
        }
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        mainCtx.setTransactionSignature(transactionSignature.serialize());
        chain.getLogger().debug("本链协议跨链交易转主网协议跨链交易完成!");
        return mainCtx;
    }

    /**
     * 主网协议跨链交易转友链协议跨链交易
     * Main Network Protocol Cross-Chain Transaction Transfer Chain Protocol Cross-Chain Transaction
     */
    public static Transaction mainConvertToFriend(Transaction mainCtx, int ctxType){
        Transaction friendCtx = new Transaction(ctxType);
        friendCtx.setRemark(mainCtx.getRemark());
        friendCtx.setTime(mainCtx.getTime());
        friendCtx.setTxData(mainCtx.getHash().getBytes());
        friendCtx.setCoinData(mainCtx.getCoinData());
        return friendCtx;
    }

    /**
     * 组装验证人变更交易
     * Assemble Verifier Change Transaction
     * */
    public static Transaction createVerifierChangeTx(List<String> registerAgentList,List<String> cancelAgentList,long time,int chainId)throws IOException{
        Transaction verifierChangeTx = new Transaction(TxType.VERIFIER_CHANGE);
        verifierChangeTx.setTime(time);
        VerifierChangeData verifierChangeData = new VerifierChangeData(registerAgentList, cancelAgentList, chainId);
        verifierChangeTx.setTxData(verifierChangeData.serialize());
        return verifierChangeTx;
    }


    /**
     * 跨链交易处理
     * Cross-Chain Transaction Processing
     * */
    @SuppressWarnings("unchecked")
    public static void  handleNewCtx(Transaction ctx, Chain chain){
        int chainId = chain.getChainId();
        chain.getCtxStageMap().put(ctx.getHash(), 2);
        NulsHash hash = ctx.getHash();
        String hashHex = hash.toHex();
        /*
        判断本节点是否收到过该交易
        */
        if(newCtxService.get(ctx.getHash(), chainId) != null || NulsCrossChainConstant.CTX_STATE_PROCESSING.equals(chain.getCtxStageMap().get(hash))){
            chain.getLogger().info("已经收到过该交易,hash:{}",hashHex);
            return;
        }
        chain.getCtxStageMap().putIfAbsent(hash, NulsCrossChainConstant.CTX_STATE_PROCESSING);
        /*
        判断本节点是否为共识节点，如果为共识节点则签名，如果不为共识节点则广播该交易
        */
        Map packerInfo = ConsensusCall.getPackerInfo(chain);
        String password = (String) packerInfo.get("password");
        String address = (String) packerInfo.get("address");
        BroadCtxSignMessage message = new BroadCtxSignMessage();
        message.setLocalHash(hash);
        if (!StringUtils.isBlank(address)) {
            chain.getLogger().info("本节点为共识节点，对跨链交易签名,Hash:{}", hashHex);
            P2PHKSignature p2PHKSignature;
            try {
                p2PHKSignature = AccountCall.signDigest(address, password, hash.getBytes());
                message.setSignature(p2PHKSignature.serialize());
                TransactionSignature signature = new TransactionSignature();
                List<P2PHKSignature> p2PHKSignatureList = new ArrayList<>();
                p2PHKSignatureList.add(p2PHKSignature);
                signature.setP2PHKSignatures(p2PHKSignatureList);
                ctx.setTransactionSignature(signature.serialize());
                MessageUtil.signByzantineInChain(chain, ctx, signature, (List<String>) packerInfo.get("packAddressList"));
            }catch (Exception e){
                chain.getLogger().error(e);
                chain.getLogger().error("签名错误!,hash:{}",hashHex);
                chain.getCtxStageMap().remove(hash);
                return;
            }
        }
        /*
        保存并广播该交易
        */
        if (!chain.getWaitBroadSignMap().keySet().contains(hash)) {
            chain.getWaitBroadSignMap().put(hash, new HashSet<>());
        }
        chain.getWaitBroadSignMap().get(hash).add(message);
        newCtxService.save(hash, ctx, chainId);
        convertCtxService.save(hash, ctx, chainId);
        MessageUtil.broadcastCtx(chain,hash,chainId,hashHex);
        chain.getCtxStageMap().remove(hash);
    }


    /**
     * 还原本链协议CoinData
     * Restore the Chain Protocol CoinData
     * */
    private static void restoreCoinData(CoinData coinData){
        //资产与手续费 key:assetChainId_assetId   value:from中该资产 - to中该资产总额
        Map<String, BigInteger> assetMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
        String key;
        String mainKey = config.getMainChainId() +"_"+ config.getMainAssetId();
        for (Coin coin:coinData.getFrom()) {
            key = coin.getAssetsChainId()+"_"+coin.getAssetsId();
            if(assetMap.containsKey(key)){
                BigInteger amount = assetMap.get(key).add(coin.getAmount());
                assetMap.put(key, amount);
            }else{
                assetMap.put(key, coin.getAmount());
            }
        }
        for (Coin coin:coinData.getTo()) {
            key = coin.getAssetsChainId()+"_"+coin.getAssetsId();
            BigInteger amount = assetMap.get(key).subtract(coin.getAmount());
            assetMap.put(key, amount);
        }
        for (Map.Entry<String, BigInteger> entry:assetMap.entrySet()) {
            String entryKey = entry.getKey();
            if(entryKey.equals(mainKey)){
                continue;
            }
            BigInteger entryValue = entry.getValue();
            Iterator<CoinFrom> it = coinData.getFrom().iterator();
            while (it.hasNext()){
                Coin coin = it.next();
                key = coin.getAssetsChainId()+"_"+coin.getAssetsId();
                if(entryKey.equals(key)){
                    if(coin.getAmount().compareTo(entryValue) > 0){
                        coin.setAmount(coin.getAmount().subtract(entryValue));
                        break;
                    }else{
                        it.remove();
                        entryValue = entryValue.subtract(coin.getAmount());
                    }
                }
            }
        }
    }
}
