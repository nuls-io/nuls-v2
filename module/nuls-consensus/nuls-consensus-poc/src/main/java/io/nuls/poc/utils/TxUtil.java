package io.nuls.poc.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.model.dto.input.CreateAgentDTO;
import io.nuls.poc.model.dto.input.CreateDepositDTO;
import io.nuls.poc.model.dto.input.StopAgentDTO;

import java.util.Arrays;
import java.util.List;

/**
 * 交易工具类
 * Transaction Tool Class
 *
 * @author tag
 * 2019/7/25
 */
public class TxUtil {
    public static Agent createAgent(CreateAgentDTO dto){
        Agent agent = new Agent();
        agent.setAgentAddress(AddressTool.getAddress(dto.getAgentAddress()));
        agent.setPackingAddress(AddressTool.getAddress(dto.getPackingAddress()));
        if (StringUtils.isBlank(dto.getRewardAddress())) {
            agent.setRewardAddress(agent.getAgentAddress());
        } else {
            agent.setRewardAddress(AddressTool.getAddress(dto.getRewardAddress()));
        }
        agent.setDeposit(BigIntegerUtils.stringToBigInteger(dto.getDeposit()));
        agent.setCommissionRate(dto.getCommissionRate());
        return agent;
    }

    public static StopAgent createStopAgent(Chain chain, StopAgentDTO dto) throws NulsException {
        StopAgent stopAgent = new StopAgent();
        stopAgent.setAddress(AddressTool.getAddress(dto.getAddress()));
        List<Agent> agentList = chain.getAgentList();
        Agent agent = null;
        for (Agent a : agentList) {
            if (a.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(a.getAgentAddress(), AddressTool.getAddress(dto.getAddress()))) {
                agent = a;
                break;
            }
        }
        if (agent == null || agent.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        stopAgent.setCreateTxHash(agent.getTxHash());
        return stopAgent;
    }

    public static Deposit createDeposit(CreateDepositDTO dto){
        Deposit deposit = new Deposit();
        deposit.setAddress(AddressTool.getAddress(dto.getAddress()));
        deposit.setAgentHash(NulsHash.fromHex(dto.getAgentHash()));
        deposit.setDeposit(BigIntegerUtils.stringToBigInteger(dto.getDeposit()));
        return deposit;
    }
}
