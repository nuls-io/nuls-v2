package io.nuls.poc.utils.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.*;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;

import java.util.*;

/**
 * 共识模块批量验证器
 * Consensus Module Batch Verifier
 *
 * @author tag
 * 2018/12/11
 * */
@Component
public class BatchValidator {

    /**
     * 共识模块交易批量验证方法,返回验证未通过的交易
     * Batch Verification Method for Consensus Module Transactions
     *
     * @param txList transaction list
     * @param chain  chain info
     * */
    public void batchValid(List<Transaction> txList, Chain chain)throws NulsException {
        if (null == txList || txList.isEmpty()) {
            throw new NulsException(ConsensusErrorCode.TRANSACTION_LIST_IS_NULL);
        }
        List<Transaction> redPunishTxs = new ArrayList<>();
        List<Transaction> createAgentTxs = new ArrayList<>();
        List<Transaction> stopAgentTxs = new ArrayList<>();
        List<Transaction> depositTxs = new ArrayList<>();
        List<Transaction> withdrawTxs = new ArrayList<>();
        for (Transaction tx : txList) {
            switch (tx.getType()){
                case ConsensusConstant.TX_TYPE_RED_PUNISH : redPunishTxs.add(tx);
                    break;
                case ConsensusConstant.TX_TYPE_REGISTER_AGENT : createAgentTxs.add(tx);
                    break;
                case ConsensusConstant.TX_TYPE_STOP_AGENT : stopAgentTxs.add(tx);
                    break;
                case ConsensusConstant.TX_TYPE_JOIN_CONSENSUS : depositTxs.add(tx);
                    break;
                case ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT : withdrawTxs.add(tx);
                    break;
                default:break;
            }
        }
        Set<String> redPunishAddressSet = new HashSet<>();
        Set<NulsDigestData> invalidAgentHash = new HashSet<>();
        if(!redPunishTxs.isEmpty()){
            redPunishAddressSet = redPunishValid(redPunishTxs);
        }

        if(!redPunishAddressSet.isEmpty() && !createAgentTxs.isEmpty()){
            createAgentValid(createAgentTxs,redPunishAddressSet);
        }

        if(!stopAgentTxs.isEmpty()){
            stopAgentValid(stopAgentTxs,redPunishAddressSet);
        }

        if(!redPunishAddressSet.isEmpty() || !stopAgentTxs.isEmpty()){
            invalidAgentHash = getInvalidAgentHash(redPunishAddressSet,stopAgentTxs,chain);
        }

        if(!invalidAgentHash.isEmpty() && !depositTxs.isEmpty()){
            depositValid(depositTxs,invalidAgentHash);
        }

        if (!withdrawTxs.isEmpty()){
            withdrawValid(withdrawTxs,invalidAgentHash);
        }

        txList.removeAll(redPunishTxs);
        txList.removeAll(createAgentTxs);
        txList.removeAll(stopAgentTxs);
        txList.removeAll(depositTxs);
        txList.removeAll(withdrawTxs);
    }

    /**
     * 共识模块红牌交易批量验证方法
     * Bulk Verification Method for Red Card Trading in Consensus Module
     *
     * @param redPunishTxs    red punish transaction list
     * */
    private Set<String> redPunishValid(List<Transaction>redPunishTxs)throws NulsException{
        Set<String> addressHexSet = new HashSet<>();
        Iterator<Transaction> iterator = redPunishTxs.iterator();
        RedPunishData redPunishData = new RedPunishData();
        Transaction tx;
        while (iterator.hasNext()){
            tx = iterator.next();
            redPunishData.parse(tx.getTxData(),0);
            String addressHex = AddressTool.getStringAddressByBytes(redPunishData.getAddress());
            /*
            * 重复的红牌交易不打包
            * */
            if(!addressHexSet.add(addressHex)){
               iterator.remove();
            }
        }
        return addressHexSet;
    }

    /**
     * 共识模块创建节点交易批量验证方法
     * Creating Batch Verification Method for Node Transactions in Consensus Module
     *
     * @param createTxs              create agent transaction list
     * @param redPunishAddressSet    red punish address list
     * */
    private void createAgentValid(List<Transaction>createTxs,Set<String> redPunishAddressSet)throws NulsException{
        Iterator<Transaction> iterator = createTxs.iterator();
        Agent agent = new Agent();
        String agentAddressHex;
        String packAddressHex;
        Set<String> createAgentAddressSet = new HashSet<>();
        while (iterator.hasNext()){
            agent.parse(iterator.next().getTxData(),0);
            agentAddressHex = HexUtil.encode(agent.getAgentAddress());
            packAddressHex = HexUtil.encode(agent.getPackingAddress());
            if(!redPunishAddressSet.isEmpty()){
                if(redPunishAddressSet.contains(agentAddressHex) || redPunishAddressSet.contains(packAddressHex)){
                    iterator.remove();
                    continue;
                }
            }
            if(!createAgentAddressSet.add(agentAddressHex) || !createAgentAddressSet.add(packAddressHex)){
                iterator.remove();
            }
        }
    }

