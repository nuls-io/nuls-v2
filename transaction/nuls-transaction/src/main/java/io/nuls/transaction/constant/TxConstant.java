package io.nuls.transaction.constant;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TxConstant {

    String MODULE_CODE = "tx";

    int NULS_CHAINID = 12345;
    int NULS_CHAIN_ASSETID = 1;

    /**
     * Map初始值
     */
    int INIT_CAPACITY = 16;

    int PAGESIZE = 20;

    /**
     * context path
     */
    String CONTEXT_PATH = "io.nuls.transaction";

    /**
     * webSocket config
     */
    String TX_MODULE_NAME = "transaction";

    String TX_CMD_PATH = "io.nuls.transaction.rpc.cmd";
    int TX_CMD_PORT = 8823;
    String KERNEL_URL = "ws://127.0.0.1:8887";

    /**
     * system params
     */
    String SYS_ALLOW_NULL_ARRAY_ELEMENT = "protostuff.runtime.allow_null_array_element";
    String SYS_FILE_ENCODING = "file.encoding";

    /**
     * DB config
     */
    String DB_CONFIG_NAME = "db_config.properties";
    String DB_DATA_PATH = "rocksdb.datapath";
    String TX_UNVERIFIED_QUEUE = "tx_unverified_queue";
    long TX_UNVERIFIED_QUEUE_MAXSIZE = 10000000L;


    /**
     * H2
     */
    String H2_TX_TABLE_NAME_PREFIX = "transaction_";
    String H2_TX_TABLE_INDEX_NAME_PREFIX = "tx_index_";
    String H2_TX_TABLE_UNIQUE_NAME_PREFIX = "tx_unique_";
    int H2_TX_TABLE_NUMBER = 128;


    int ORPHAN_CONTAINER_MAX_SIZE = 200000;


    int TX_TYPE_COINBASE = 1;
    int TX_TYPE_TRANSFER = 2;

    int TX_TYPE_REDPUNISH = 8;

    /**
     * 跨链交易
     */
    int TX_TYPE_CROSS_CHAIN_TRANSFER = 10;
    String TX_MODULE_VALIDATOR = "txProcess";
    String CROSS_TRANSFER_VALIDATOR = "crossTxValidator";
    String CROSS_TRANSFER_COMMIT = "crossTxCommit";
    String CROSS_TRANSFER_ROLLBACK = "crossTxCommit";

    /**
     * 跨链交易打包确认后需要达到的最低阈值高度才生效
     */
    long CTX_EFFECT_THRESHOLD = 30;

    /**
     * 跨链交易验证过程
     */
    /** 已发送请求跨链验证消息 */
    int CTX_UNPROCESSED_0 = 0;
    /** 已发送请求跨链验证消息 */
    int CTX_VERIFY_REQUEST_1 = 1;
    /** 已接收到跨链验证结果 广播本节点验证结果给本链其他节点进行统计 */
    int CTX_VERIFY_RESULT_2 = 2;
    /** 接收到其他节点发送的验证结果，并已统计出结果，放入待打包 */
    int CTX_NODE_STATISTICS_RESULT_3 = 3;
    /** 已进入区块并确认 */
    int CTX_COMFIRM_4 = 4;

    /**
     * 跨链交易固定为非解锁交易
     */
    byte CORSS_TX_LOCKED = 0;

    /**
     * 交易基础信息
     */
    int TX_HASH_DIGEST_BYTE_MAX_LEN = 70;
    int TX_MAX_BYTES = 300;
    int TX_MAX_SIZE = TX_MAX_BYTES * 1024;

    /**
     * 创建多签交易时，返回map的key
     */
    String MULTI_TX_HASH = "txHash";
    String MULTI_TX_HEX = "txHex";

    /**
     * config file path
     */
    String CONFIG_FILE_PATH = "transaction-config.json";

    /**
     * 打包交易，预留模块统一验证的时间 毫秒
     */
    long VERIFY_OFFSET = 500L;

    /**
     * 参数key
     */
    String KEY_CHAIN_ID ="chainId";
    String KEY_NODE_ID="nodeId";
    String KEY_MESSAGE_BODY="messageBody";

    /** 跨链验证通过率百分比, 跨链通过率 */
    String CROSS_VERIFY_RESULT_PASS_RATE = "0.51";

    /** 链内通过率 */
    String CHAIN_NODES_RESULT_PASS_RATE = "0.8";

    /** 友链链内最近N个出块者阈值*/
    int RECENT_PACKAGER_THRESHOLD = 30;
}
