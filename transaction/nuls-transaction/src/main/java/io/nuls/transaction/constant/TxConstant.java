package io.nuls.transaction.constant;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TxConstant {


    /** coinbase交易*/
    int TX_TYPE_COINBASE = 1;
    /** 转账交易*/
    int TX_TYPE_TRANSFER = 2;
    /** 设置别名*/
    int TX_TYPE_ALIAS = 3;
    /** 创建共识节点交易*/
    int TX_TYPE_REGISTER_AGENT = 4;
    /** 委托交易(加入共识)*/
    int TX_TYPE_JOIN_CONSENSUS = 5;
    /** 取消委托交易(退出共识)*/
    int TX_TYPE_CANCEL_DEPOSIT = 6;
    /** 黄牌惩罚*/
    int TX_TYPE_YELLOW_PUNISH = 7;
    /** 红牌惩罚*/
    int TX_TYPE_RED_PUNISH = 8;
    /** 停止节点(删除共识节点)*/
    int TX_TYPE_STOP_AGENT = 9;
    /** 跨链转账交易*/
    int TX_TYPE_CROSS_CHAIN_TRANSFER = 10;
    /** 注册链交易*/
    int TX_TYPE_REGISTER_CHAIN_AND_ASSET = 11;
    /** 销毁链*/
    int TX_TYPE_DESTROY_CHAIN_AND_ASSET = 12;
    /** 为链新增一种资产*/
    int TX_TYPE_ADD_ASSET_TO_CHAIN = 13;
    /** 删除链上资产*/
    int TX_TYPE_REMOVE_ASSET_FROM_CHAIN = 14;
    /** 创建智能合约交易*/
    int TX_TYPE_CREATE_CONTRACT = 15;
    /** 调用智能合约交易*/
    int TX_TYPE_CALL_CONTRACT = 16;
    /** 删除智能合约交易*/
    int TX_TYPE_DELETE_CONTRACT = 17;
    /** contract transfer tx */
    int TX_TYPE_CONTRACT_TRANSFER = 18;
    /** contract return gas tx */
    int TX_TYPE_CONTRACT_RETURN_GAS = 19;

    /** 获取网络时间间隔*/
    long GETTIME_INTERVAL = 30000L;

    long GETTIME_INTERFACE_TIMEOUT = 200L;


    String LOG_TX = "tx/txChain";
    String LOG_NEW_TX_PROCESS = "tx/newTxProcess";
    String LOG_TX_MESSAGE = "tx/message";


    String TX_CMD_PATH = "io.nuls.transaction.rpc.cmd";

    /** system params */
    String SYS_ALLOW_NULL_ARRAY_ELEMENT = "protostuff.runtime.allow_null_array_element";
    String SYS_FILE_ENCODING = "file.encoding";

    String RPC_VERSION = "1.0";

    /** 新本地交易task,初始延迟值(秒) */
    int TX_TASK_INITIALDELAY = 5;
    /** 新本地交易task, 运行周期间隔(秒) */
    int TX_TASK_PERIOD = 3;

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

    /** 接收新交易的文件队列名**/
    String TX_UNVERIFIED_QUEUE_PREFIX = "tx_unverified_queue_";

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
    int INIT_CAPACITY_16 = 16;
    int INIT_CAPACITY_8 = 8;
    int INIT_CAPACITY_4 = 4;
    int INIT_CAPACITY_2 = 2;

    /**
     * 跨链注册信息交易
     */
    String TX_MODULE_VALIDATOR = "txProcess";
    String CROSS_TRANSFER_VALIDATOR = "crossTxValidator";
    String CROSS_TRANSFER_COMMIT = "crossTxCommit";
    String CROSS_TRANSFER_ROLLBACK = "crossTxRollback";

    String THREAD_VERIFIY_BLOCK_TXS = "verifiyBlockTxs";
    String THREAD_CLEAR_TXS = "clearTxs";
}
