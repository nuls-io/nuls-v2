package io.nuls.consensus.model.dto.input;

/**
 * Smart contract query commission information parameters
 * Intelligent Contract Creation Node Parameters
 *
 * @author tag
 * 2019/5/5
 * */
public class SearchContractDepositDTO {
    private int chainId;
    private String joinAgentHash;
    private String contractAddress;
    private String contractSender;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getJoinAgentHash() {
        return joinAgentHash;
    }

    public void setJoinAgentHash(String joinAgentHash) {
        this.joinAgentHash = joinAgentHash;
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
}
