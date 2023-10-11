package io.nuls.consensus.utils.validator;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.tx.txdata.*;
import io.nuls.consensus.model.po.AgentPo;
import io.nuls.consensus.model.po.DepositPo;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.consensus.storage.AgentStorageService;
import io.nuls.consensus.storage.DepositStorageService;

import java.io.IOException;
import java.util.*;

/**
 * 共识模块批量验证器
 * Consensus Module Batch Verifier
 *
 * @author tag
 * 2018/12/11
 */
@Component
public class BatchValidator {
    @Autowired
    private DepositStorageService depositStorageService;

    @Autowired
    private AgentStorageService agentStorageService;

    @Autowired
    private TxValidator txValidator;

    /**
     * 共识模块交易批量验证方法,返回验证未通过的交易
     * Batch Verification Method for Consensus Module Transactions
     *
     * @param txList transaction list
     * @param chain  chain info
     */
    public void batchValid(List<Transaction> txList, Chain chain) throws NulsException {
        if (null == txList || txList.isEmpty()) {
            throw new NulsException(ConsensusErrorCode.TRANSACTION_LIST_IS_NULL);
        }
        txList.sort((tx, compareTx) -> {
            if (tx.getType() == compareTx.getType()) {
                return (int) (tx.getTime() - compareTx.getTime());
            } else {
                return compareTx.getType() - tx.getType();
            }
        });
        List<Transaction> redPunishTxs = new ArrayList<>();
        List<Transaction> yellowPunishTxs = new ArrayList<>();
        List<Transaction> coinBasePunishTxs = new ArrayList<>();
        List<Transaction> createAgentTxs = new ArrayList<>();
        List<Transaction> stopAgentTxs = new ArrayList<>();
        List<Transaction> depositTxs = new ArrayList<>();
        List<Transaction> withdrawTxs = new ArrayList<>();
        for (Transaction tx : txList) {
            switch (tx.getType()) {
                case TxType.RED_PUNISH:
                    redPunishTxs.add(tx);
                    break;
                case TxType.REGISTER_AGENT:
                case TxType.CONTRACT_CREATE_AGENT:
                    createAgentTxs.add(tx);
                    break;
                case TxType.STOP_AGENT:
                case TxType.CONTRACT_STOP_AGENT:
                    stopAgentTxs.add(tx);
                    break;
                case TxType.DEPOSIT:
                case TxType.CONTRACT_DEPOSIT:
                    depositTxs.add(tx);
                    break;
                case TxType.CANCEL_DEPOSIT:
                case TxType.CONTRACT_CANCEL_DEPOSIT:
                    withdrawTxs.add(tx);
                    break;
                case TxType.YELLOW_PUNISH:
                    yellowPunishTxs.add(tx);
                    break;
                case TxType.COIN_BASE:
                    coinBasePunishTxs.add(tx);
                    break;
                default:
                    break;
            }
        }
        Set<String> redPunishAddressSet = new HashSet<>();
        Set<NulsHash> invalidAgentHash = new HashSet<>();
        if (!redPunishTxs.isEmpty()) {
            redPunishAddressSet = redPunishValid(redPunishTxs);
        }
        if (!redPunishAddressSet.isEmpty() || !createAgentTxs.isEmpty()) {
            createAgentValid(createAgentTxs, redPunishAddressSet, chain);
        }
        if (!stopAgentTxs.isEmpty()) {
            stopAgentValid(stopAgentTxs, redPunishAddressSet, chain);
        }
        if (!depositTxs.isEmpty() || !withdrawTxs.isEmpty()) {
            if (!redPunishAddressSet.isEmpty() || !stopAgentTxs.isEmpty()) {
                invalidAgentHash = getInvalidAgentHash(redPunishAddressSet, stopAgentTxs, chain);
            }
        }
        if (!invalidAgentHash.isEmpty() && !depositTxs.isEmpty()) {
            depositValid(depositTxs, invalidAgentHash, chain);
        }
        if (!withdrawTxs.isEmpty()) {
            withdrawValid(withdrawTxs, invalidAgentHash, chain);
        }
        txList.removeAll(redPunishTxs);
        txList.removeAll(createAgentTxs);
        txList.removeAll(stopAgentTxs);
        txList.removeAll(depositTxs);
        txList.removeAll(withdrawTxs);
        txList.removeAll(yellowPunishTxs);
        txList.removeAll(coinBasePunishTxs);
    }

