package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.base.service.VerifierChangeTxService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.BlockCall;
import io.nuls.crosschain.nuls.srorage.ConfigService;
import io.nuls.crosschain.nuls.srorage.ConvertHashService;
import io.nuls.crosschain.nuls.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.nuls.utils.CommonUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import io.nuls.crosschain.nuls.utils.manager.LocalVerifierManager;

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
                    chain.getLogger().error("验证人变更信息无效,chainId:{}", verifierChainId);
                }
                //如果为本链验证人变更，验证拜占庭
                if(verifierChainId == chainId){
                    verifierList = new ArrayList<>(chain.getVerifierList());
                }else{
                    chainInfo = chainManager.getChainInfo(verifierChainId);
                    if (chainInfo == null) {
                        chain.getLogger().error("链未注册,chainId:{}", verifierChainId);
                        throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED);
                    }
                    verifierList = new ArrayList<>(chainInfo.getVerifierList());
                }
                if(haveCancelVerifier){
                    //如果退出的验证人大于30%则无效
                    int maxCancelCount = verifierList.size() * NulsCrossChainConstant.VERIFIER_CANCEL_MAX_RATE / NulsCrossChainConstant.MAGIC_NUM_100;
                    if (cancelList.size() > maxCancelCount) {
                        chain.getLogger().error("Abnormal change of transaction data of verifier: the verifier who exits is more than 30%,cancelCount:{},maxCancelCount:{},totalCount:{}", cancelList.size(), maxCancelCount, verifierList.size());
                        throw new NulsException(NulsCrossChainErrorCode.TO_MANY_VERIFIER_EXIT);
                    }
                    verifierList.removeAll(cancelList);
                }

                minPassCount = CommonUtil.getByzantineCount(chain, verifierList.size());

                if (verifierList.isEmpty()) {
                    chain.getLogger().error("链还未注册验证人,chainId:{}", verifierChainId);
                    throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED_VERIFIER);
                }
                if (!SignatureUtil.validateCtxSignture(verifierChangeTx)) {
                    chain.getLogger().info("主网协议跨链交易签名验证失败！");
                    throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
                }
                if (!TxUtil.signByzantineVerify(chain, verifierChangeTx, verifierList, minPassCount,verifierChainId)) {
                    chain.getLogger().info("签名拜占庭验证失败！");
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
                //如果为本链验证人变更，则保存更新本地验证人
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
                    chain.getLogger().info("链{}当前验证人列表为：{}",verifierChainId,chainInfo.getVerifierList().toString() );
                    if(registerList != null && !registerList.isEmpty()){
                        chainInfo.getVerifierList().addAll(registerList);
                        chain.getLogger().info("新增验证列表为：{}" ,registerList.toString());
                    }
                    if(cancelList != null && !cancelList.isEmpty()){
                        chainInfo.getVerifierList().removeAll(cancelList);
                        chain.getLogger().info("注销的验证人列表为：{}",cancelList.toString() );
                    }
                    chain.getLogger().info("链{}更新后的验证列表为{}",verifierChainId,chainInfo.getVerifierList().toString() );
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
