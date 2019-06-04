package io.nuls.poc.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
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
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.utils.LoggerUtil;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.validator.TxValidator;

import java.util.*;

@Component("StopAgentProcessorV1")
public class StopAgentProcessor implements TransactionProcessor {
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;

    @Override
    public int getType() {
        return TxType.STOP_AGENT;
    }

    @Override
    public int getPriority() {
        return 6;
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
        Set<NulsHash> hashSet = new HashSet<>();
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
        List<Transaction> contractStopAgentTxList = txMap.get(TxType.CONTRACT_STOP_AGENT);
        if(contractStopAgentTxList != null && contractStopAgentTxList.size() >0){
            try {
                for (Transaction contractStopAgentTx:contractStopAgentTxList) {
                    StopAgent stopAgent = new StopAgent();
                    stopAgent.parse(contractStopAgentTx.getTxData(), 0);
                    hashSet.add(stopAgent.getCreateTxHash());
                }
            }catch (Exception e){
                chain.getLogger().error(e);
            }
        }
        for (Transaction stopAgentTx:txs) {
            try {
                if(!txValidator.validateTx(chain, stopAgentTx)){
                    invalidTxList.add(stopAgentTx);
                    chain.getLogger().info("Intelligent Contract Exit Node Trading Verification Failed");
                    continue;
                }
                StopAgent stopAgent = new StopAgent();
                stopAgent.parse(stopAgentTx.getTxData(), 0);
                if (!hashSet.add(stopAgent.getCreateTxHash())) {
                    invalidTxList.add(stopAgentTx);
                    chain.getLogger().info("Repeated transactions");
                    continue;
                }
                Agent agent = new Agent();
                if (stopAgent.getAddress() == null) {
                    Transaction createAgentTx = CallMethodUtils.getTransaction(chain, stopAgent.getCreateTxHash().toHex());
                    if (createAgentTx == null) {
                        invalidTxList.add(stopAgentTx);
                        chain.getLogger().info("The creation node transaction corresponding to intelligent contract cancellation node transaction does not exist");
                        continue;
                    }
                    agent.parse(createAgentTx.getTxData(), 0);
                    stopAgent.setAddress(agent.getAgentAddress());
                }
                if (!redPunishAddressSet.isEmpty()) {
                    if (redPunishAddressSet.contains(HexUtil.encode(stopAgent.getAddress())) || redPunishAddressSet.contains(HexUtil.encode(agent.getPackingAddress()))) {
                        invalidTxList.add(stopAgentTx);
                        chain.getLogger().info("Intelligent contract cancellation node transaction cancellation node does not exist");
                    }
                }
            }catch (Exception e){
                invalidTxList.add(stopAgentTx);
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
                if(agentManager.stopAgentCommit(tx,blockHeader,chain)){
                    commitSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to create node transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if(!commitResult){
            for (Transaction rollbackTx:commitSuccessList) {
                try {
                    agentManager.stopAgentRollBack(rollbackTx, chain, blockHeader);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to create node transaction rollback");
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
                if(agentManager.stopAgentRollBack(tx,chain,blockHeader)){
                    rollbackSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to stop agent transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if(!rollbackResult){
            for (Transaction commitTx:rollbackSuccessList) {
                try {
                    agentManager.stopAgentCommit(commitTx, blockHeader, chain);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to stop agent transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }
}
