package io.nuls.api.model.po.db;

import io.nuls.base.data.Address;
import lombok.Data;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
public class AccountInfo {

    private String address;

    private String alias;

    private int type;

    private int txCount;

    private BigInteger totalOut;

    private BigInteger totalIn;

    private BigInteger consensusLock;

    private BigInteger timeLock;

    private BigInteger balance;

    private BigInteger totalBalance;

    private List<String> tokens;

    //是否是根据最新区块的交易新创建的账户，只为业务使用，不存储该字段
    private boolean isNew;

    public AccountInfo() {

    }

    public AccountInfo(String address) {
        this.address = address;
        Address address1 = new Address(address);
        this.type = address1.getAddressType();
        this.tokens = new ArrayList<>();
        this.isNew = true;
    }
}