    /**
     * 共识模块停止节点交易批量验证方法
     * Batch Verification Method for Stopping Node Trading in Consensus Module
     *
     * @param stopAgentTxs              transaction list
     * @param redPunishAddressSet       red punish address list
     * */
    private void stopAgentValid(List<Transaction>stopAgentTxs,Set<String> redPunishAddressSet)throws NulsException{
        Set<NulsDigestData> hashSet = new HashSet<>();Iterator<Transaction> iterator = stopAgentTxs.iterator();
        StopAgent stopAgent = new StopAgent();
        while (iterator.hasNext()){
            stopAgent.parse(iterator.next().getTxData(),0);
            if(!hashSet.add(stopAgent.getCreateTxHash())){
                iterator.remove();
                continue;
            }
            if(stopAgent.getAddress() == null){
                //todo 从交易模块获取创建该节点的交易
                Transaction createAgentTx = new Transaction();
                if(createAgentTx == null){
                    iterator.remove();
                    continue;
                }
                Agent agent = new Agent();
                agent.parse(createAgentTx.getTxData(),0);
                stopAgent.setAddress(agent.getAgentAddress());
            }
            if(!redPunishAddressSet.isEmpty() && redPunishAddressSet.contains(AddressTool.getStringAddressByBytes(stopAgent.getAddress()))){
                iterator.remove();
            }
        }
    }

    /**
     * 共识模块委托交易批量验证方法
     * Batch Verification Method of Delegated Transactions in Consensus Module
     *
     * @param depositTxs  deposit transaction list
     * */
    private void depositValid(List<Transaction>depositTxs,Set<NulsDigestData> invalidAgentHash)throws NulsException{
        Deposit deposit = new Deposit();
        Iterator<Transaction>iterator = depositTxs.iterator();
        while (iterator.hasNext()){
            deposit.parse(iterator.next().getTxData(),0);
            if(invalidAgentHash.contains(deposit.getAgentHash())){
                iterator.remove();
            }
        }
    }

    /**
     * 共识模块退出委托交易批量验证方法
     * Volume Verification Method for Consensus Module Exit from Delegated Transactions
     *
     * @param withdrawTxs               withdraw  transaction list
     * @param invalidAgentHash          invalid agent hash
     * */
    private void withdrawValid(List<Transaction>withdrawTxs,Set<NulsDigestData> invalidAgentHash)throws NulsException{
        Iterator<Transaction> iterator = withdrawTxs.iterator();
        CancelDeposit cancelDeposit = new CancelDeposit();
        Set<NulsDigestData> hashSet = new HashSet<>();
        while(iterator.hasNext()){
            cancelDeposit.parse(iterator.next().getTxData(),0);
            if (!hashSet.add(cancelDeposit.getJoinTxHash())) {
                iterator.remove();
                continue;
            }
            if(!invalidAgentHash.isEmpty() && invalidAgentHash.contains(cancelDeposit.getJoinTxHash())){
                iterator.remove();
            }
        }
    }

    /**
     * 获取区块交易列表中，红牌交易或停止节点交易对应的节点Hash列表
     * Get the node Hash list corresponding to the block transaction list, the red card transaction or the stop node transaction
     *
     * @param redPunishAddressSet   红牌处罚节点地址/Red card penalty node address
     * @param stopAgentTxs          停止节点交易列表/Stop Node Trading List
     * @param chain                 chain info
     * */
    private Set<NulsDigestData> getInvalidAgentHash( Set<String> redPunishAddressSet,List<Transaction>stopAgentTxs,Chain chain)throws NulsException{
        Set<NulsDigestData> agentHashSet = new HashSet<>();
        List<Agent> agentList = chain.getAgentList();
        long startBlockHeight = chain.getNewestHeader().getHeight();
        if(!redPunishAddressSet.isEmpty()){
            for (Agent agent:agentList) {
                if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                    continue;
                }
                if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                    continue;
                }
                if(redPunishAddressSet.contains(HexUtil.encode(agent.getAgentAddress()))){
                    agentHashSet.add(agent.getTxHash());
                }
            }
        }
        if(stopAgentTxs != null){
            StopAgent stopAgent = new StopAgent();
            for (Transaction tx:stopAgentTxs) {
                stopAgent.parse(tx.getTxData(),0);
                agentHashSet.add(stopAgent.getCreateTxHash());
            }
        }
        return agentHashSet;
    }
}
