package io.nuls.poc.constant;

/**
 * @author tag
 * 2018/11/6
 * */
public interface ConsensusConstant {

    /**
     * consensus transaction types
     * */
    int TX_TYPE_REGISTER_AGENT = 4;
    int TX_TYPE_JOIN_CONSENSUS = 5;
    int TX_TYPE_CANCEL_DEPOSIT = 6;
    int TX_TYPE_YELLOW_PUNISH = 7;
    int TX_TYPE_RED_PUNISH = 8;
    int TX_TYPE_STOP_AGENT = 9;
    int TX_TYPE_COINBASE = 1;

    /**
     * Consensus module related table name/共识模块相关表名
     * */
    String DB_NAME_CONSENSUS_AGENT = "consensus_agent";
    String DB_NAME_CONSENSUS_DEPOSIT = "consensus_deposit";
    String DB_NAME_CONSENSUS_PUNISH = "consensus_punish";
    String DB_NAME_CONSUME_TX = "consensus_tx";
    String DB_NAME_CONSUME_LANGUAGE = "language";
    String DB_NAME_CONSUME_CONGIF = "config";

    /**
     * config param
     * */
    String PARAM_PACKING_INTERVAL = "packing_interval";
    String PARAM_BLOCK_SIZE = "block_size";
    String PARAM_PACKING_AMOUNT = "packing_amount";
    String PARAM_COINBASE_UNLOCK_HEIGHT = "coinbase_unlock_height";
    String PARAM_RED_PUBLISH_LOCKTIME = "redPublish_lockTime";
    String PARAM_STOP_AGENT_LOCKTIME = "stopAgent_lockTime";
    String PARAM_COMMISSION_RATE_MIN = "commissionRate_min";
    String PARAM_COMMISSION_RATE_MAX = "commissionRate_max";
    String PARAM_DEPOSIT_MIN = "deposit_min";
    String PARAM_DEPOSIT_MAX = "deposit_max";
    String PARAM_COMMISSION_MIN = "commission_min";
    String PARAM_COMMISSION_MAX = "commission_max";
    String PARAM_SEED_NODES = "seed_nodes";
    String PARAM_PARTAKE_PACKING = "partake_packing";

    /**
     * context path
     * */
    String CONTEXT_PATH = "io.nuls.poc";

    /**
     * config file path
     * */
    String CONFIG_FILE_PATH = "consensus-config.json";

    /**
     * system params
     * */
    String SYS_ALLOW_NULL_ARRAY_ELEMENT = "protostuff.runtime.allow_null_array_element";
    String SYS_FILE_ENCODING = "file.encoding";

    /**
     * webSocket config
     * */
    String CONSENSUS_MODULE_NAME = "consensus";
    String CONSENSUS_RPC_PATH = "io.nuls.poc.rpc";
    int CONSENSUS_RPC_PORT = 8888;
    String KERNEL_URL = "ws://127.0.0.1:8887";

    /**
     * DB config
     * */
    String DB_CONFIG_NAME ="db_config.properties";
    String DB_DATA_PATH ="rocksdb.datapath";
    String DB_DATA_DEFAULT_PATH ="rocksdb.datapath";

    /**
     * RPC_VERSION
     */
    public static final double RPC_VERSION = 1.0;

    /**
     * Regularly clear the round before the specified number of rounds of the main chain
     * 定期清理主链指定轮数之前的轮次信息
     */
    int CLEAR_MASTER_CHAIN_ROUND_COUNT = 5;

    /**
     * Maximum acceptable number of delegate
     */
    int MAX_ACCEPT_NUM_OF_DEPOSIT = 250;
    int MAX_AGENT_COUNT_OF_ADRRESS = 1;

    Na SUM_OF_DEPOSIT_OF_AGENT_LOWER_LIMIT = Na.parseNuls(200000);
    Na SUM_OF_DEPOSIT_OF_AGENT_UPPER_LIMIT = Na.parseNuls(500000);

    /**
     * unit:round of consensus
     * 用于计算信誉值（表示只用最近这么多轮的轮次信息来计算信誉值）
     */
    int RANGE_OF_CAPACITY_COEFFICIENT = 100;

    /**
     * Penalty coefficient,greater than 4.
     */
    int CREDIT_MAGIC_NUM = 100;

    /**
     * Load the block header of the last specified number of rounds during initialization
     * 初始化时加载最近指定轮数的惩罚信息
     */
    int INIT_PUNISH_OF_ROUND_COUNT = 200;

    /**
     * 系统运行的最小连接节点数量
     * The number of minimum connection nodes that the system runs.
     */
    int ALIVE_MIN_NODE_COUNT = 1;

    /**
     * 同一个出块地址连续3轮发出两个相同高度，但不同hash的block，节点将会被红牌惩罚
     */
    byte REDPUNISH_BIFURCATION = 3;

    /**
     * 空值占位符
     * Null placeholder.
     */
    byte[] PLACE_HOLDER = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    /**
     * 每出一个块获得的共识奖励，一年总的共识奖励金5000000，一年总出块数3154600,相除得到每一块的奖励金
     * value = 5000000/3154600
     */
    Na BLOCK_REWARD = Na.valueOf(158548960);

    /**
     * 信誉值的最小值，小于等于该值会给红牌处罚
     * */
    double RED_PUNISH_CREDIT_VAL = -1D;

    /**
     * 共识锁定时间
     * */
    long CONSENSUS_LOCK_TIME = -1;

    /**
     * lock of lockTime,(max of int48)(281474976710655L)
     */
    long LOCK_OF_LOCK_TIME = -1L ;
}
