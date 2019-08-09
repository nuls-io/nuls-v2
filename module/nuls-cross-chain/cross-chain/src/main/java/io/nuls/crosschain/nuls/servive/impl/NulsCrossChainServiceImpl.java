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
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.model.dto.input.CoinDTO;
import io.nuls.crosschain.base.model.dto.input.CrossTxTransferDTO;
import io.nuls.crosschain.base.service.CrossChainService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPO;
import io.nuls.crosschain.nuls.rpc.call.AccountCall;
import io.nuls.crosschain.nuls.rpc.call.ChainManagerCall;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.srorage.*;
import io.nuls.crosschain.nuls.utils.CommonUtil;
import io.nuls.crosschain.nuls.utils.MessageUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import io.nuls.crosschain.nuls.utils.manager.CoinDataManager;
import io.nuls.crosschain.nuls.utils.validator.CrossTxValidator;

import java.io.IOException;
import java.util.*;

import static io.nuls.crosschain.nuls.constant.NulsCrossChainConstant.*;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.*;
import static io.nuls.crosschain.nuls.constant.ParamConstant.*;

/**
 * 跨链模块默认接口实现类
 *
 * @author tag
 * @date 2019/4/9
 */
@Component
public class NulsCrossChainServiceImpl implements CrossChainService {
    @Autowired
    private ChainManager chainManager;

    @Autowired
    private NulsCrossChainConfig config;

    @Autowired
    private CoinDataManager coinDataManager;

    @Autowired
    private CrossTxValidator txValidator;

    @Autowired
    private SendHeightService sendHeightService;

    @Autowired
    private ConvertHashService convertHashService;

    @Autowired
    private CtxStateService ctxStateService;

    @Autowired
    private ConvertCtxService convertCtxService;

    @Autowired
    private CtxStatusService ctxStatusService;

    @Autowired
    private CommitedOtherCtxService otherCtxService;

