package io.nuls.api.model.po.db;

import java.math.BigInteger;

public class CoinContextInfo {

    private BigInteger total;

    private BigInteger consensusTotal;

    private BigInteger circulation;

    public CoinContextInfo() {
        total = BigInteger.ZERO;
        consensusTotal = BigInteger.ZERO;
        circulation = BigInteger.ZERO;
    }

    public BigInteger getTotal() {
        return total;
    }

    public void setTotal(BigInteger total) {
        this.total = total;
    }

    public BigInteger getConsensusTotal() {
        return consensusTotal;
    }

    public void setConsensusTotal(BigInteger consensusTotal) {
        this.consensusTotal = consensusTotal;
    }

    public BigInteger getCirculation() {
        return circulation;
    }

    public void setCirculation(BigInteger circulation) {
        this.circulation = circulation;
    }
}
