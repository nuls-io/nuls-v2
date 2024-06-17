package io.nuls.consensus.economic.nuls.model.bo;

import java.math.BigDecimal;
/**
 * Details of current inflation stage
 * Details of the current inflation stage
 *
 * @author tag
 * 2019/7/23
 * */
public class InflationInfo {
    private long startTime;
    private long endTime;
    private BigDecimal inflationAmount ;
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

    public BigDecimal getInflationAmount() {
        return inflationAmount;
    }

    public void setInflationAmount(BigDecimal inflationAmount) {
        this.inflationAmount = inflationAmount;
    }

    public double getAwardUnit() {
        return awardUnit;
    }

    public void setAwardUnit(double awardUnit) {
        this.awardUnit = awardUnit;
    }
}
