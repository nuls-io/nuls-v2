package io.nuls.transaction.constant;

import io.nuls.core.crypto.HexUtil;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TxConstant {

    String LOG_TX = "tx/txChain";
    String LOG_NEW_TX_PROCESS = "tx/newTxProcess";
    String LOG_TX_MESSAGE = "tx/message";


    String TX_CMD_PATH = "io.nuls.transaction.rpc.cmd";

    /** system params */
    String SYS_ALLOW_NULL_ARRAY_ELEMENT = "protostuff.runtime.allow_null_array_element";
    String SYS_FILE_ENCODING = "file.encoding";

    String RPC_VERSION = "1.0";

    /** 交易task, 初始延迟值(秒) */
    int TX_TASK_INITIALDELAY = 1;
    /** 网络新交易task, 运行周期间隔(秒) */
    int TX_TASK_PERIOD = 10;
    /** 孤儿交易task, 运行周期间隔(秒) */
    int TX_ORPHAN_TASK_PERIOD = 3;

    /** 新跨链交易task,初始延迟值(秒) */
    int CTX_TASK_INITIALDELAY = 5;
    /** 新跨链交易task, 运行周期间隔(秒) */
    int CTX_TASK_PERIOD = 10;

    /** 未确认交易清理机制task,初始延迟值(秒) */
    int CLEAN_TASK_INITIALDELAY = 5;
    /** 未确认交易清理机制task, 运行周期间隔(分钟) */
    int CLEAN_TASK_PERIOD = 5;

    /**
     * 参数key
     */
    String KEY_CHAIN_ID ="chainId";
    String KEY_NODE_ID="nodeId";
    String KEY_MESSAGE_BODY="messageBody";

    /**
     * 创建多签交易时，返回map的key
     */
    String MULTI_TX_HASH = "txHash";
    String MULTI_TX = "tx";

    int PAGESIZE = 20;

    int PAGENUMBER = 1;

    /** DB config */
    String DB_CONFIG_NAME = "db_config.properties";

    int PACKAGE_ORPHAN_MAXCOUNT = 5;

    /**
     * 交易hash最大长度
     */
    int TX_HASH_DIGEST_BYTE_MAX_LEN = 70;

    /**
     * 跨链交易固定为非解锁交易
     */
    byte CORSS_TX_LOCKED = 0;

    /**
     * Map初始值
     */
    int INIT_CAPACITY_32 = 32;
    int INIT_CAPACITY_16 = 16;
    int INIT_CAPACITY_8 = 8;
    int INIT_CAPACITY_4 = 4;
    int INIT_CAPACITY_2 = 2;

    int NET_NEW_TX_LIST_MAX_LENGTH = 1000000;

    int NET_TX_PROCESS_NUMBER_ONCE = 2000;

    /**
     * 跨链注册信息交易
     */
    String TX_MODULE_VALIDATOR = "txProcess";
    String CROSS_TRANSFER_VALIDATOR = "crossTxValidator";
    String CROSS_TRANSFER_COMMIT = "crossTxCommit";
    String CROSS_TRANSFER_ROLLBACK = "crossTxRollback";

    String THREAD_VERIFIY_BLOCK_TXS = "verifiyBlockTxs";
    String THREAD_CLEAR_TXS = "clearTxs";
    String THREAD_VERIFIY_NEW_TX = "verifiyNewTxs";


    byte[] DEFAULT_NONCE = HexUtil.decode("0000000000000000");
}
