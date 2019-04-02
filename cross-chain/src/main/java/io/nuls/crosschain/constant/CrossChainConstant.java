package io.nuls.crosschain.constant;

/**
 * 跨链模块常量管理类
 * @author tag
 * 2019/04/01
 */
public interface CrossChainConstant {
    /**
     * cross chain module transaction type
     * */
    int TX_TYPE_CROSS_CHAIN = 10;

    /**
     * boot path
     */
    String BOOT_PATH = "io.nuls";

    /**
     * context path
     */
    String CONTEXT_PATH = "io.nuls.crosschain";

    /**
     * rpc file path
     */
    String RPC_PATH = "io.nuls.crosschain.rpc.cmd";

    /**
     * config
     * */
    String SYS_FILE_ENCODING = "file.encoding";
    String DB_CONFIG_NAME ="db_config.properties";
    String DB_DATA_PATH ="rocksdb.datapath";
    String DB_DATA_DEFAULT_PATH ="rocksdb.datapath";
    String DB_NAME_CONSUME_LANGUAGE = "language";
    String DB_NAME_CONSUME_CONGIF = "config";

    /**
     * Map初始值
     * */
    int  INIT_CAPACITY =16;
}
