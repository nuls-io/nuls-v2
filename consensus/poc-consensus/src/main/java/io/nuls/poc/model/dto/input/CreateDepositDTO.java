package io.nuls.poc.model.dto.input;
/**
 * 创建委托交易参数类
 * Create delegate transaction parameter class
 *
 * @author tag
 * 2018/11/12
 * */
public class CreateDepositDTO {
    private int chainId;
    private String address;
    private String agentHash;
    private String deposit;
    private String password;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
