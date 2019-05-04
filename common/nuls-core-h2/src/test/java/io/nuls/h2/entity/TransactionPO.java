package io.nuls.h2.entity;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public class TransactionPO {

    private String address;

    private String hash;

    private Integer type;

    private Long amount;

    /**
     * 0:转出, 1:转入, 2:冻结
     */
    private Integer state;

    private Long time;



    /**
     * 以账户地址来分表储存
     * 交易地址的hashCode 与 100 取模
     * 得到本条数据存在哪张表里面
     * @return
     */
    public int getTableIndex(){
        return this.address.hashCode() % 100;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}
