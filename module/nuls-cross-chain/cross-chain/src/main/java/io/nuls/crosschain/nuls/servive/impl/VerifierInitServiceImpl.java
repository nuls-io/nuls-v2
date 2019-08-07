package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.constant.CrossChainConstant;
import io.nuls.crosschain.base.message.RegisteredChainMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.VerifierInitData;
import io.nuls.crosschain.base.service.VerifierInitService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.srorage.ConfigService;
import io.nuls.crosschain.nuls.srorage.ConvertHashService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.io.IOException;
import java.util.*;

/**
 * 验证人初始化交易实现类
 *
 * @author tag
 * @date 2019/8/7
 */
@Component
public class VerifierInitServiceImpl implements VerifierInitService {
    @Autowired
    private NulsCrossChainConfig config;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ConvertHashService convertHashService;
    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;
    @Autowired
    private ConfigService configService;

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        List<Transaction> invalidTxList = new ArrayList<>();
        Chain chain = chainManager.getChainMap().get(chainId);
        List<String> verifierList;
        int minPassCount;
        Map<String, Object> result = new HashMap<>(2);
        String errorCode = null;
        for (Transaction verifierInitTx : txs) {
            try {
                ChainInfo chainInfo;
                VerifierInitData verifierInitData = new VerifierInitData();
                verifierInitData.parse(verifierInitTx.getTxData(),0);
                List<String> initVerifierList = verifierInitData.getVerifierList();
                int verifierChainId = verifierInitData.getRegisterChainId();
                if (initVerifierList == null || initVerifierList.isEmpty() || verifierChainId <= 0) {
                    chain.getLogger().error("验证人变更信息无效,chainId:{}", verifierChainId);
                }

                if(!config.isMainNet()){
                    verifierList = new ArrayList<>(Arrays.asList(config.getVerifiers().split(NulsCrossChainConstant.VERIFIER_SPLIT)));
                    minPassCount = verifierList.size() * config.getMainByzantineRatio()/ CrossChainConstant.MAGIC_NUM_100;
                    if(minPassCount == 0){
                        minPassCount = 1;
                    }
                }else{
                    chainInfo = chainManager.getChainInfo(verifierChainId);
                    if (chainInfo == null) {
                        chain.getLogger().error("链未注册,chainId:{}", verifierChainId);
                        throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED);
                    }
                    verifierList = new ArrayList<>(chainInfo.getVerifierList());
                    minPassCount = chainInfo.getMinPassCount();
                }
                if (verifierList.isEmpty()) {
                    chain.getLogger().error("链还未注册验证人,chainId:{}", verifierChainId);
                    throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED_VERIFIER);
                }
                if (!SignatureUtil.validateCtxSignture(verifierInitTx)) {
                    chain.getLogger().info("主网协议跨链交易签名验证失败！");
                    throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
                }
                if (!TxUtil.signByzantineVerify(chain, verifierInitTx, verifierList, minPassCount, verifierChainId)) {
                    chain.getLogger().info("签名拜占庭验证失败！");
                    throw new NulsException(NulsCrossChainErrorCode.CTX_SIGN_BYZANTINE_FAIL);
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
                invalidTxList.add(verifierInitTx);
            }

        }
        result.put("txList", invalidTxList);
        result.put("errorCode", errorCode);
        return result;
    }


    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        for (Transaction verifierInitTx : txs) {
            try {
                NulsHash ctxHash = verifierInitTx.getHash();
                if (!convertHashService.save(ctxHash, ctxHash, chainId)) {
                    rollback(chainId, commitSuccessList, blockHeader);
                    return false;
                }
                VerifierInitData verifierInitData = new VerifierInitData();
                verifierInitData.parse(verifierInitTx.getTxData(),0);
                List<String> initVerifierList = verifierInitData.getVerifierList();
                int verifierChainId = verifierInitData.getRegisterChainId();
                ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
                chainInfo.getVerifierList().addAll(initVerifierList);
                chain.getLogger().info("链{}初始化后的验证列表为{}", verifierChainId, chainInfo.getVerifierList().toString());
                RegisteredChainMessage registeredChainMessage = new RegisteredChainMessage();
                registeredChainMessage.setChainInfoList(chainManager.getRegisteredCrossChainList());
                if (!registeredCrossChainService.save(registeredChainMessage)) {
                    rollback(chainId, commitSuccessList, blockHeader);
                    return false;
                }
                commitSuccessList.add(verifierInitTx);
                //todo 需判断是同步中还是下载中
                if(!config.isMainNet()){
                    Transaction tx = TxUtil.createVerifierInitTx(ConsensusCall.getWorkAgentList(chain), blockHeader.getTime(), chainId);
                    TxUtil.handleNewCtx(tx, chain, null);
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                rollback(chainId, commitSuccessList, blockHeader);
                return false;
            }catch (IOException io){
                chain.getLogger().error(io);
                rollback(chainId, commitSuccessList, blockHeader);
                return false;
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
        for (Transaction verifierInitTx : txs) {
            try {
                NulsHash ctxHash = verifierInitTx.getHash();
                if (!convertHashService.delete(ctxHash, chainId)) {
                    return false;
                }
                VerifierInitData verifierInitData = new VerifierInitData();
                verifierInitData.parse(verifierInitTx.getTxData(),0);
                List<String> initVerifierList = verifierInitData.getVerifierList();
                int verifierChainId = verifierInitData.getRegisterChainId();
                if (!convertHashService.delete(ctxHash, chainId)) {
                    return false;
                }
                ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
                chainInfo.getVerifierList().removeAll(initVerifierList);
                RegisteredChainMessage registeredChainMessage = new RegisteredChainMessage();
                registeredChainMessage.setChainInfoList(chainManager.getRegisteredCrossChainList());
                if (!registeredCrossChainService.save(registeredChainMessage)) {
                    return false;
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                return false;
            }
        }
        return true;
    }
}
