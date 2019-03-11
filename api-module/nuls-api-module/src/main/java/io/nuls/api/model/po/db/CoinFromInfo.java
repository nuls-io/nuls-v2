package io.nuls.api.model.po.db;

import lombok.Data;

import java.math.BigInteger;

@Data
public class CoinFromInfo {

    private String address;

    private int chainId;

    private int assetsId;

    private BigInteger amount;

    private int locked;

    private String nonce;
}