    private Map<Integer, Set<NulsHash>> verifiedCtxMap = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public Result createCrossTx(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        CrossTxTransferDTO crossTxTransferDTO = JSONUtils.map2pojo(params, CrossTxTransferDTO.class);
        int chainId = crossTxTransferDTO.getChainId();
        if (chainId <= CHAIN_ID_MIN) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        if (!chainManager.isCrossNetUseAble()) {
            chain.getLogger().info("跨链网络组网异常！");
            return Result.getFailed(CROSS_CHAIN_NETWORK_UNAVAILABLE);
        }
        Transaction tx = new Transaction(config.getCrossCtxType());
        try {
            tx.setRemark(StringUtils.bytes(crossTxTransferDTO.getRemark()));
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            List<CoinFrom> coinFromList = coinDataManager.assemblyCoinFrom(chain, crossTxTransferDTO.getListFrom(), false);
            List<CoinTo> coinToList = coinDataManager.assemblyCoinTo(crossTxTransferDTO.getListTo(), chain);
            coinDataManager.verifyCoin(coinFromList, coinToList, chain);
            int txSize = tx.size();
            //如果为主链跨链交易中只存在原始跨链交易签名，如果不为主链，跨链交易签名列表中会包含主网协议跨链交易的签名列表
            if (config.isMainNet()) {
                txSize += coinDataManager.getSignatureSize(coinFromList);
            } else {
                txSize += coinDataManager.getSignatureSize(coinFromList) * 2;
            }
            //如果当前节点为共识节点且转出账户不为该共识账户则共识账户需对跨链交易签名
            Map packerInfo = ConsensusCall.getPackerInfo(chain);
            String password = (String) packerInfo.get("password");
            String address = (String) packerInfo.get("address");
            List<String> packers = (List<String>) packerInfo.get("packAddressList");
            int verifierSignCount = CommonUtil.getByzantineCount(packers, chain) - 1;
            boolean isPacker = false;
            if (!StringUtils.isBlank(address) && !crossTxTransferDTO.getFromAddressList().contains(address)) {
                isPacker = true;
                verifierSignCount++;
            }
            txSize += verifierSignCount * P2PHKSignature.SERIALIZE_LENGTH;

            CoinData coinData = coinDataManager.getCoinData(chain, coinFromList, coinToList, txSize, true);
            //如果不是主网需计算主网协议跨链交易手续费
            if (!config.isMainNet()) {
                coinData = coinDataManager.getCoinData(chain, coinData.getFrom(), coinData.getTo(), txSize, false);
            }
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            //签名
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            List<String> signedAddressList = new ArrayList<>();
            Map<String, String> signedAddressMap = new HashMap<>(INIT_CAPACITY_8);
            for (CoinDTO coinDTO : crossTxTransferDTO.getListFrom()) {
                if (!signedAddressList.contains(coinDTO.getAddress())) {
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(coinDTO.getAddress(), coinDTO.getPassword(), tx.getHash().getBytes());
                    p2PHKSignatures.add(p2PHKSignature);
                    signedAddressList.add(coinDTO.getAddress());
                    signedAddressMap.put(coinDTO.getAddress(), coinDTO.getPassword());
                }
            }
            if (!txValidator.coinDataValid(chain, coinData, tx.size())) {
                chain.getLogger().error("跨链交易CoinData验证失败！\n\n");
                return Result.getFailed(COINDATA_VERIFY_FAIL);
            }
            NulsHash txHash = tx.getHash();
            BroadCtxSignMessage message = new BroadCtxSignMessage();

            //判断本链是友链还是主网，如果是友链则需要生成对应的主网协议跨链交易，如果为主网则直接将跨链交易发送给交易模块处理
            if (!config.isMainNet()) {
                Transaction mainCtx = TxUtil.friendConvertToMain(chain, tx, signedAddressMap, TxType.CROSS_CHAIN);
                NulsHash convertHash = mainCtx.getHash();
                TransactionSignature mTransactionSignature = new TransactionSignature();
                mTransactionSignature.parse(mainCtx.getTransactionSignature(), 0);
                if (isPacker) {
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, convertHash.getBytes());
                    p2PHKSignatures.add(p2PHKSignature);
                }
                p2PHKSignatures.addAll(mTransactionSignature.getP2PHKSignatures());
                if (!txValidator.coinDataValid(chain, mainCtx.getCoinDataInstance(), mainCtx.size(), false)) {
                    chain.getLogger().error("生成的主网协议跨链交易CoinData验证失败！\n\n");
                    return Result.getFailed(COINDATA_VERIFY_FAIL);
                }
                message.setSignature(mTransactionSignature.getP2PHKSignatures().get(0).serialize());
                convertCtxService.save(txHash, mainCtx, chainId);
                convertHashService.save(convertHash, txHash, chainId);
            } else {
                if (isPacker) {
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, txHash.getBytes());
                    p2PHKSignatures.add(p2PHKSignature);
                }
            }
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            //如果本链为主网，则创建的交易就是主网协议交易
            if (config.isMainNet()) {
                message.setSignature(p2PHKSignatures.get(0).serialize());
            }
            message.setLocalHash(txHash);
            CtxStatusPO ctxStatusPO = new CtxStatusPO(tx, TxStatusEnum.UNCONFIRM.getStatus());
            ctxStatusService.save(txHash, ctxStatusPO, chainId);
            MessageUtil.signByzantineInChain(chain, tx, transactionSignature, packers);
            NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
            Map<String, Object> result = new HashMap<>(2);
            result.put(TX_HASH, tx.getHash().toHex());
            return Result.getSuccess(SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(SERIALIZE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result newApiModuleCrossTx(Map<String, Object> params) {
        if (params.get(CHAIN_ID) == null || params.get(TX) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        String txStr = (String) params.get(TX);
        try {
            Transaction tx = new Transaction();
            tx.parse(RPCUtil.decode(txStr), 0);
            CoinData coinData = tx.getCoinDataInstance();
            if (!txValidator.coinDataValid(chain, coinData, tx.size())) {
                chain.getLogger().error("跨链交易CoinData验证失败！\n\n");
                return Result.getFailed(COINDATA_VERIFY_FAIL);
            }
            TransactionSignature signature = new TransactionSignature();
            signature.parse(tx.getTransactionSignature(), 0);
            NulsHash txHash = tx.getHash();
            BroadCtxSignMessage message = new BroadCtxSignMessage();
            //如果当前节点为共识节点且转出账户不为该共识账户则共识账户需对跨链交易签名
            Map packerInfo = ConsensusCall.getPackerInfo(chain);
            String password = (String) packerInfo.get("password");
            String address = (String) packerInfo.get("address");
            List<String> packers = (List<String>) packerInfo.get("packAddressList");
            boolean isPacker = false;
            if (!StringUtils.isBlank(address) && !coinData.getFromAddressList().contains(address)) {
                isPacker = true;
            }
            //判断本链是友链还是主网，如果是友链则需要生成对应的主网协议跨链交易，如果为主网则直接将跨链交易发送给交易模块处理
            if (!config.isMainNet()) {
                Transaction mainCtx = TxUtil.friendConvertToMain(chain, tx, null, TxType.CROSS_CHAIN);
                NulsHash convertHash = mainCtx.getHash();
                if (isPacker) {
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, convertHash.getBytes());
                    signature.getP2PHKSignatures().add(p2PHKSignature);
                }
                if (!txValidator.coinDataValid(chain, mainCtx.getCoinDataInstance(), mainCtx.size(), false)) {
                    chain.getLogger().error("生成的主网协议跨链交易CoinData验证失败！\n\n");
                    return Result.getFailed(COINDATA_VERIFY_FAIL);
                }
                TransactionSignature mTransactionSignature = new TransactionSignature();
                mTransactionSignature.parse(mainCtx.getTransactionSignature(), 0);
                message.setSignature(mTransactionSignature.getP2PHKSignatures().get(0).serialize());
                convertCtxService.save(txHash, mainCtx, chainId);
                convertHashService.save(convertHash, txHash, chainId);
            } else {
                if (isPacker) {
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, txHash.getBytes());
                    signature.getP2PHKSignatures().add(p2PHKSignature);
                }
            }
            tx.setTransactionSignature(signature.serialize());
            //如果本链为主网，则创建的交易就是主网协议交易
            if (config.isMainNet()) {
                message.setSignature(signature.getP2PHKSignatures().get(0).serialize());
            }
            message.setLocalHash(txHash);
            CtxStatusPO ctxStatusPO = new CtxStatusPO(tx, TxStatusEnum.UNCONFIRM.getStatus());
            ctxStatusService.save(txHash, ctxStatusPO, chainId);
            MessageUtil.signByzantineInChain(chain, tx, signature, packers);
            NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
            Map<String, Object> result = new HashMap<>(2);
            result.put(TX_HASH, tx.getHash().toHex());
            result.put("success", true);
            return Result.getSuccess(SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(SERIALIZE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result validCrossTx(Map<String, Object> params) {
        if (params.get(CHAIN_ID) == null || params.get(TX) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        String txStr = (String) params.get(TX);
        try {
            Transaction transaction = new Transaction();
            transaction.parse(RPCUtil.decode(txStr), 0);
            if (!txValidator.validateTx(chain, transaction, null)) {
                chain.getLogger().error("跨链交易验证失败,Hash:{}\n", transaction.getHash().toHex());
                return Result.getFailed(TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> validResult = new HashMap<>(2);
            validResult.put(VALUE, true);
            chain.getLogger().info("跨链交易验证成功，Hash:{}\n", transaction.getHash().toHex());
            return Result.getSuccess(SUCCESS).setData(validResult);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(SERIALIZE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean commitCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        try {
            List<NulsHash> convertHashList = new ArrayList<>();
            List<NulsHash> ctxStatusList = new ArrayList<>();
            List<NulsHash> otherCtxList = new ArrayList<>();
            List<NulsHash> waitSendList = new ArrayList<>();
            for (Transaction ctx : txs) {
                NulsHash ctxHash = ctx.getHash();
                verifiedCtxMap.get(chainId).remove(ctxHash);
                CoinData coinData = ctx.getCoinDataInstance();
                int fromChainId = AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                if (chainId == fromChainId) {
                    CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.COMMITTED.getStatus());
                    if (!config.isMainNet()) {
                        NulsHash convertHash = TxUtil.friendConvertToMain(chain, ctx, null, TxType.CROSS_CHAIN).getHash();
                        if (!convertHashService.save(convertHash, ctxHash, chainId)) {
                            rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                            return false;
                        }
                        convertHashList.add(convertHash);
                    }
                    if (!ctxStatusService.save(ctxHash, ctxStatusPO, chainId)) {
                        rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                        return false;
                    }
                    ctxStatusList.add(ctxHash);
                    waitSendList.add(ctxHash);
                } else if (chainId == toChainId) {
                    NulsHash convertHash = ctxHash;
                    if (!config.isMainNet()) {
                        convertHash = TxUtil.friendConvertToMain(chain, ctx, null, TxType.CROSS_CHAIN).getHash();
                    }
                    if (!convertHashService.save(convertHash, ctxHash, chainId)) {
                        rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                        return false;
                    }
                    convertHashList.add(convertHash);
                    if (!otherCtxService.save(convertHash, ctx, chainId)) {
                        rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                        return false;
                    }
                    otherCtxList.add(convertHash);
                } else {
                    if (!otherCtxService.save(ctxHash, ctx, chainId)) {
                        rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                        return false;
                    }
                    otherCtxList.add(ctxHash);
                    if (!convertHashService.save(ctxHash, ctxHash, chainId)) {
                        rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                        return false;
                    }
                    convertHashList.add(ctxHash);
                }
            }
            if (!waitSendList.isEmpty()) {
                //跨链交易被打包的高度
                long sendHeight = blockHeader.getHeight() + chain.getConfig().getSendHeight();
                SendCtxHashPO sendCtxHashPo = new SendCtxHashPO(waitSendList);
                if (!sendHeightService.save(sendHeight, sendCtxHashPo, chainId)) {
                    rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                    return false;
                }
            }
            //如果本链为主网通知跨链管理模块发起链与接收链资产变更
            if (config.isMainNet()) {
                List<String> txStrList = new ArrayList<>();
                for (Transaction tx : txs) {
                    txStrList.add(RPCUtil.encode(tx.serialize()));
                }
                String headerStr = RPCUtil.encode(blockHeader.serialize());
                ChainManagerCall.ctxAssetCirculateCommit(chainId, txStrList, headerStr);
            }
            chain.getLogger().info("高度：{} 的跨链交易提交完成\n", blockHeader.getHeight());
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean rollbackCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        try {
            for (Transaction ctx : txs) {
                CoinData coinData = ctx.getCoinDataInstance();
                int fromChainId = AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                NulsHash ctxHash = ctx.getHash();
                if (chainId == fromChainId) {
                    CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
                    if (!ctxStatusService.save(ctxHash, ctxStatusPO, chainId)) {
                        return false;
                    }
                } else if (chainId == toChainId) {
                    NulsHash convertHash = ctxHash;
                    if (!config.isMainNet() && ctx.getType() == config.getCrossCtxType()) {
                        convertHash = TxUtil.friendConvertToMain(chain, ctx, null, TxType.CROSS_CHAIN).getHash();
                    }
                    if (!convertHashService.delete(convertHash, chainId)) {
                        return false;
                    }
                } else {
                    if (!ctxStatusService.delete(ctxHash, chainId) || !convertHashService.delete(ctxHash, chainId)) {
                        return false;
                    }
                }
            }
            //需要被清理的跨链交易高度
            long sendHeight = blockHeader.getHeight() + chain.getConfig().getSendHeight();
            if (!sendHeightService.delete(sendHeight, chainId)) {
                chain.getLogger().error("已广播跨链高度清除失败！\n\n");
                return false;
            }
            //如果为主网通知跨链管理模块发起链与接收链资产变更
            if (config.isMainNet()) {
                List<String> txStrList = new ArrayList<>();
                for (Transaction tx : txs) {
                    txStrList.add(RPCUtil.encode(tx.serialize()));
                }
                String headerStr = RPCUtil.encode(blockHeader.serialize());
                ChainManagerCall.ctxAssetCirculateRollback(chainId, txStrList, headerStr);
            }
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> crossTxBatchValid(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if (chain == null) {
            result.put("txList", txs);
            result.put("errorCode", NulsCrossChainErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        if (!verifiedCtxMap.keySet().contains(chainId)) {
            verifiedCtxMap.put(chainId, new HashSet<>());
        }
        Set<NulsHash> verifiedCtxSet = verifiedCtxMap.get(chainId);
        List<Transaction> invalidCtxList = new ArrayList<>();
        String errorCode = null;
        for (Transaction ctx : txs) {
            NulsHash ctxHash = ctx.getHash();
            try {
                if (verifiedCtxSet.contains(ctxHash)) {
                    if (!txValidator.packageValid(chain, ctx, blockHeader)) {
                        verifiedCtxSet.remove(ctxHash);
                        invalidCtxList.add(ctx);
                    }
                } else {
                    if (!txValidator.validateTx(chain, ctx, blockHeader)) {
                        invalidCtxList.add(ctx);
                    } else {
                        verifiedCtxSet.add(ctxHash);
                    }
                }
            } catch (NulsException e) {
                invalidCtxList.add(ctx);
                chain.getLogger().error("Cross-Chain Transaction Verification Failure");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            } catch (IOException io) {
                invalidCtxList.add(ctx);
                chain.getLogger().error("Cross-Chain Transaction Verification Failure");
                chain.getLogger().error(io);
                errorCode = NulsCrossChainErrorCode.SERIALIZE_ERROR.getCode();
            }
        }
        result.put("txList", invalidCtxList);
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getCrossTxState(Map<String, Object> params) {
        if (params.get(CHAIN_ID) == null || params.get(TX_HASH) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        String hashStr = (String) params.get(TX_HASH);
        Map<String, Object> result = new HashMap<>(2);
        NulsHash requestHash = NulsHash.fromHex(hashStr);
        byte statisticsResult = TxUtil.getCtxState(chain, requestHash);
        result.put(VALUE, statisticsResult);
        return Result.getSuccess(SUCCESS).setData(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getRegisteredChainInfoList(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>(2);
        result.put(LIST, chainManager.getRegisteredCrossChainList());
        return Result.getSuccess(SUCCESS).setData(result);
    }

    @Override
    public int getCrossChainTxType() {
        return config.getCrossCtxType();
    }

    private void rollbackCtx(List<NulsHash> convertHashList, List<NulsHash> ctxStatusList, List<NulsHash> otherCtxList, int chainId) {
        for (NulsHash convertHash : convertHashList) {
            convertHashService.delete(convertHash, chainId);
        }
        for (NulsHash otherHash : otherCtxList) {
            otherCtxService.delete(otherHash, chainId);
        }
        for (NulsHash ctxStatusHash : ctxStatusList) {
            CtxStatusPO ctxStatusPO = ctxStatusService.get(ctxStatusHash, chainId);
            ctxStatusPO.setStatus(TxStatusEnum.UNCONFIRM.getStatus());
            ctxStatusService.save(ctxStatusHash, ctxStatusPO, chainId);
        }
    }
}
