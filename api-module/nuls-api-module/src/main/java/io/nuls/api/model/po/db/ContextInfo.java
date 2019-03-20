package io.nuls.api.model.po.db;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ContextInfo {

    private BigInteger total;

    private BigInteger consensusTotal;

    private BigInteger circulation;

    public ContextInfo() {
        total = BigInteger.ZERO;
        consensusTotal = BigInteger.ZERO;
        circulation = BigInteger.ZERO;
    }
}
