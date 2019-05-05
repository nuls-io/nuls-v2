package io.nuls.poc.model.dto.input;

/**
 * 查询红/黄牌牌参数类
 * Query Red/Yellow Card Parameter Class
 *
 * @author tag
 * 2018/11/12
 * */
public class SearchPunishDTO {
    private int chainId;
    private String address;
    private int type;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
