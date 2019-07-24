package io.nuls.economic.nuls.model.bo;

import java.math.BigInteger;

/**
 * 当前通胀阶段详情
 * Details of the current inflation stage
 *
 * @author tag
 * 2019/7/23
 * */
public class InflationInfo {
    private long startTime;
    private long endTime;
    private BigInteger inflationAmount ;
    private double awardUnit;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public BigInteger getInflationAmount() {
        return inflationAmount;
    }

    public void setInflationAmount(BigInteger inflationAmount) {
        this.inflationAmount = inflationAmount;
    }

    public double getAwardUnit() {
        return awardUnit;
    }

    public void setAwardUnit(double awardUnit) {
        this.awardUnit = awardUnit;
    }
}
