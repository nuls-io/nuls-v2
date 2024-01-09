package io.nuls.consensus.constant;

import java.math.BigInteger;

/**
 * Consensus module constant class
 * @author tag
 * 2018/11/6
 * */
public interface ConsensusConstant {

    /**
     * Consensus module related table name/Consensus module related table names
     * */
    String DB_NAME_CONSENSUS_AGENT = "consensus_agent";
    String DB_NAME_CONSENSUS_DEPOSIT = "consensus_deposit";
    String DB_NAME_CONSENSUS_PUNISH = "consensus_punish";
    String DB_NAME_CONSUME_TX = "consensus_tx";
    String DB_NAME_CONSUME_LANGUAGE = "language";
    String DB_NAME_CONSUME_CONGIF = "config";
    String DB_NAME_RANDOM_SEEDS = "random_seed";

    byte[] EMPTY_SEED = new byte[32];

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
     * boot path
     * */
    String BOOT_PATH = "io.nuls";

    /**
     * context path
     * */
    String CONTEXT_PATH = "io.nuls.poc";

    /**
     * rpc file path
     * */
    String RPC_PATH = "io.nuls.poc.rpc";


    /**
     * system params
     * */
    String SYS_ALLOW_NULL_ARRAY_ELEMENT = "protostuff.runtime.allow_null_array_element";
    String SYS_FILE_ENCODING = "file.encoding";


    /**
     * DB config
     * */
    String DB_CONFIG_NAME ="db_config.properties";
    String DB_DATA_PATH ="rocksdb.datapath";
    String DB_DATA_DEFAULT_PATH ="rocksdb.datapath";


    /**
     * unit:round of consensus
     * Used to calculate reputation value（Indicates that only the most recent round information is used to calculate the reputation value）
     */
    int RANGE_OF_CAPACITY_COEFFICIENT = 100;

    /**
     * Penalty coefficient,greater than 4.
     */
    int CREDIT_MAGIC_NUM = 100;

    /**
     * Load the block header of the last specified number of rounds during initialization
     * Load penalty information for the most recent specified number of rounds during initialization
     */
    int INIT_PUNISH_OF_ROUND_COUNT = 400;

    /**
     * Cache blocks for specified rounds during system startup
     * Buffer a specified number of blocks at system startup
     * */
    int INIT_BLOCK_HEADER_COUNT = 110;

    /**
     * The minimum number of connected nodes for system operation
     * The number of minimum connection nodes that the system runs.
     */
    int ALIVE_MIN_NODE_COUNT = 1;

    /**
     * The same outgoing block address is consecutive3The wheel emits two identical heights, but differenthashofblockNodes will be penalized with red cards
     */
    byte REDPUNISH_BIFURCATION = 3;

    /**
     * Blank value placeholder
     * Null placeholder.
     */
    byte[] PLACE_HOLDER = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    /**
     * Consensus reward obtained for each block, total consensus reward for one year5000000Total number of blocks produced in a year3154600,Divide and receive rewards for each piece
     * value = 5000000/3154600
     */
    BigInteger YEAR_MILLISECOND = new BigInteger("31536000");

    /**
     * The minimum value of reputation value, less than or equal to this value will result in a red card penalty
     * */
    double RED_PUNISH_CREDIT_VAL = -1D;

    long CHANGE_NET_HEIGHT_MIN = 878000;

    long CHANGE_NET_HEIGHT_MAX = 900000;

    /**
     * Consensus locking time
     * */
    long CONSENSUS_LOCK_TIME = -1;

    /**
     * lock of lockTime,(max of int48)(281474976710655L)
     */
    long LOCK_OF_LOCK_TIME = -1L ;

    /**
     * MapInitial value
     * */
    int  INIT_CAPACITY =16;

    /**
     * RPCInterface parameter control
     * RPC Interface Parameter Control
     * */
    int MIN_VALUE = 0;
    int PAGE_NUMBER_INIT_VALUE = 1;
    int PAGE_SIZE_INIT_VALUE = 10;
    int PAGE_SIZE_MAX_VALUE = 300;
    String PARAM_CHAIN_ID = "chainId";
    String PARAM_ADDRESS = "address";
    String PARAM_TX = "tx";
    String PARAM_TX_HEX_LIST = "txList";
    String PARAM_HEIGHT = "height";
    String PARAM_BLOCK_HEADER ="blockHeader";
    String PARAM_EXTEND="extend";
    String PARAM_BLOCK_HEADER_HEX ="blockHeader";
    String PARAM_BLOCK="block";
    String PARAM_EVIDENCE_HEADER ="evidenceHeader";
    String VALID_RESULT ="valid";
    String PARAM_RESULT_VALUE ="value";
    String PARAM_STATUS = "status";
    String HEADER_LIST = "headerList";
    String STATE_ROOT = "stateRoot";
    String LAST_ROUND = "lastRound";
    String CURRENT_ROUND = "currentRound";
    String PUB_KEY = "pubKey";

    /**
     * Consensus module log management
     * Consensus module log management
     * */
    String CONSENSUS_LOGGER_NAME = "consensus/consensus";
    String BASIC_LOGGER_NAME = "consensus/rpc";
    String COMMON_LOG_NAME = "common";

    String CHAIN ="chain";

    String RPC_VERSION = "1.0";

    String MODULE_NAME = "cs";

    int ROUND_CACHE_COUNT = 10;

    int RPC_CALL_TRY_COUNT = 5;

    byte VALUE_OF_ONE_HUNDRED =100;

    String SEPARATOR = "_";
 }
