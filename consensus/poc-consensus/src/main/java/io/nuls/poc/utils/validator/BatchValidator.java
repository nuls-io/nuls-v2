package io.nuls.poc.utils.validator;

import io.nuls.base.data.Transaction;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
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
     * 共识模块交易批量验证方法
     * Batch Verification Method for Consensus Module Transactions
     *
     * @param txList transaction list
     * */
    public boolean batchValid(List<Transaction> txList)throws NulsException {
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
                default:continue;
            }
        }
        Set<String> redPunishAddressSet =redPunishValid(redPunishTxs);

        return true;
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
            String addressHex = HexUtil.encode(redPunishData.getAddress());
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
        while (iterator.hasNext()){
            agent.parse(iterator.next().getTxData(),0);
            agentAddressHex = HexUtil.encode(agent.getAgentAddress());
            packAddressHex = HexUtil.encode(agent.getPackingAddress());
            if(redPunishAddressSet.contains(agentAddressHex) || redPunishAddressSet.contains(packAddressHex)){
                iterator.remove();
            }
        }
    }

    /**
     * 共识模块停止节点交易批量验证方法
     * Batch Verification Method for Stopping Node Trading in Consensus Module
     *
     * @param txList transaction list
     * */
    private void stopAgentValid(List<Transaction>txList){

    }

    /**
     * 共识模块委托交易批量验证方法
     * Batch Verification Method of Delegated Transactions in Consensus Module
     *
     * @param txList transaction list
     * */
    private void depositValid(List<Transaction>txList){

    }

    /**
     * 共识模块退出委托交易批量验证方法
     * Volume Verification Method for Consensus Module Exit from Delegated Transactions
     *
     * @param txList transaction list
     * */
    private void withdrawValid(List<Transaction>txList){

    }
}
