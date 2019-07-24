package io.nuls.economic.nuls.model.bo;
/**
 * 轮次相关信息
 * Round related information
 *
 * @author tag
 * 2019/7/23
 * */
public class RoundInfo {
    private double totalWeight;
    private long roundStartTime;
    private long roundEndTime;
    private int memberCount;

    public RoundInfo(){}

    public RoundInfo(double totalWeight,long roundStartTime,long roundEndTime,int memberCount){
        this.totalWeight = totalWeight;
        this.roundStartTime = roundStartTime;
        this.roundEndTime = roundEndTime;
        this.memberCount = memberCount;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public long getRoundEndTime() {
        return roundEndTime;
    }

    public void setRoundEndTime(long roundEndTime) {
        this.roundEndTime = roundEndTime;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}
