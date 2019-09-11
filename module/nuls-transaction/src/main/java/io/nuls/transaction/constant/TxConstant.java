package io.nuls.transaction.constant;

import io.nuls.core.crypto.HexUtil;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TxConstant {

    String TX_CMD_PATH = "io.nuls.transaction.rpc.cmd";

    /** system params */
    String SYS_ALLOW_NULL_ARRAY_ELEMENT = "protostuff.runtime.allow_null_array_element";
    String SYS_FILE_ENCODING = "file.encoding";

    String RPC_VERSION = "1.0";

    /** 新交易线程名称*/
    String TX_THREAD = "newNetTxThread";
    /** 孤儿交易处理线程名称*/
    String TX_ORPHAN_THREAD = "orphanTxThread";
    /** 未确认交易清理机制线程名称 */
    String TX_CLEAN_THREAD = "cleanTxThread";
    /** 验证交易签名线程 */
    String VERIFY_TX_SIGN_THREAD = "verifyTxSignThread";

    /** 孤儿交易处理task, 初始延迟值(秒) */
    int TX_ORPHAN_TASK_INITIALDELAY = 1;
    /** 孤儿交易处理task, 运行周期间隔(秒) */
    int TX_ORPHAN_TASK_PERIOD = 3;

    /** 未确认交易清理机制task,初始延迟值 */
    int TX_CLEAN_TASK_INITIALDELAY = 10 * 60;
    /** 未确认交易清理机制task, 运行周期间隔(秒) */
    int TX_CLEAN_TASK_PERIOD = 3 * 60;

    /** 打包时孤儿交易返回待打包队列重新处理的最大次数，超过该次数则不再处理该孤儿交易(丢弃) */
    int PACKAGE_ORPHAN_MAXCOUNT = 5;
    int PACKAGE_ORPHAN_MAP_MAXCOUNT = 10000;
    /** 处理网络新交易时，一次从待处理集合中获取新交易的最大值 */
    int NET_TX_PROCESS_NUMBER_ONCE = 3000;

    /** 打包时，一批次给账本进行验证的交易数 */
    int PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS = 2000;

    /** Map初始值 */
    int INIT_CAPACITY_32 = 32;
    int INIT_CAPACITY_16 = 16;
    int INIT_CAPACITY_8 = 8;
    int INIT_CAPACITY_4 = 4;
    int INIT_CAPACITY_2 = 2;

    /** nonce值初始值 */
    byte[] DEFAULT_NONCE = HexUtil.decode("0000000000000000");

    int CACHED_SIZE = 50000;

    /** 待打包队列存储交易的map 所有交易size 最大限制 (B)*/
    int PACKABLE_TX_MAP_STRESS_DATA_SIZE = 150000 * 300;
    int PACKABLE_TX_MAP_HEAVY_DATA_SIZE = 200000 * 300;
    int PACKABLE_TX_MAP_MAX_DATA_SIZE = 250000 * 300;

    int ORPHAN_LIST_MAX_DATA_SIZE = 50000 * 300;

    int PACKAGE_TX_MAX_COUNT = 10000;
    /** 一个区块中最大允许跨链模块交易的数量*/
    int PACKAGE_CROSS_TX_MAX_COUNT = 500;
    /** 一个区块中最大允许智能合约交易的数量*/
    int PACKAGE_CONTRACT_TX_MAX_COUNT = 600;

    /**(毫秒) 打包时的时间分配分为两大部分
     1：从待打包队列获取交易以及账本验证。
     2：调用各模块验证器验证交易，获取智能合约结果。
     该配置为固定给第二部分预留的时间，其他时间留给第一部分。
     */
    long PACKAGE_MODULE_VALIDATOR_RESERVE_TIME = 2000L;//1500L;


    long TIMEOUT = 600 * 1000L;
}
