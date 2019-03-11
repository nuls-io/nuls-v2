package io.nuls.api.model.po.db;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CoinToInfo {

    private String address;

    private int chainId;

    private int assetsId;

    private BigInteger amount;

    private String nonce;

    private long lockTime;
}
