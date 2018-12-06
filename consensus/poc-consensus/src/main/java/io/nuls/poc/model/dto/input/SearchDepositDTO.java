package io.nuls.poc.model.dto.input;

/**
 * 查询委托信息参数
 * Query delegation information parameters
 *
 * @author tag
 * 2018/11/12
 * */
public class SearchDepositDTO {
    private int chainId;
    private int pageNumber;
    private int pageSize;
    private String address;
    private String agentHash;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
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
}
