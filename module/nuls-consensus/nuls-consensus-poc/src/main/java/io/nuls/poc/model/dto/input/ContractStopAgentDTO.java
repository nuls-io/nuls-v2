package io.nuls.poc.model.dto.input;

/**
 * 智能合约创建注销参数
 * Intelligent Contract Stop Node Parameters
 *
 * @author tag
 * 2019/5/5
 * */
public class ContractStopAgentDTO {
    private int chainId;
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
