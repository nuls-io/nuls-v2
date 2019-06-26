package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.base.service.VerifierChangeTxService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.srorage.ConvertHashService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.util.*;

/**
 * 验证人变更交易实现类
 *
 * @author tag
 * @date 2019/6/19
 */
@Component
public class VerifierChangeTxServiceImpl implements VerifierChangeTxService {
    @Autowired
    private NulsCrossChainConfig config;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ConvertHashService convertHashService;
    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        List<Transaction> invalidTxList = new ArrayList<>();
        Chain chain = chainManager.getChainMap().get(chainId);
        List<String> verifierList;
        int minPassCount;
        for (Transaction verifierChangeTx : txs) {
            try {
                ChainInfo chainInfo;
                VerifierChangeData verifierChangeData = new VerifierChangeData();
                verifierChangeData.parse(verifierChangeTx.getTxData(), 0);
                List<String> registerList = verifierChangeData.getRegisterAgentList();
                List<String> cancelList = verifierChangeData.getCancelAgentList();
                int verifierChainId = verifierChangeData.getChainId();
                boolean isValid = (registerList == null || registerList.isEmpty()) && (cancelList == null || cancelList.isEmpty());
                if (isValid || verifierChainId <= 0) {
                    chain.getLogger().error("验证人变更信息无效,chainId:{}", verifierChainId);
                }
                chainInfo = chainManager.getChainInfo(verifierChainId);
                if (chainInfo == null) {
                    chain.getLogger().error("链未注册,chainId:{}", verifierChainId);
                    throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED);
                }
                verifierList = new ArrayList<>(chainInfo.getVerifierList());
                if (verifierList.isEmpty()) {
                    chain.getLogger().error("链还未注册验证人,chainId:{}", verifierChainId);
                    throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED_VERIFIER);
                }
                minPassCount = chainInfo.getMaxSignatureCount() * chainInfo.getSignatureByzantineRatio() / NulsCrossChainConstant.MAGIC_NUM_100;
                if (!SignatureUtil.validateCtxSignture(verifierChangeTx)) {
                    chain.getLogger().info("主网协议跨链交易签名验证失败！");
                    throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
                }
                if (!signByzantineVerify(chain, verifierChangeTx, verifierList, minPassCount)) {
                    chain.getLogger().info("签名拜占庭验证失败！");
                    throw new NulsException(NulsCrossChainErrorCode.CTX_SIGN_BYZANTINE_FAIL);
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                invalidTxList.add(verifierChangeTx);
            }

        }
        return invalidTxList;
    }


    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        for (Transaction verifierChangeTx : txs) {
            try {
                NulsHash ctxHash = verifierChangeTx.getHash();
                if (!convertHashService.save(ctxHash, ctxHash, chainId)) {
                    rollback(chainId, commitSuccessList, blockHeader);
                    return false;
                }
                VerifierChangeData verifierChangeData = new VerifierChangeData();
                verifierChangeData.parse(verifierChangeTx.getTxData(), 0);
                List<String> registerList = verifierChangeData.getRegisterAgentList();
                List<String> cancelList = verifierChangeData.getCancelAgentList();
                int verifierChainId = verifierChangeData.getChainId();
                ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
                if(registerList != null && !registerList.isEmpty()){
                    chainInfo.getVerifierList().addAll(registerList);
                }
                if(cancelList != null && !cancelList.isEmpty()){
                    chainInfo.getVerifierList().removeAll(cancelList);
                }
                RegisteredChainMessage registeredChainMessage = new RegisteredChainMessage();
                registeredChainMessage.setChainInfoList(chainManager.getRegisteredCrossChainList());
                if(!registeredCrossChainService.save(registeredChainMessage)){
                    rollback(chainId, commitSuccessList, blockHeader);
                    return false;
                }
                commitSuccessList.add(verifierChangeTx);
            } catch (NulsException e) {
                chain.getLogger().error(e);
                rollback(chainId, commitSuccessList, blockHeader);
            }
        }
        return true;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        for (Transaction verifierChangeTx : txs) {
            try {
                NulsHash ctxHash = verifierChangeTx.getHash();
                if (!convertHashService.delete(ctxHash, chainId)) {
                    return false;
                }
                VerifierChangeData verifierChangeData = new VerifierChangeData();
                verifierChangeData.parse(verifierChangeTx.getTxData(), 0);
                List<String> registerList = verifierChangeData.getRegisterAgentList();
                List<String> cancelList = verifierChangeData.getCancelAgentList();
                int verifierChainId = verifierChangeData.getChainId();
                if (!convertHashService.delete(ctxHash, chainId)) {
                    return false;
                }
                ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
                if(registerList != null && !registerList.isEmpty()){
                    chainInfo.getVerifierList().removeAll(registerList);
                }
                if(cancelList != null && !cancelList.isEmpty()){
                    chainInfo.getVerifierList().addAll(cancelList);
                }
                RegisteredChainMessage registeredChainMessage = new RegisteredChainMessage();
                registeredChainMessage.setChainInfoList(chainManager.getRegisteredCrossChainList());
                if(!registeredCrossChainService.save(registeredChainMessage)){
                    return false;
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                return false;
            }
        }
        return true;
    }

    /**
     * 跨链交易签名拜占庭验证
     * Byzantine Verification of Cross-Chain Transaction Signature
     */
    private boolean signByzantineVerify(Chain chain, Transaction ctx, List<String> verifierList, int byzantineCount) throws NulsException {
        TransactionSignature transactionSignature = new TransactionSignature();
        try {
            transactionSignature.parse(ctx.getTransactionSignature(), 0);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            throw e;
        }
        if (transactionSignature.getP2PHKSignatures().size() < byzantineCount) {
            chain.getLogger().error("跨链交易签名数量小于拜占庭数量，Hash:{},signCount:{},byzantineCount:{}", ctx.getHash().toHex(), transactionSignature.getP2PHKSignatures().size(), byzantineCount);
            return false;
        }
        Iterator<P2PHKSignature> iterator = transactionSignature.getP2PHKSignatures().iterator();
        while (iterator.hasNext()) {
            P2PHKSignature signature = iterator.next();
            boolean isMatchSign = false;
            for (String verifier : verifierList) {
                if (Arrays.equals(AddressTool.getAddress(signature.getPublicKey(), chain.getChainId()), AddressTool.getAddress(verifier))) {
                    isMatchSign = true;
                    break;
                }
            }
            if (!isMatchSign) {
                chain.getLogger().error("跨链交易签名验证失败，Hash:{},sign{}", ctx.getHash().toHex(), signature.getSignerHash160());
                return false;
            }
        }
        return true;
    }
}
