package io.nuls.transaction.constant;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TransactionConstant {

    /**
     * context path
     */
    String CONTEXT_PATH = "io.nuls.transaction";

    /**
     * webSocket config
     * */
    String TX_MODULE_NAME = "transaction";
    String TX_CMD_PATH = "io.nuls.transaction.cmd";
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
    String DB_CONFIG_NAME ="db_config.properties";
    String DB_DATA_PATH ="rocksdb.datapath";
    String DB_DATA_DEFAULT_PATH ="rocksdb.datapath";

    /**
     * Transaction module related table name
     * 交易管理模块相关表名
     */
    String DB_NAME_CONSUME_LANGUAGE = "language";
}
