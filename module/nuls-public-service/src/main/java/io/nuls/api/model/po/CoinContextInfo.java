package io.nuls.api.model.po;

import java.math.BigInteger;
import java.util.List;

public class CoinContextInfo {

    private BigInteger total;

    private BigInteger consensusTotal;

    private BigInteger circulation;

    private BigInteger dailyReward;

    private BigInteger business;

    private BigInteger team;

    private BigInteger community;

    private BigInteger unmapped;

    private BigInteger destroy;

    private long txCount;

    private List<DestroyInfo> destroyInfoList;

    public CoinContextInfo() {
        total = BigInteger.ZERO;
        consensusTotal = BigInteger.ZERO;
        circulation = BigInteger.ZERO;
        dailyReward = BigInteger.ZERO;
        business = BigInteger.ZERO;
        team = BigInteger.ZERO;
        community = BigInteger.ZERO;
        unmapped = BigInteger.ZERO;
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

    public BigInteger getDailyReward() {
        return dailyReward;
    }

    public void setDailyReward(BigInteger dailyReward) {
        this.dailyReward = dailyReward;
    }

    public long getTxCount() {
        return txCount;
    }

    public void setTxCount(long txCount) {
        this.txCount = txCount;
    }

    public BigInteger getBusiness() {
        return business;
    }

    public void setBusiness(BigInteger business) {
        this.business = business;
    }

    public BigInteger getTeam() {
        return team;
    }

    public void setTeam(BigInteger team) {
        this.team = team;
    }

    public BigInteger getCommunity() {
        return community;
    }

    public void setCommunity(BigInteger community) {
        this.community = community;
    }

    public BigInteger getUnmapped() {
        return unmapped;
    }

    public void setUnmapped(BigInteger unmapped) {
        this.unmapped = unmapped;
    }


    public BigInteger getDestroy() {
        return destroy;
    }

    public void setDestroy(BigInteger destroy) {
        this.destroy = destroy;
    }

    public List<DestroyInfo> getDestroyInfoList() {
        return destroyInfoList;
    }

    public void setDestroyInfoList(List<DestroyInfo> destroyInfoList) {
        this.destroyInfoList = destroyInfoList;
    }
}
