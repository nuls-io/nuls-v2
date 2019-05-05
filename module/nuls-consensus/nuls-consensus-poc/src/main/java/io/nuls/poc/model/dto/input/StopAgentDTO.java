package io.nuls.poc.model.dto.input;

/**
 * 停止节点参数类
 * Stop Node Parameter Class
 *
 * @author tag
 * 2018/11/12
 * */
public class StopAgentDTO {
    private int chainId;
    private String address;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
