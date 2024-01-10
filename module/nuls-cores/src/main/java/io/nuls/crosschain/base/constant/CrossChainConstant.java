package io.nuls.crosschain.base.constant;

/**
 * Cross chain module constant management class
 * @author tag
 * 2019/04/08
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
    String RPC_PATH = "io.nuls.crosschain.base.rpc.cmd";

    /**
     * config
     * */
    String DB_NAME_CONSUME_LANGUAGE = "language";
    String DB_NAME_CONSUME_CONGIF = "config";

    /**
     * MapInitial value
     * */
    int  INIT_CAPACITY =16;

    String CROSS_TX_VALIDATOR = "validCrossTx";
    String VALIDATOR = "crossTxBatchValid";
    String ROLLBACK = "rollbackCrossTx";
    String COMMIT = "commitCrossTx";

    String CHAIN = "chain";

    int MAGIC_NUM_100 = 100;
}
