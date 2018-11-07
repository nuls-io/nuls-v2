package io.nuls.poc.model.bo.config;
/**
 * 共识模块配置类
 * @author tag
 * 2018/11/7
 * */
public class ConfigBean {
    /**
     * 打包间隔时间
     * */
    private long packing_interval;
    /**
     * 出块最小金额
     * */
    private long packing_amount;
    /**
     * 奖励金锁定块数
     * */
    private int coinbase_unlock_height;
    /**
     * 获得红牌保证金锁定时间
     * */
    private long redPublish_lockTime;
    /**
     * 注销节点保证金锁定时间
     * */
    private long stopAgent_lockTime;
    /**
     * 佣金比例的最小值
     * */
    private double commissionRate_min;
    /**
     * 佣金比例的最大值
     * */
    private double commissionRate_max;
    /**
     * 创建节点的保证金最小值
     * */
    private long deposit_min;
    /**
     * 创建节点的保证金最大值
     * */
    private long deposit_max;
    /**
     * 委托金额最小值
     * */
    private long commission_min;
    /**
     * 委托金额最大值
     * */
    private long commission_max;

    public long getPacking_interval() {
        return packing_interval;
    }

    public void setPacking_interval(long packing_interval) {
        this.packing_interval = packing_interval;
    }

    public long getPacking_amount() {
        return packing_amount;
    }

    public void setPacking_amount(long packing_amount) {
        this.packing_amount = packing_amount;
    }

    public int getCoinbase_unlock_height() {
        return coinbase_unlock_height;
    }

    public void setCoinbase_unlock_height(int coinbase_unlock_height) {
        this.coinbase_unlock_height = coinbase_unlock_height;
    }

    public long getRedPublish_lockTime() {
        return redPublish_lockTime;
    }

    public void setRedPublish_lockTime(long redPublish_lockTime) {
        this.redPublish_lockTime = redPublish_lockTime;
    }

    public long getStopAgent_lockTime() {
        return stopAgent_lockTime;
    }

    public void setStopAgent_lockTime(long stopAgent_lockTime) {
        this.stopAgent_lockTime = stopAgent_lockTime;
    }

    public double getCommissionRate_min() {
        return commissionRate_min;
    }

    public void setCommissionRate_min(double commissionRate_min) {
        this.commissionRate_min = commissionRate_min;
    }

    public double getCommissionRate_max() {
        return commissionRate_max;
    }

    public void setCommissionRate_max(double commissionRate_max) {
        this.commissionRate_max = commissionRate_max;
    }

    public long getDeposit_min() {
        return deposit_min;
    }

    public void setDeposit_min(long deposit_min) {
        this.deposit_min = deposit_min;
    }

    public long getDeposit_max() {
        return deposit_max;
    }

    public void setDeposit_max(long deposit_max) {
        this.deposit_max = deposit_max;
    }

    public long getCommission_min() {
        return commission_min;
    }

    public void setCommission_min(long commission_min) {
        this.commission_min = commission_min;
    }

    public long getCommission_max() {
        return commission_max;
    }

    public void setCommission_max(long commission_max) {
        this.commission_max = commission_max;
    }
}
