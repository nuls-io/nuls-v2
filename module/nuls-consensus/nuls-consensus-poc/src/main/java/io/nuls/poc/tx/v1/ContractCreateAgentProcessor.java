package io.nuls.poc.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.model.dto.transaction.TransactionDto;
import io.nuls.poc.utils.LoggerUtil;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.validator.TxValidator;

import java.io.IOException;
import java.util.*;

/**
 * 智能合约创建节点交易处理器
 * @author tag
 * @date 2019/6/1
 */
@Component("ContractCreateAgentProcessorV1")
public class ContractCreateAgentProcessor implements TransactionProcessor {
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;

    @Override
    public int getType() {
        return TxType.CONTRACT_CREATE_AGENT;
    }

    @Override
    public int getPriority() {
        return 9;
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
        Set<String> redPunishAddressSet = new HashSet<>();
        Set<String> createAgentAddressSet = new HashSet<>();
        List<Transaction> redPunishTxList = txMap.get(TxType.RED_PUNISH);
        if(redPunishTxList != null && redPunishTxList.size() >0){
            for (Transaction redPunishTx:redPunishTxList) {
                RedPunishData redPunishData = new RedPunishData();
                try {
                    redPunishData.parse(redPunishTx.getTxData(), 0);
                    String addressHex = HexUtil.encode(redPunishData.getAddress());
                    redPunishAddressSet.add(addressHex);
                }catch (NulsException e){
                    chain.getLogger().error(e);
                }
            }
        }
        for (Transaction contractCreateAgentTx:txs) {
            try {
                if (!txValidator.validateTx(chain, contractCreateAgentTx)) {
                    invalidTxList.add(contractCreateAgentTx);
                    chain.getLogger().info("Failure to create node transaction validation");
                    continue;
                }
                Agent agent = new Agent();
                agent.parse(contractCreateAgentTx.getTxData(), 0);
                String agentAddressHex = HexUtil.encode(agent.getAgentAddress());
                String packAddressHex = HexUtil.encode(agent.getPackingAddress());
                /*
                 * 获得过红牌交易的地址不能创建节点
                 * */
                if (!redPunishAddressSet.isEmpty()) {
                    if (redPunishAddressSet.contains(agentAddressHex) || redPunishAddressSet.contains(packAddressHex)) {
                        invalidTxList.add(contractCreateAgentTx);
                        chain.getLogger().info("Creating Node Trading and Red Card Trading Conflict");
                        errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                        continue;
                    }
                }
                /*
                 * 重复创建节点
                 * */
                if (!createAgentAddressSet.add(agentAddressHex) || !createAgentAddressSet.add(packAddressHex)) {
                    invalidTxList.add(contractCreateAgentTx);
                    chain.getLogger().info("Repeated transactions");
                    errorCode = ConsensusErrorCode.AGENT_EXIST.getCode();
                }
            }catch (NulsException e){
                invalidTxList.add(contractCreateAgentTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            }catch (IOException io){
                invalidTxList.add(contractCreateAgentTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(io);
                errorCode = ConsensusErrorCode.SERIALIZE_ERROR.getCode();
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
                if(agentManager.createAgentCommit(tx,blockHeader,chain)){
                    commitSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to create agent transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if(!commitResult){
            for (Transaction rollbackTx:commitSuccessList) {
                try {
                    agentManager.createAgentRollBack(rollbackTx, chain);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to create agent transaction rollback");
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
                try {
                    chain.getLogger().info("contract create node transaction rollback, hash is {}, tx is {}", tx.getHash().toHex(), JSONUtils.obj2json(new TransactionDto(tx)));
                } catch (Exception e) {
                    chain.getLogger().warn(e.getMessage());
                }
                if(agentManager.createAgentRollBack(tx,chain)){
                    rollbackSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to create node transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if(!rollbackResult){
            for (Transaction commitTx:rollbackSuccessList) {
                try {
                    agentManager.createAgentCommit(commitTx, blockHeader, chain);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to create node transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }
}
