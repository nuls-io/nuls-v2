package io.nuls.transaction.model.po;

import io.nuls.transaction.constant.TxConstant;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public class TransactionPo {

    private Integer id;

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
     * 交易地址的hashCode 与 H2_TX_TABLE_NUMBER 取模
     * 得到本条数据存在哪张表里面
     * @return
     */
    public int createTableIndex(){
        return (this.address.hashCode() & Integer.MAX_VALUE) % TxConstant.H2_TX_TABLE_NUMBER;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
