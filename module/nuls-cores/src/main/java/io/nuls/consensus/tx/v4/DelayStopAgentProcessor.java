package io.nuls.consensus.tx.v4;

import io.nuls.base.data.*;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.tx.txdata.DelayStopAgent;
import io.nuls.consensus.model.po.AgentPo;
import io.nuls.consensus.model.po.DepositPo;
import io.nuls.consensus.storage.AgentStorageService;
import io.nuls.consensus.storage.DepositStorageService;
import io.nuls.consensus.utils.LoggerUtil;
import io.nuls.consensus.utils.manager.ChainManager;
import io.nuls.consensus.utils.manager.DepositManager;
import io.nuls.consensus.utils.validator.TxValidator;

import java.io.IOException;
import java.util.*;

@Component("DelayStopAgentProcessorV1")
public class DelayStopAgentProcessor implements TransactionProcessor {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AgentStorageService agentStorageService;
    @Autowired
    private DepositStorageService depositStorageService;
    @Autowired
    private TxValidator txValidator;
    @Autowired
    private DepositManager depositManager;

    @Override
    public int getType() {
        return TxType.DELAY_STOP_AGENT;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            result.put("txList", txs);
            result.put("errorCode", ConsensusErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        List<Transaction> invalidTxList = new ArrayList<>();
        String errorCode = null;
        for (Transaction tx : txs) {
            try {
                if (!txValidator.validateTx(chain, tx)) {
                    invalidTxList.add(tx);
                    chain.getLogger().info("Delay stop agent transaction verification failed");
                    continue;
                }
            } catch (NulsException e) {
                invalidTxList.add(tx);
                chain.getLogger().error("Intelligent Delay stop agent Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            } catch (IOException io) {
                invalidTxList.add(tx);
                chain.getLogger().error("Intelligent Delay stop agent Transaction Verification Failed");
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
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        boolean commitResult = true;
        for (Transaction tx : txs) {
            try {
                if (realCommit(tx, chain, blockHeader)) {
                    commitSuccessList.add(tx);
                } else {
                    commitResult = false;
                }
            } catch (NulsException e) {
                chain.getLogger().error("Failure to red punish transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if (!commitResult) {
            for (Transaction rollbackTx : commitSuccessList) {
                try {
                    realRollback(rollbackTx, chain, blockHeader);
                } catch (NulsException e) {
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
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> rollbackSuccessList = new ArrayList<>();
        boolean rollbackResult = true;
        for (Transaction tx : txs) {
            try {
                if (realRollback(tx, chain, blockHeader)) {
                    rollbackSuccessList.add(tx);
                } else {
                    rollbackResult = false;
                }
            } catch (NulsException e) {
                chain.getLogger().error("Failure to red punish transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if (!rollbackResult) {
            for (Transaction commitTx : rollbackSuccessList) {
                try {
                    realCommit(commitTx, chain, blockHeader);
                } catch (NulsException e) {
                    chain.getLogger().error("Failure to red punish transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }


    public boolean realCommit(Transaction tx, Chain chain, BlockHeader blockHeader) throws NulsException {
        long blockHeight = blockHeader.getHeight();
        int chainId = chain.getConfig().getChainId();
        DelayStopAgent txData = new DelayStopAgent();
        txData.parse(tx.getTxData(), 0);

        /*
        找到被惩罚的节点
        Find the punished node
         */
        AgentPo agent = agentStorageService.get(txData.getAgentHash(), chainId);

        if (null == agent) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }

        /*
        找到被惩罚节点的委托
        Delegation to Find Penalized Nodes
         */
        List<DepositPo> depositPoList = depositStorageService.getList(chainId);
        List<DepositPo> updatedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            if (po.getDelHeight() >= 0) {
                continue;
            }
            if (!po.getAgentHash().equals(txData.getAgentHash())) {
                continue;
            }
            po.setDelHeight(blockHeight);
            boolean b = depositStorageService.save(po, chainId);
            if (!b) {
                for (DepositPo po2 : updatedList) {
                    po2.setDelHeight(-1);
                    this.depositStorageService.save(po2, chainId);
                }
                throw new NulsException(ConsensusErrorCode.SAVE_FAILED);
            }
            updatedList.add(po);
        }
        /*
         * 更新缓存
         * */
        if (!updatedList.isEmpty()) {
            for (DepositPo depositPo : updatedList) {
                depositManager.updateDeposit(chain, depositManager.poToDeposit(depositPo));
            }
        }
        return true;
    }

    public boolean realRollback(Transaction tx, Chain chain, BlockHeader blockHeader) throws NulsException {
        long blockHeight = blockHeader.getHeight();
        int chainId = chain.getConfig().getChainId();
        DelayStopAgent txData = new DelayStopAgent();
        txData.parse(tx.getTxData(), 0);
        /*
        找到被惩罚的节点
        Find the punished node
         */
        AgentPo agent1 = agentStorageService.get(txData.getAgentHash(), chainId);

        if (null == agent1) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }

        /*
        找到被惩罚节点的委托
        Delegation to Find Penalized Nodes
         */
        List<DepositPo> depositPoList = depositStorageService.getList(chainId);
        List<DepositPo> updatedList = new ArrayList<>();
        for (DepositPo po : depositPoList) {
            if (!po.getAgentHash().equals(txData.getAgentHash())) {
                continue;
            }
            if (po.getDelHeight() == blockHeight) {
                po.setDelHeight(-1);
            }
            boolean success = this.depositStorageService.save(po, chainId);
            if (!success) {
                for (DepositPo po2 : updatedList) {
                    po2.setDelHeight(blockHeight);
                    this.depositStorageService.save(po2, chainId);
                }
                throw new NulsException(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            updatedList.add(po);
        }

        /*
         * 修改缓存
         * */
        if (!updatedList.isEmpty()) {
            for (DepositPo po2 : updatedList) {
                depositManager.updateDeposit(chain, depositManager.poToDeposit(po2));
            }
        }
        return true;
    }
}
