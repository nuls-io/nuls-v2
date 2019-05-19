package io.nuls.poc.model.dto.input;

/**
 * 智能合约委托共识参数
 * Intelligent Contract Delegation Consensus Parameters
 *
 * @author tag
 * 2019/5/5
 * */
public class ContractDepositDTO {
    private int chainId;
    private String agentHash;
    private String deposit;
    private String contractAddress;
    private String contractSender;
    private String contractBalance;
    private String contractNonce;
    private long blockTime;

    public long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractSender() {
        return contractSender;
    }

    public void setContractSender(String contractSender) {
        this.contractSender = contractSender;
    }

    public String getContractBalance() {
        return contractBalance;
    }

    public void setContractBalance(String contractBalance) {
        this.contractBalance = contractBalance;
    }

    public String getContractNonce() {
        return contractNonce;
    }

    public void setContractNonce(String contractNonce) {
        this.contractNonce = contractNonce;
    }
}
