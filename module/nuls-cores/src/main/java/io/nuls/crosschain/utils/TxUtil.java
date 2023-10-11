package io.nuls.crosschain.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.message.GetCtxStateMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.CrossTransferData;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainChangeData;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.base.model.bo.txdata.VerifierInitData;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.ParamConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.CtxStateEnum;
import io.nuls.crosschain.model.bo.message.WaitBroadSignMessage;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.rpc.call.AccountCall;
import io.nuls.crosschain.rpc.call.ConsensusCall;
import io.nuls.crosschain.rpc.call.NetWorkCall;
import io.nuls.crosschain.srorage.ConvertCtxService;
import io.nuls.crosschain.srorage.ConvertHashService;
import io.nuls.crosschain.srorage.CtxStateService;
import io.nuls.crosschain.srorage.CtxStatusService;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private static NulsCoresConfig config;
    @Autowired
    private static ConvertCtxService convertCtxService;
    @Autowired
    private static CtxStatusService ctxStatusService;
    @Autowired
    private static ConvertHashService convertHashService;
    @Autowired
    private static CtxStateService ctxStateService;
    @Autowired
    private static ChainManager chainManager;

    /**
     * 友链协议跨链交易转主网协议跨链交易
     * Friendly Chain Protocol Cross-Chain Transaction to Main Network Protocol Cross-Chain Transaction
     */
    public static Transaction friendConvertToMain(Chain chain, Transaction friendCtx, int ctxType) throws NulsException, IOException {
        return friendConvertToMain(chain,friendCtx,ctxType,false);
    }

    /**
     * 友链协议跨链交易转主网协议跨链交易
     * Friendly Chain Protocol Cross-Chain Transaction to Main Network Protocol Cross-Chain Transaction
     */
    public static Transaction friendConvertToMain(Chain chain, Transaction friendCtx, int ctxType, boolean needSign) throws NulsException, IOException {
        Transaction mainCtx = new Transaction(ctxType);
        mainCtx.setRemark(friendCtx.getRemark());
        mainCtx.setTime(friendCtx.getTime());
        mainCtx.setTxData(friendCtx.getTxData());
        //还原并重新结算CoinData
        CoinData realCoinData = friendCtx.getCoinDataInstance();
        restoreCoinData(realCoinData);
        mainCtx.setCoinData(realCoinData.serialize());
        int fromChainId = AddressTool.getChainIdByAddress(realCoinData.getFrom().get(0).getAddress());
        //如果是发起链则需要重构txData，将发起链的交易hash设置到txData中
        if(chain.getChainId() == fromChainId){
            CrossTransferData crossTransferData = new CrossTransferData();
            crossTransferData.parse(friendCtx.getTxData(),0);
            crossTransferData.setSourceHash(friendCtx.getHash().getBytes());
            mainCtx.setTxData(crossTransferData.serialize());
        }else{
            mainCtx.setTxData(friendCtx.getTxData());
        }
        if(needSign){
            mainCtx.setTransactionSignature(friendCtx.getTransactionSignature());
        }
        /*
        //如果是新建跨链交易则直接用账户信息签名，否则从原始签名中获取签名
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        int fromChainId = AddressTool.getChainIdByAddress(realCoinData.getFrom().get(0).getAddress());
        if (fromChainId == chain.getChainId()) {
            TransactionSignature originalSignature = new TransactionSignature();
            originalSignature.parse(friendCtx.getTransactionSignature(), 0);
            int signCount = realCoinData.getFromAddressCount();
            int size = originalSignature.getP2PHKSignatures().size();
            for (int index = signCount; index < size; index++) {
                p2PHKSignatures.add(originalSignature.getP2PHKSignatures().get(index));
            }
            if (!p2PHKSignatures.isEmpty()) {
                transactionSignature.setP2PHKSignatures(p2PHKSignatures);
                mainCtx.setTransactionSignature(transactionSignature.serialize());
            }
        } else {
            mainCtx.setTransactionSignature(friendCtx.getTransactionSignature());
        }*/

        chain.getLogger().debug("本链协议跨链交易转主网协议跨链交易完成!");
        return mainCtx;
    }

    /**
     * 主网协议跨链交易转友链协议跨链交易
     * Main Network Protocol Cross-Chain Transaction Transfer Chain Protocol Cross-Chain Transaction
     */
    public static Transaction mainConvertToFriend(Transaction mainCtx, int ctxType) {
        Transaction friendCtx = new Transaction(ctxType);
        friendCtx.setRemark(mainCtx.getRemark());
        friendCtx.setTime(mainCtx.getTime());
        friendCtx.setTxData(mainCtx.getTxData());
        friendCtx.setCoinData(mainCtx.getCoinData());
        return friendCtx;
    }

    /**
     * 组装验证人变更交易
     * Assemble Verifier Change Transaction
     */
    public static Transaction createVerifierChangeTx(List<String> registerAgentList, List<String> cancelAgentList, long time, int chainId) throws IOException {
        Transaction verifierChangeTx = new Transaction(TxType.VERIFIER_CHANGE);
        verifierChangeTx.setTime(time);
        if(registerAgentList != null){
            registerAgentList.sort(Comparator.naturalOrder());
        }
        if(cancelAgentList != null){
            cancelAgentList.sort(Comparator.naturalOrder());
        }
        VerifierChangeData verifierChangeData = new VerifierChangeData(registerAgentList, cancelAgentList, chainId);
        verifierChangeTx.setTxData(verifierChangeData.serialize());
        return verifierChangeTx;
    }

    /**
     * 组装验证人初始化交易
     * Assemble Verifier Change Transaction
     */
    public static Transaction createVerifierInitTx(List<String> verifierList, long time, int registerChainId) throws IOException {
        Transaction verifierInitTx = new Transaction(TxType.VERIFIER_INIT);
        verifierInitTx.setTime(time);
        VerifierInitData verifierInitData = new VerifierInitData(registerChainId, verifierList);
        verifierInitTx.setTxData(verifierInitData.serialize());
        return verifierInitTx;
    }

    /**
     * 组装注册跨链变更交易交易
     * Assemble Verifier Change Transaction
     */
    public static Transaction createCrossChainChangeTx(long time, int registerChainId, int type) throws IOException {
        Transaction crossChainChangeTx = new Transaction(TxType.REGISTERED_CHAIN_CHANGE);
        crossChainChangeTx.setTime(time);
        List<ChainInfo> chainInfoList = new ArrayList<>();
        RegisteredChainChangeData txData = new RegisteredChainChangeData(registerChainId, type, chainInfoList);
        crossChainChangeTx.setTxData(txData.serialize());
        return crossChainChangeTx;
    }

    /**
     * 组装注册跨链变更交易交易
     * Assemble Verifier Change Transaction
     */
    public static Transaction createCrossChainChangeTx(ChainInfo chainInfo, long time, int registerChainId, int type) throws IOException {
        Transaction crossChainChangeTx = new Transaction(TxType.REGISTERED_CHAIN_CHANGE);
        crossChainChangeTx.setTime(time);
        List<ChainInfo> chainInfoList = new ArrayList<>();
        chainInfoList.add(chainInfo);
        RegisteredChainChangeData txData = new RegisteredChainChangeData(registerChainId, type, chainInfoList);

        crossChainChangeTx.setTxData(txData.serialize());
        return crossChainChangeTx;
    }

    /**
     * 组装注册跨链变更交易交易
     * Assemble Verifier Change Transaction
     */
    public static Transaction createCrossChainChangeTx(List<ChainInfo> chainInfoList, long time, int registerChainId, int type) throws IOException {
        Transaction crossChainChangeTx = new Transaction(TxType.REGISTERED_CHAIN_CHANGE);
        crossChainChangeTx.setTime(time);
        RegisteredChainChangeData txData = new RegisteredChainChangeData(registerChainId, type, chainInfoList);
        crossChainChangeTx.setTxData(txData.serialize());
        return crossChainChangeTx;
    }

    /**
     * 验证人变更交易处理时需要等待高度变更
     * When the verifier changes the transaction processing, it needs to wait for the height change
     */
    public static void verifierChangeWait(Chain chain, long height) {
        while (chainManager.getChainHeaderMap().get(chain.getChainId()).getHeight() < height - 1) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                chain.getLogger().error(e);
            }
        }
    }

    /**
     * 本链发起的跨链交易被打包之后，发起拜占庭验证
     * After the cross chain transaction initiated by this chain is packaged, Byzantine verification is initiated
     */
    @SuppressWarnings("unchecked")
    public static void localCtxByzantine(Transaction ctx, Chain chain) {
        int chainId = chain.getChainId();
        NulsHash hash = ctx.getHash();
        try {
            Map packerInfo = ConsensusCall.getPackerInfo(chain);
            String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
            List<String> localPackers = (List<String>) packerInfo.get(ParamConstant.PARAM_ADDRESS + "es");

            List<String> packAddressList = (List<String>) packerInfo.get(ParamConstant.PARAM_PACK_ADDRESS_LIST);


            NulsHash convertHash = hash;
            if (!config.isMainNet()) {
                //txData中存储来源链交易hash和nuls主链交易hash，如果发起链是nuls主链，来源链hash和nuls主链hash相同。
                Transaction mainCtx = TxUtil.friendConvertToMain(chain, ctx, TxType.CROSS_CHAIN);
                convertHash = mainCtx.getHash();
                convertCtxService.save(hash, mainCtx, chainId);
            }
            CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
            //如果本节点是共识节点，则需要签名并做拜占庭，否则只需广播本地收集到的签名信息
            if (!localPackers.isEmpty()) {
                BroadCtxSignMessage message = new BroadCtxSignMessage();
                message.setLocalHash(hash);
                TransactionSignature transactionSignature = new TransactionSignature();
                if (ctx.getTransactionSignature() != null) {
                    transactionSignature.parse(ctx.getTransactionSignature(), 0);
                } else {
                    List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
                    transactionSignature.setP2PHKSignatures(p2PHKSignatures);
                }
                //循环本地所有打包地址
                for (String packerAddress : localPackers) {
                    if (!chain.getVerifierList().contains(packerAddress)) {
                        continue;
                    }
                    if (config.isMainNet()) {
                        if (ctx.getType() == TxType.CROSS_CHAIN && ctx.getCoinDataInstance().getFromAddressList().contains(packerAddress)) {
                            message.setSignature(transactionSignature.getP2PHKSignatures().get(0).serialize());
                        } else {
                            P2PHKSignature p2PHKSignature = AccountCall.signDigest(packerAddress, password, hash.getBytes());
                            transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
                            message.setSignature(p2PHKSignature.serialize());
                        }
                    } else {
                        P2PHKSignature p2PHKSignature = AccountCall.signDigest(packerAddress, password, convertHash.getBytes());
                        transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
                        message.setSignature(p2PHKSignature.serialize());
                    }
                    NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
                }
                MessageUtil.signByzantineInChain(chain, ctx, transactionSignature, packAddressList, hash);
            } else {
                ctxStatusService.save(hash, ctxStatusPO, chainId);
            }
            //将收到的签名消息加入消息队列
            if (chain.getFutureMessageMap().containsKey(hash)) {
                chain.getLogger().debug("将本跨链交易:{}已收到的签名放入消息队列中", hash.toHex());
                chain.getSignMessageByzantineQueue().addAll(chain.getFutureMessageMap().remove(hash));
            }
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
        }
    }


    /**
     * 重置平行链存储的主链验证人列表
     * Cross-Chain Transaction Processing
     */
    @SuppressWarnings("unchecked")
    public static void handleResetOtherVerifierListCtx(Transaction ctx, Chain chain) {
        int chainId = chain.getChainId();
        NulsHash hash = ctx.getHash();
        String hashHex = hash.toHex();
        /*
        判断本节点是否为共识节点，如果为共识节点则签名，如果不为共识节点则广播该交易
        */
        Map packerInfo;
        List<String> verifierList = chain.getVerifierList();
        packerInfo = ConsensusCall.getPackerInfo(chain);
        String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
        String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);
        BroadCtxSignMessage message = new BroadCtxSignMessage();
        message.setLocalHash(hash);
        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        boolean byzantinePass = false;
        //验证人变更，减少的验证人不签名
        boolean sign = verifierList.contains(address);
        if (sign) {
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
                byzantinePass = MessageUtil.verifierInitLocalByzantine(chain, ctx, signature, verifierList,hash,1F);
            } catch (Exception e) {
                chain.getLogger().error(e);
                chain.getLogger().error("签名错误!,hash:{}", hashHex);
                return;
            }
            if (!chain.getWaitBroadSignMap().keySet().contains(hash)) {
                chain.getWaitBroadSignMap().put(hash, new HashSet<>());
            }
            /*
            保存并广播该交易
            */
            chain.getWaitBroadSignMap().get(hash).add(new WaitBroadSignMessage(null, message));
        }else{
            chain.getLogger().debug("本节点不是共识节点，不对交易此交易进行签名,Hash:{}",hashHex);
            ctxStatusService.save(hash, ctxStatusPO, chainId);
        }
        if (byzantinePass) {
            chain.getFutureMessageMap().remove(hash);
        } else {
            if (chain.getFutureMessageMap().containsKey(hash)) {
                chain.getSignMessageByzantineQueue().addAll(chain.getFutureMessageMap().remove(hash));
            }
        }
        MessageUtil.broadcastCtx(chain, hash, chainId, hashHex);
    }


    /**
     * 跨链交易处理
     * Cross-Chain Transaction Processing
     */
    @SuppressWarnings("unchecked")
    public static void handleNewCtx(Transaction ctx, Chain chain, List<String> cancelList) {
        int chainId = chain.getChainId();
        NulsHash hash = ctx.getHash();
        String hashHex = hash.toHex();
        /*
        判断本节点是否为共识节点，如果为共识节点则签名，如果不为共识节点则广播该交易
        */
        Map packerInfo = ConsensusCall.getPackerInfo(chain);
        List<String> localPackers = (List<String>) packerInfo.get(ParamConstant.PARAM_ADDRESS + "es");
        List<String> verifierList = chain.getVerifierList();
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            packerInfo = ConsensusCall.getSeedNodeList(chain);
            verifierList = (List<String>) packerInfo.get(ParamConstant.PARAM_PACK_ADDRESS_LIST);
        } else {
            packerInfo = ConsensusCall.getPackerInfo(chain);
        }
        String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
        String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);

        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        boolean byzantinePass = false;
        //验证人变更，减少的验证人不签名
        boolean sign = !StringUtils.isBlank(address) && verifierList.contains(address);
        if (sign && cancelList != null) {
            sign = !cancelList.contains(address);
        }
        if (sign) {
            chain.getLogger().info("本节点为共识节点，对跨链交易签名,Hash:{}", hashHex);
            TransactionSignature signature = new TransactionSignature();
            HashSet<WaitBroadSignMessage> messageList = new HashSet<>();
            List<P2PHKSignature> p2PHKSignatureList = new ArrayList<>();
            for (var packageAddress : localPackers){
                P2PHKSignature p2PHKSignature;
                try {
                    p2PHKSignature = AccountCall.signDigest(packageAddress, password, hash.getBytes());
                    BroadCtxSignMessage message = new BroadCtxSignMessage();
                    message.setLocalHash(hash);
                    message.setSignature(p2PHKSignature.serialize());
                    messageList.add(new WaitBroadSignMessage(null, message));
                    p2PHKSignatureList.add(p2PHKSignature);
                } catch (Exception e) {
                    chain.getLogger().error(e);
                    chain.getLogger().error("签名错误!,hash:{}", hashHex);
                    return;
                }
            }
            try{
                signature.setP2PHKSignatures(p2PHKSignatureList);
                ctx.setTransactionSignature(signature.serialize());
                byzantinePass = MessageUtil.signByzantineInChain(chain, ctx, signature, verifierList, hash);
            } catch (Exception e) {
                chain.getLogger().error(e);
                chain.getLogger().error("签名错误!,hash:{}", hashHex);
                return;
            }
            /*
            保存并广播该交易
            */
            if (!chain.getWaitBroadSignMap().keySet().contains(hash)) {
                chain.getWaitBroadSignMap().put(hash, messageList);
            }else{
                chain.getWaitBroadSignMap().get(hash).addAll(messageList);
            }
        } else {
            ctxStatusService.save(hash, ctxStatusPO, chainId);
        }
        if (!config.isMainNet()) {
            convertHashService.save(hash, hash, chainId);
        }

        if (byzantinePass) {
            chain.getFutureMessageMap().remove(hash);
        } else {
            if (chain.getFutureMessageMap().containsKey(hash)) {
                chain.getSignMessageByzantineQueue().addAll(chain.getFutureMessageMap().remove(hash));
            }
        }
        MessageUtil.broadcastCtx(chain, hash, chainId, hashHex);
    }

    /**
     * 签名并广播交易（同步过程中的跨链交易只签名广播不做其他处理）
     * Sign and broadcast transactions
     *
     * @param chain 链信息
     * @param ctx   跨链交易
     */
    @SuppressWarnings("unchecked")
    public static void signAndBroad(Chain chain, Transaction ctx) {
        Map packerInfo;
        List<String> verifierList = chain.getVerifierList();
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            packerInfo = ConsensusCall.getSeedNodeList(chain);
            verifierList = (List<String>) packerInfo.get(ParamConstant.PARAM_PACK_ADDRESS_LIST);
        } else {
            packerInfo = ConsensusCall.getPackerInfo(chain);
        }
        String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
        String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);
        boolean sign = !StringUtils.isBlank(address) && verifierList.contains(address);
        if (!sign) {
            return;
        }
        BroadCtxSignMessage message = new BroadCtxSignMessage();
        message.setLocalHash(ctx.getHash());
        Transaction realTx = ctx;
        try {
            if (ctx.getType() == TxType.CROSS_CHAIN) {
                //如果不是主网则转为主网协议跨链交易
                if (!config.isMainNet()) {
                    realTx = TxUtil.friendConvertToMain(chain, ctx, TxType.CROSS_CHAIN);
                }
            }
            P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, realTx.getHash().getBytes());
            message.setSignature(p2PHKSignature.serialize());
            NetWorkCall.broadcast(chain.getChainId(), message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
        } catch (IOException | NulsException e) {
            chain.getLogger().error(e);
        }
    }


    /**
     * 查询跨链交易处理状态
     */
    public static byte getCtxState(Chain chain, NulsHash ctxHash) {
        int chainId = chain.getChainId();
        //查看本交易是否已经存在查询处理成功记录，如果有直接返回，否则需向主网节点验证
        if (ctxStateService.get(ctxHash.getBytes(), chainId)) {
            return CtxStateEnum.CONFIRMED.getStatus();
        }
        try {
            CtxStatusPO ctxStatusPO = ctxStatusService.get(ctxHash, chainId);
            int fromChainId = AddressTool.getChainIdByAddress(ctxStatusPO.getTx().getCoinDataInstance().getFrom().get(0).getAddress());
            if (chainId == fromChainId && ctxStatusPO.getStatus() != TxStatusEnum.CONFIRMED.getStatus()) {
                return CtxStateEnum.UNCONFIRM.getStatus();
            }
            GetCtxStateMessage message = new GetCtxStateMessage();
            NulsHash requestHash = ctxHash;
            int linkedChainId = chainId;
            if (!config.isMainNet()) {
                requestHash = friendConvertToMain(chain, ctxStatusPO.getTx(), TxType.CROSS_CHAIN).getHash();
            } else {
                linkedChainId = AddressTool.getChainIdByAddress(ctxStatusPO.getTx().getCoinDataInstance().getTo().get(0).getAddress());
            }
            if (MessageUtil.canSendMessage(chain, linkedChainId) != 2) {
                return CtxStateEnum.UNCONFIRM.getStatus();
            }
            message.setRequestHash(requestHash);
            NetWorkCall.broadcast(linkedChainId, message, CommandConstant.GET_CTX_STATE_MESSAGE, true);
            if (!chain.getCtxStateMap().containsKey(requestHash)) {
                chain.getCtxStateMap().put(requestHash, new ArrayList<>());
            }
            //统计处理结果
            byte result = statisticsCtxState(chain, linkedChainId, requestHash);
            if (result == CtxStateEnum.CONFIRMED.getStatus()) {
                ctxStateService.save(ctxHash.getBytes(), chainId);
            }
            return result;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return CtxStateEnum.UNCONFIRM.getStatus();
        }
    }

    private static byte statisticsCtxState(Chain chain, int linkedChainId, NulsHash requestHash) {
        byte ctxState = CtxStateEnum.UNCONFIRM.getStatus();
        try {
            int tryCount = 0;
            int linkedNode = NetWorkCall.getAvailableNodeAmount(linkedChainId, true);
            Map<Byte, Integer> ctxStateMap = new HashMap<>(4);
            while (tryCount < NulsCrossChainConstant.BYZANTINE_TRY_COUNT) {
                for (Byte state : chain.getCtxStateMap().get(requestHash)) {
                    if (ctxStateMap.containsKey(state)) {
                        int count = ctxStateMap.get(state);
                        count++;
                        if (count >= linkedNode / 2) {
                            return state;
                        }
                    } else {
                        ctxStateMap.put(state, 1);
                    }
                }
                if (chain.getCtxStateMap().get(requestHash).size() >= linkedNode) {
                    break;
                }
                Thread.sleep(2000);
                tryCount++;
            }
            int maxCount = 0;
            for (Map.Entry<Byte, Integer> entry : ctxStateMap.entrySet()) {
                int value = entry.getValue();
                if (value > maxCount) {
                    maxCount = value;
                    ctxState = entry.getKey();
                }
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
            return ctxState;
        } finally {
            chain.getCtxStateMap().remove(requestHash);
        }
        return ctxState;
    }


    /**
     * 还原本链协议CoinData
     * Restore the Chain Protocol CoinData
     */
    private static void restoreCoinData(CoinData coinData) {
        //资产与手续费 key:assetChainId_assetId   value:from中该资产 - to中该资产总额
        Map<String, BigInteger> assetMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
        String key;
        String mainKey = config.getMainChainId() + "_" + config.getMainAssetId();
        for (Coin coin : coinData.getFrom()) {
            key = coin.getAssetsChainId() + "_" + coin.getAssetsId();
            if (assetMap.containsKey(key)) {
                BigInteger amount = assetMap.get(key).add(coin.getAmount());
                assetMap.put(key, amount);
            } else {
                assetMap.put(key, coin.getAmount());
            }
        }
        for (Coin coin : coinData.getTo()) {
            key = coin.getAssetsChainId() + "_" + coin.getAssetsId();
            BigInteger amount = assetMap.get(key).subtract(coin.getAmount());
            assetMap.put(key, amount);
        }
        for (Map.Entry<String, BigInteger> entry : assetMap.entrySet()) {
            String entryKey = entry.getKey();
            if (entryKey.equals(mainKey)) {
                continue;
            }
            BigInteger entryValue = entry.getValue();
            Iterator<CoinFrom> it = coinData.getFrom().iterator();
            while (it.hasNext()) {
                Coin coin = it.next();
                key = coin.getAssetsChainId() + "_" + coin.getAssetsId();
                if (entryKey.equals(key)) {
                    if (coin.getAmount().compareTo(entryValue) > 0) {
                        coin.setAmount(coin.getAmount().subtract(entryValue));
                        break;
                    } else {
                        it.remove();
                        entryValue = entryValue.subtract(coin.getAmount());
                    }
                }
            }
        }
    }

    /**
     * 跨链交易签名拜占庭验证
     * Byzantine Verification of Cross-Chain Transaction Signature
     */
    public static boolean signByzantineVerify(Chain chain, Transaction ctx, List<String> verifierList, int byzantineCount, int verifierChainId) throws NulsException {
        TransactionSignature transactionSignature = new TransactionSignature();
        try {
            transactionSignature.parse(ctx.getTransactionSignature(), 0);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            throw e;
        }
        //由于在3505754高度之前又验证人列表丢失的bug，所以在此高度之前只要有5个种子节点签名的交易就可以验证通过
        if (ctx.getBlockHeight() > 3505754 && transactionSignature.getP2PHKSignatures().size() < byzantineCount) {
            chain.getLogger().error("跨链交易签名数量小于拜占庭数量，Hash:{},signCount:{},byzantineCount:{}", ctx.getHash().toHex(), transactionSignature.getP2PHKSignatures().size(), byzantineCount);
            return false;
        }
        chain.getLogger().debug("当前验证人列表：{}", verifierList.toString());
        Iterator<P2PHKSignature> iterator = transactionSignature.getP2PHKSignatures().iterator();
        int passCount = 0;
        Set<String> passedAddress = new HashSet<>();
        while (iterator.hasNext()) {
            P2PHKSignature signature = iterator.next();
            for (String verifier : verifierList) {
                if (passedAddress.contains(verifier)) {
                    continue;
                }
                if (Arrays.equals(AddressTool.getAddress(signature.getPublicKey(), verifierChainId), AddressTool.getAddress(verifier))) {
                    passedAddress.add(verifier);
                    passCount++;
                    break;
                }
            }
        }
        //由于在3505754高度之前又验证人列表丢失的bug，所以在此高度之前只要有5个种子节点签名的交易就可以验证通过
        if (ctx.getBlockHeight() <= 3505754 && passCount == 5) {
            return true;
        }
        if (passCount < byzantineCount ) {
            chain.getLogger().error("跨链交易签名验证通过数小于拜占庭数量，Hash:{},passCount:{},byzantineCount:{}", ctx.getHash().toHex(), passCount, byzantineCount);
            return false;
        }
        return true;
    }
}