    /**
     * 共识模块红牌交易批量验证方法
     * Bulk Verification Method for Red Card Trading in Consensus Module
     *
     * @param redPunishTxs red punish transaction list
     */
    private Set<String> redPunishValid(List<Transaction> redPunishTxs) throws NulsException {
        Set<String> addressHexSet = new HashSet<>();
        Iterator<Transaction> iterator = redPunishTxs.iterator();
        RedPunishData redPunishData = new RedPunishData();
        Transaction tx;
        while (iterator.hasNext()) {
            tx = iterator.next();
            redPunishData.parse(tx.getTxData(), 0);
            String addressHex = HexUtil.encode(redPunishData.getAddress());
            /*
             * 重复的红牌交易不打包
             * */
            if (!addressHexSet.add(addressHex)) {
                iterator.remove();
            }
        }
        return addressHexSet;
    }

    /**
     * 共识模块创建节点交易批量验证方法
     * Creating Batch Verification Method for Node Transactions in Consensus Module
     *
     * @param createTxs           create agent transaction list
     * @param redPunishAddressSet red punish address list
     */
    private void createAgentValid(List<Transaction> createTxs, Set<String> redPunishAddressSet, Chain chain) throws NulsException {
        Iterator<Transaction> iterator = createTxs.iterator();
        Agent agent = new Agent();
        String agentAddressHex;
        String packAddressHex;
        Set<String> createAgentAddressSet = new HashSet<>();
        boolean basicValidResult;
        while (iterator.hasNext()) {
            Transaction tx = iterator.next();
            try {
                basicValidResult = txValidator.validateTx(chain, tx);
                if (!basicValidResult) {
                    iterator.remove();
                    continue;
                }
            } catch (IOException e) {
                iterator.remove();
                continue;
            }
            agent.parse(tx.getTxData(), 0);
            agentAddressHex = HexUtil.encode(agent.getAgentAddress());
            packAddressHex = HexUtil.encode(agent.getPackingAddress());
            /*
             * 获得过红牌交易的地址不能创建节点
             * */
            if (!redPunishAddressSet.isEmpty()) {
                if (redPunishAddressSet.contains(agentAddressHex) || redPunishAddressSet.contains(packAddressHex)) {
                    iterator.remove();
                    continue;
                }
            }
            /*
             * 重复创建节点
             * */
            if (!createAgentAddressSet.add(agentAddressHex) || !createAgentAddressSet.add(packAddressHex)) {
                iterator.remove();
            }
        }
    }

