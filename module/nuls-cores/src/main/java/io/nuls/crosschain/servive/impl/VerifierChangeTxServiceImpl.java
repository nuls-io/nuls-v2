package io.nuls.crosschain.servive.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.base.service.VerifierChangeTxService;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.rpc.call.BlockCall;
import io.nuls.crosschain.srorage.ConvertHashService;
import io.nuls.crosschain.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.utils.CommonUtil;
import io.nuls.crosschain.utils.TxUtil;
import io.nuls.crosschain.utils.manager.ChainManager;
import io.nuls.crosschain.utils.manager.LocalVerifierManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verifier change transaction implementation class
 *
 * @author tag
 * @date 2019/6/19
 */
@Component
public class VerifierChangeTxServiceImpl implements VerifierChangeTxService {
    @Autowired
    private NulsCoresConfig config;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ConvertHashService convertHashService;
    @Autowired
    private RegisteredCrossChainService registeredCrossChainService;

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        List<Transaction> invalidTxList = new ArrayList<>();
        Chain chain = chainManager.getChainMap().get(chainId);
        List<String> verifierList;
        int minPassCount;
        Map<String, Object> result = new HashMap<>(2);
        String errorCode = null;
        for (Transaction verifierChangeTx : txs) {
            try {
                ChainInfo chainInfo;
                VerifierChangeData verifierChangeData = new VerifierChangeData();
                verifierChangeData.parse(verifierChangeTx.getTxData(), 0);
                List<String> registerList = verifierChangeData.getRegisterAgentList();
                List<String> cancelList = verifierChangeData.getCancelAgentList();
                int verifierChainId = verifierChangeData.getChainId();
                boolean haveCancelVerifier = cancelList != null && !cancelList.isEmpty();
                boolean dataValid = haveCancelVerifier || (registerList != null && !registerList.isEmpty());
                if (!dataValid || verifierChainId <= 0) {
                    chain.getLogger().error("Verifier change information is invalid,chainId:{}", verifierChainId);
                }
                //If the verifier of this chain changes, verify Byzantium
                if(verifierChainId == chainId){
                    verifierList = new ArrayList<>(chain.getVerifierList());
                }else{
                    chainInfo = chainManager.getChainInfo(verifierChainId);
                    if (chainInfo == null) {
                        chain.getLogger().error("Chain not registered,chainId:{}", verifierChainId);
                        throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED);
                    }
                    verifierList = new ArrayList<>(chainInfo.getVerifierList());
                }
                if(haveCancelVerifier){
                    //If the exiting verifier is greater than30%Invalid
                    int maxCancelCount = verifierList.size() * NulsCrossChainConstant.VERIFIER_CANCEL_MAX_RATE / NulsCrossChainConstant.MAGIC_NUM_100;
                    if (cancelList.size() > maxCancelCount) {
                        chain.getLogger().error("Abnormal change of transaction data of verifier: the verifier who exits is more than 30%,cancelCount:{},maxCancelCount:{},totalCount:{}", cancelList.size(), maxCancelCount, verifierList.size());
                        throw new NulsException(NulsCrossChainErrorCode.TO_MANY_VERIFIER_EXIT);
                    }
                    verifierList.removeAll(cancelList);
                }

                minPassCount = CommonUtil.getByzantineCount(chain, verifierList.size());

                if (verifierList.isEmpty()) {
                    chain.getLogger().error("The chain has not registered a verifier yet,chainId:{}", verifierChainId);
                    throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED_VERIFIER);
                }
                if (!SignatureUtil.validateCtxSignture(verifierChangeTx)) {
                    chain.getLogger().error("Main network protocol cross chain transaction signature verification failed！");
                    throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
                }
                if (!TxUtil.signByzantineVerify(chain, verifierChangeTx, verifierList, minPassCount,verifierChainId)) {
                    chain.getLogger().error("Signature Byzantine verification failed！");
                    throw new NulsException(NulsCrossChainErrorCode.CTX_SIGN_BYZANTINE_FAIL);
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
                invalidTxList.add(verifierChangeTx);
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
        int syncStatus = BlockCall.getBlockStatus(chain);
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
                //If there is a change in the verifier for this chain, save and update the local verifier
                if(verifierChainId == chainId){
                    if(chain.getVerifierChangeTx() != null && !ctxHash.equals(chain.getVerifierChangeTx().getHash())){
                        chain.getLogger().warn("Local processing verifier change transaction changed,commitHash:{},cacheHash:{}",ctxHash.toHex(),chain.getVerifierChangeTx().getHash().toHex());
                        continue;
                    }
                    VerifierChangeData txData = new VerifierChangeData();
                    try {
                        txData.parse(verifierChangeTx.getTxData(), 0);
                    }catch (NulsException e){
                        chain.getLogger().error(e);
                        continue;
                    }
                    if(!LocalVerifierManager.localVerifierChangeCommit(chain,verifierChangeTx, txData.getCancelAgentList(), txData.getRegisterAgentList(),blockHeader.getHeight(),ctxHash,syncStatus)) {
                        chain.getLogger().error("Verifier change failed");
                        continue;
                    }
                    commitSuccessList.add(verifierChangeTx);
                }else{
                    ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
                    chain.getLogger().info("chain{}The current list of validators is：{}",verifierChainId,chainInfo.getVerifierList().toString() );
                    if(registerList != null && !registerList.isEmpty()){
                        chainInfo.getVerifierList().addAll(registerList);
                        chain.getLogger().info("Add validation list as：{}" ,registerList.toString());
                    }
                    if(cancelList != null && !cancelList.isEmpty()){
                        chainInfo.getVerifierList().removeAll(cancelList);
                        chain.getLogger().info("The list of verifiers to be logged out is：{}",cancelList.toString() );
                    }
                    chain.getLogger().info("chain{}The updated validation list is{}",verifierChainId,chainInfo.getVerifierList().toString() );
                    RegisteredChainMessage registeredChainMessage = new RegisteredChainMessage();
                    registeredChainMessage.setChainInfoList(chainManager.getRegisteredCrossChainList());
                    if(!registeredCrossChainService.save(registeredChainMessage)){
                        rollback(chainId, commitSuccessList, blockHeader);
                        return false;
                    }
                    commitSuccessList.add(verifierChangeTx);
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
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
                if(verifierChainId == chainId){
                    LocalVerifierManager.localVerifierChangeRollback(chain, verifierChangeData.getCancelAgentList(), verifierChangeData.getRegisterAgentList(), blockHeader.getHeight(), ctxHash);
                }else{
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
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                return false;
            }
        }
        return true;
    }
}
