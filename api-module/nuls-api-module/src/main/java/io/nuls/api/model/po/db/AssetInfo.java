package io.nuls.api.model.po.db;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AssetInfo {

    private int chainId;

    private int AssetId;

    private String symbol;

}
