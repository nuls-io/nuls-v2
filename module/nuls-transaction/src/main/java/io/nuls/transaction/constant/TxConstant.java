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
    /** 清理无效交易(验证未通过)线程 */
    String CLEAN_INVALID_TX_THREAD = "cleanInvalidTxThread";
//    /** 验证交易线程 */
//    String VERIFY_TX_THREAD = "verifyTxThread";
//    /** 网络新交易处理线程名称前缀 */
//    String NET_TX_THREAD_PREFIX = "netTxWorker-chain-";

    /** 新交易task, 初始延迟值(秒) */
    int TX_TASK_INITIALDELAY = 1;
    /** 新交易task, 运行周期间隔(秒) */
    int TX_TASK_PERIOD = 15;

    /** 孤儿交易处理task, 初始延迟值(秒) */
    int TX_ORPHAN_TASK_INITIALDELAY = 1;
    /** 孤儿交易处理task, 运行周期间隔(秒) */
    int TX_ORPHAN_TASK_PERIOD = 3;

    /** 未确认交易清理机制task,初始延迟值(秒) */
    int TX_CLEAN_TASK_INITIALDELAY = 5;
    /** 未确认交易清理机制task, 运行周期间隔(分钟) */
    int TX_CLEAN_TASK_PERIOD = 5;

    /** 打包时孤儿交易返回待打包队列重新处理的最大次数，超过该次数则不再处理该孤儿交易(丢弃) */
    int PACKAGE_ORPHAN_MAXCOUNT = 5;
    /** 处理网络新交易时，一次从待处理集合中获取新交易的最大值 */
    int NET_TX_PROCESS_NUMBER_ONCE = 3000;

    /** 处理网络新交易时，一次从待处理集合中获取新交易的最大值 */
    int PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS = 1500;

    /** 计算打包预留时间的临界值*/
    long PACKAGE_RESERVE_CRITICAL_TIME = 6000L;

    /** Map初始值 */
    int INIT_CAPACITY_32 = 32;
    int INIT_CAPACITY_16 = 16;
    int INIT_CAPACITY_8 = 8;
    int INIT_CAPACITY_4 = 4;
    int INIT_CAPACITY_2 = 2;

    /** nonce值初始值 */
    byte[] DEFAULT_NONCE = HexUtil.decode("0000000000000000");

    int CACHED_SIZE = 50000;

    /** 待打包队列存储交易的map 最大限制*/
    int PACKABLE_TX_MAX_SIZE = 150000;

    int PACKAGE_TX_MAX_COUNT = 10500;//12000

    long PACKAGE_MODULE_VALIDATOR_RESERVE_TIME =1800L;
}
