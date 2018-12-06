package io.nuls.transaction.constant;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TxConstant {

    String MODULE_CODE = "tx";

    int NUlS_CHAINID = 1;
    int NUlS_CHAIN_ASSETID = 1;
    int CURRENT_CHAINID = 1;
    int CURRENT_CHAIN_ASSETID = 1;

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

    /**
     * 跨链交易
     */
    int TX_TYPE_CROSS_CHAIN_TRANSFER = 10;
    String TX_MODULE_VALIDATOR = "txValidator";
    String CROSS_TRANSFER_VALIDATOR = "crossTxValidator";
    String CROSS_TRANSFER_COMMIT = "crossTxCommit";
    String CROSS_TRANSFER_ROLLBACK = "crossTxCommit";
    /**
     * 跨链交易固定为非解锁交易
     */
    byte CORSS_TX_LOCKED = 0;


}
