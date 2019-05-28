package io.nuls.poc.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.utils.LoggerUtil;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.validator.TxValidator;

import java.util.*;

@Component("CreateAgentProcessorV1")
public class CreateAgentProcessor implements TransactionProcessor {
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;

    @Override
    public int getType() {
        return TxType.REGISTER_AGENT;
    }

    @Override
    public int getPriority() {
        return 8;
    }

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return null;
        }
        List<Transaction> invalidTxList = new ArrayList<>();
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
        List<Transaction> contractCreateAgentTxList = txMap.get(TxType.CONTRACT_CREATE_AGENT);
        if(contractCreateAgentTxList != null && contractCreateAgentTxList.size() >0){
            for (Transaction contractCreateAgentTx:contractCreateAgentTxList) {
                try {
                    Agent agent = new Agent();
                    agent.parse(contractCreateAgentTx.getTxData(), 0);
                    createAgentAddressSet.add(HexUtil.encode(agent.getAgentAddress()));
                    createAgentAddressSet.add(HexUtil.encode(agent.getPackingAddress()));
                }catch (Exception e){
                    chain.getLogger().error(e);
                }
            }
        }
        for (Transaction createAgentTx:txs) {
            try {
                if (!txValidator.validateTx(chain, createAgentTx)) {
                    invalidTxList.add(createAgentTx);
                    chain.getLogger().info("Failure to create node transaction validation");
                    continue;
                }
                Agent agent = new Agent();
                agent.parse(createAgentTx.getTxData(), 0);
                String agentAddressHex = HexUtil.encode(agent.getAgentAddress());
                String packAddressHex = HexUtil.encode(agent.getPackingAddress());
                /*
                 * 获得过红牌交易的地址不能创建节点
                 * */
                if (!redPunishAddressSet.isEmpty()) {
                    if (redPunishAddressSet.contains(agentAddressHex) || redPunishAddressSet.contains(packAddressHex)) {
                        invalidTxList.add(createAgentTx);
                        chain.getLogger().info("Creating Node Trading and Red Card Trading Conflict");
                        continue;
                    }
                }
                /*
                 * 重复创建节点
                 * */
                if (!createAgentAddressSet.add(agentAddressHex) || !createAgentAddressSet.add(packAddressHex)) {
                    invalidTxList.add(createAgentTx);
                    chain.getLogger().info("Repeated transactions");
                }
            }catch (Exception e){
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
            }
        }
        return invalidTxList;
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
