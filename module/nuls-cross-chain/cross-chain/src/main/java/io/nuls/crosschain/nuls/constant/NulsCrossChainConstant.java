package io.nuls.crosschain.nuls.constant;

/**
 * 跨链模块常量管理类
 * @author tag
 * 2019/04/08
 */
public interface NulsCrossChainConstant {
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
    String RPC_PATH = "io.nuls.crosschain.nuls.rpc.cmd";

    /**
     * 数据库表明
     * */
    String DB_NAME_CONSUME_LANGUAGE = "language";
    String DB_NAME_CONSUME_CONGIF = "config";
    /**新创建和验证通过的交易*/
    String DB_NAME_NEW_CTX = "new_ctx";
    /**已提交但是还未广播给其他链的跨链交易*/
    String DB_NAME_COMMITED_CTX = "commit_ctx";
    /**已广播给其他链的跨链交易*/
    String DB_NAME_COMPLETED_CTX = "completed_ctx";
    /**指定高度需发送的跨链交易列表*/
    String DB_NAME_SEND_HEIGHT = "send_height";
    /**接收到的其他链发起的交易*/
    String DB_NAME_CONVERT_TO_CTX = "convert_to_ctx";
    /**本链发起广播给其他链的交易的交易*/
    String DB_NAME_CONVERT_FROM_CTX = "convert_from_ctx";
    /**跨链交易处理状态*/
    String DB_NAME_CTX_STATE ="ctx_state";


    /**
     * config
     * */
    String SYS_FILE_ENCODING = "file.encoding";

    /**
     * Map初始值
     * */
    int  INIT_CAPACITY =16;

    /**
     * log name
     * */
    String COMMON_LOG_NAME = "common";

    int CHAIN_ID_MIN = 1;

    /**
     * 跨链交易固定为非解锁交易
     */
    byte CORSS_TX_LOCKED = 0;

    int INIT_CAPACITY_8 = 8;

    int INIT_CAPACITY_16 = 16;

    String RPC_VERSION = "1.0";

    String VERSION = "1.0";

    /**
     * 拜占庭超时时间
     */
    long BYZANTINE_TIMEOUT = 10 * 1000L;

    int BYZANTINE_TRY_COUNT = 5;

    int MAGIC_NUM_100 =100;

    long RPC_TIME_OUT = 5 * 1000L;

    int CTX_STAGE_WAIT_RECEIVE = 1;
    Integer CTX_STATE_PROCESSING = 2;


    int NODE_TYPE_CURRENT_CHAIN = 1;
    int NODE_TYPE_OTHER_CHAIN = 2;


    /**
     * cmd
     * 查询已注册跨链交易此案次
     * */
    String GET_REGISTERED_CHAIN_MESSAGE = "getChains";
}
