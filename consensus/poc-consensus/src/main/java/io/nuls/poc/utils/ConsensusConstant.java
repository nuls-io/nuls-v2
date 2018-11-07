package io.nuls.poc.utils;

/**
 * @author tag
 * 2018/11/6
 * */
public class ConsensusConstant {
    /**
     * consensus transaction types
     * */
    public static int TX_TYPE_REGISTER_AGENT = 4;
    public static int TX_TYPE_JOIN_CONSENSUS = 5;
    public static int TX_TYPE_CANCEL_DEPOSIT = 6;
    public static int TX_TYPE_YELLOW_PUNISH = 7;
    public static int TX_TYPE_RED_PUNISH = 8;
    public static int TX_TYPE_STOP_AGENT = 9;

    /**
     * Consensus module related table name/共识模块相关表明
     * */
    public static String DB_NAME_CONSENSUS_AGENT = "consensus_agent";
    public static String DB_NAME_CONSENSUS_DEPOSIT = "consensus_deposit";
    public static String DB_NAME_CONSENSUS_PUNISH = "consensus_punish";
    public static String DB_NAME_CONSUME_TX = "consensus_tx";

    /**
     * config param
     * */
    public static String PACKING_INTERVAL = "packing_interval";
    public static String PACKING_AMOUNT = "packing_amount";
    public static String COINBASE_UNLOCK_HEIGHT = "coinbase_unlock_height";
    public static String RED_PUBLISH_LOCKTIME = "redPublish_lockTime";
    public static String STOP_AGENT_LOCKTIME = "stopAgent_lockTime";
    public static String COMMISSION_RATE_MIN = "commissionRate_min";
    public static String COMMISSION_RATE_MAX = "commissionRate_max";
    public static String DEPOSIT_MIN = "deposit_min";
    public static String DEPOSIT_MAX = "deposit_max";
    public static String COMMISSION_MIN = "commission_min";
    public static String COMMISSION_MAX = "commission_max";
}
