package io.nuls.poc.tx.v4;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.utils.LoggerUtil;
import io.nuls.poc.utils.enumeration.PunishReasonEnum;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.manager.PunishManager;

import java.util.*;

/**
 * 红牌交易处理器
 * @author tag
 * @date 2019/6/1
 */
@Component("RedPunishProcessorV4")
public class RedPunishProcessor implements TransactionProcessor {
    @Autowired
    private PunishManager punishManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AgentManager agentManager;

    @Override
    public int getType() {
        return TxType.RED_PUNISH;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            result.put("txList", txs);
            result.put("errorCode", ConsensusErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        List<Transaction> invalidTxList = new ArrayList<>();
        String errorCode = null;
        Set<String> addressHexSet = new HashSet<>();
        for (Transaction tx:txs) {
            try {
                RedPunishData redPunishData = new RedPunishData();
                redPunishData.parse(tx.getTxData(), 0);
                if(!verifyV4(chain, redPunishData)){
                    invalidTxList.add(tx);
                    errorCode = ConsensusErrorCode.BLOCK_RED_PUNISH_ERROR.getCode();
                    continue;
                }
                String addressHex = HexUtil.encode(redPunishData.getAddress());
                /*
                 * 重复的红牌交易不打包
                 * */
                if (!addressHexSet.add(addressHex)) {
                    invalidTxList.add(tx);
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                }
            }catch (NulsException e){
                invalidTxList.add(tx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            }
        }
        result.put("txList", invalidTxList);
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        boolean commitResult = true;
        for (Transaction tx:txs) {
            try {
                if(punishManager.redPunishCommit(tx,chain,blockHeader)){
                    commitSuccessList.add(tx);
                }else{
                    commitResult = false;
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to red punish transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if(!commitResult){
            for (Transaction rollbackTx:commitSuccessList) {
                try {
                    punishManager.redPunishRollback(rollbackTx, chain,blockHeader);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to red punish transaction rollback");
                    chain.getLogger().error(e);
                }
            }
        }
        return commitResult;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> rollbackSuccessList = new ArrayList<>();
        boolean rollbackResult = true;
        for (Transaction tx:txs) {
            try {
                if(punishManager.redPunishRollback(tx,chain,blockHeader)){
                    rollbackSuccessList.add(tx);
                }else{
                    rollbackResult = false;
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to red punish transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if(!rollbackResult){
            for (Transaction commitTx:rollbackSuccessList) {
                try {
                    punishManager.redPunishCommit(commitTx, chain, blockHeader);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to red punish transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }

    private boolean verifyV4(Chain chain, RedPunishData punishData){
        //验证红牌证据正确性
        if (punishData.getReasonCode() == PunishReasonEnum.BIFURCATION.getCode()) {
            NulsByteBuffer byteBuffer = new NulsByteBuffer(punishData.getEvidence());
            Set<Long> heightSet = new HashSet<>();
            for (int i = 0; i < ConsensusConstant.REDPUNISH_BIFURCATION && !byteBuffer.isFinished(); i++) {
                BlockHeader header1;
                BlockHeader header2;
                try {
                    header1 = byteBuffer.readNulsData(new BlockHeader());
                    header2 = byteBuffer.readNulsData(new BlockHeader());
                } catch (NulsException e) {
                    chain.getLogger().error(e.getMessage());
                    return false;
                }
                //证据是否重复
                if(heightSet.contains(header1.getHeight())){
                    chain.getLogger().error("Bifurcated evidence with duplicate data");
                    return false;
                }
                //验证区块头签名是否正确
                if (header1.getBlockSignature().verifySignature(header1.getHash()).isFailed() || header2.getBlockSignature().verifySignature(header2.getHash()).isFailed()) {
                    chain.getLogger().error("Signature verification failed in branching evidence");
                    return false;
                }
                //区块签名者是否为节点处罚者
                Agent agent = agentManager.getAgentByAgentAddress(chain, punishData.getAddress());
                if(!Arrays.equals(AddressTool.getAddress(header1.getBlockSignature().getPublicKey(), chain.getConfig().getChainId()), agent.getPackingAddress())
                        || !Arrays.equals(AddressTool.getAddress(header2.getBlockSignature().getPublicKey(), chain.getConfig().getChainId()), agent.getPackingAddress())){
                    chain.getLogger().error("Whether there is a block out of the penalty node in the branch evidence block!");
                    return false;
                }
                heightSet.add(header1.getHeight());
            }
        }
        return true;
    }
}