    /**
     * 共识模块停止节点交易批量验证方法
     * Batch Verification Method for Stopping Node Trading in Consensus Module
     *
     * @param stopAgentTxs        transaction list
     * @param redPunishAddressSet red punish address list
     */
    private void stopAgentValid(List<Transaction> stopAgentTxs, Set<String> redPunishAddressSet, Chain chain) throws NulsException {
        Set<NulsHash> hashSet = new HashSet<>();
        Iterator<Transaction> iterator = stopAgentTxs.iterator();
        StopAgent stopAgent = new StopAgent();
        Agent agent = new Agent();
        boolean basicValidResult;
        while (iterator.hasNext()) {
            Transaction tx = iterator.next();
            try {
                basicValidResult = txValidator.validateTx(chain, tx);
                if (!basicValidResult) {
                    iterator.remove();
                    continue;
                }
            } catch (IOException e) {
                iterator.remove();
                continue;
            }
            stopAgent.parse(tx.getTxData(), 0);
            if (!hashSet.add(stopAgent.getCreateTxHash())) {
                iterator.remove();
                continue;
            }
            if (stopAgent.getAddress() == null) {
                Transaction createAgentTx = CallMethodUtils.getTransaction(chain, stopAgent.getCreateTxHash().toHex());
                if (createAgentTx == null) {
                    iterator.remove();
                    continue;
                }
                agent.parse(createAgentTx.getTxData(), 0);
                stopAgent.setAddress(agent.getAgentAddress());
            }
            if (!redPunishAddressSet.isEmpty()) {
                if (redPunishAddressSet.contains(HexUtil.encode(stopAgent.getAddress())) || redPunishAddressSet.contains(HexUtil.encode(agent.getPackingAddress()))) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 共识模块委托交易批量验证方法
     * Batch Verification Method of Delegated Transactions in Consensus Module
     *
     * @param depositTxs deposit transaction list
     */
    private void depositValid(List<Transaction> depositTxs, Set<NulsHash> invalidAgentHash, Chain chain) throws NulsException {
        Deposit deposit = new Deposit();
        Iterator<Transaction> iterator = depositTxs.iterator();
        boolean basicValidResult;
        while (iterator.hasNext()) {
            Transaction tx = iterator.next();
            try {
                basicValidResult = txValidator.validateTx(chain, tx);
                if (!basicValidResult) {
                    iterator.remove();
                    continue;
                }
            } catch (IOException e) {
                iterator.remove();
                continue;
            }
            deposit.parse(tx.getTxData(), 0);
            if (invalidAgentHash.contains(deposit.getAgentHash())) {
                iterator.remove();
            }
        }
    }

    /**
     * 共识模块退出委托交易批量验证方法
     * Volume Verification Method for Consensus Module Exit from Delegated Transactions
     *
     * @param withdrawTxs      withdraw  transaction list
     * @param invalidAgentHash invalid agent hash
     */
    private void withdrawValid(List<Transaction> withdrawTxs, Set<NulsHash> invalidAgentHash, Chain chain) throws NulsException {
        Iterator<Transaction> iterator = withdrawTxs.iterator();
        CancelDeposit cancelDeposit = new CancelDeposit();
        Set<NulsHash> hashSet = new HashSet<>();
        int chainId = chain.getConfig().getChainId();
        boolean basicValidResult;
        while (iterator.hasNext()) {
            Transaction tx = iterator.next();
            try {
                basicValidResult = txValidator.validateTx(chain, tx);
                if (!basicValidResult) {
                    iterator.remove();
                    continue;
                }
            } catch (IOException e) {
                iterator.remove();
                continue;
            }
            cancelDeposit.parse(tx.getTxData(), 0);
            /*
             * 重复退出节点
             * */
            if (!hashSet.add(cancelDeposit.getJoinTxHash())) {
                iterator.remove();
                continue;
            }
            DepositPo depositPo = depositStorageService.get(cancelDeposit.getJoinTxHash(), chainId);
            AgentPo agentPo = agentStorageService.get(depositPo.getAgentHash(), chainId);
            if (null == agentPo) {
                iterator.remove();
                continue;
            }
            if (invalidAgentHash.contains(agentPo.getHash())) {
                iterator.remove();
            }
        }
    }

    /**
     * 获取区块交易列表中，红牌交易或停止节点交易对应的节点Hash列表
     * Get the node Hash list corresponding to the block transaction list, the red card transaction or the stop node transaction
     *
     * @param redPunishAddressSet 红牌处罚节点地址/Red card penalty node address
     * @param stopAgentTxs        停止节点交易列表/Stop Node Trading List
     * @param chain               chain info
     */
    private Set<NulsHash> getInvalidAgentHash(Set<String> redPunishAddressSet, List<Transaction> stopAgentTxs, Chain chain) throws NulsException {
        Set<NulsHash> agentHashSet = new HashSet<>();
        List<Agent> agentList = chain.getAgentList();
        long startBlockHeight = chain.getNewestHeader().getHeight();
        if (!redPunishAddressSet.isEmpty()) {
            for (Agent agent : agentList) {
                if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                    continue;
                }
                if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                    continue;
                }
                if (redPunishAddressSet.contains(HexUtil.encode(agent.getAgentAddress())) || redPunishAddressSet.contains(HexUtil.encode(agent.getPackingAddress()))) {
                    agentHashSet.add(agent.getTxHash());
                }
            }
        }
        if (stopAgentTxs != null) {
            StopAgent stopAgent = new StopAgent();
            for (Transaction tx : stopAgentTxs) {
                stopAgent.parse(tx.getTxData(), 0);
                agentHashSet.add(stopAgent.getCreateTxHash());
            }
        }
        return agentHashSet;
    }
}
