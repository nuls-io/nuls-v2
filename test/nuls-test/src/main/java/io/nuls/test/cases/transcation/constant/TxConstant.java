package io.nuls.test.cases.transcation.constant;

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

    /** New transaction thread name*/
    String TX_THREAD = "newNetTxThread";
    /** Orphan transaction processing thread name*/
    String TX_ORPHAN_THREAD = "orphanTxThread";
    /** Unconfirmed transaction cleaning mechanism thread name */
    String TX_CLEAN_THREAD = "cleanTxThread";
    /** Verify transaction signature thread */
    String VERIFY_TX_SIGN_THREAD = "verifyTxSignThread";
    /** Clean up invalid transactions(Verification failed)thread */
    String CLEAN_INVALID_TX_THREAD = "cleanInvalidTxThread";
//    /** Verify transaction thread */
//    String VERIFY_TX_THREAD = "verifyTxThread";
//    /** Network New Transaction Processing Thread Name Prefix */
//    String NET_TX_THREAD_PREFIX = "netTxWorker-chain-";

    /** New transactionstask, Initial delay value(second) */
    int TX_TASK_INITIALDELAY = 1;
    /** New transactionstask, Run cycle interval(second) */
    int TX_TASK_PERIOD = 15;

    /** Orphan transaction processingtask, Initial delay value(second) */
    int TX_ORPHAN_TASK_INITIALDELAY = 1;
    /** Orphan transaction processingtask, Run cycle interval(second) */
    int TX_ORPHAN_TASK_PERIOD = 3;

    /** Unconfirmed transaction clearance mechanismtask,Initial delay value(second) */
    int TX_CLEAN_TASK_INITIALDELAY = 5;
    /** Unconfirmed transaction clearance mechanismtask, Run cycle interval(minute) */
    int TX_CLEAN_TASK_PERIOD = 5;

    /** The maximum number of times an orphan transaction can be reprocessed in the waiting queue during packaging. If this number is exceeded, the orphan transaction will no longer be processed(discard) */
    int PACKAGE_ORPHAN_MAXCOUNT = 5;
    /** When processing new transactions on the network, retrieve the maximum value of the new transaction from the set to be processed at once */
    int NET_TX_PROCESS_NUMBER_ONCE = 3000;

    /** When processing new transactions on the network, retrieve the maximum value of the new transaction from the set to be processed at once */
    int PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS = 2000;

    /** Calculate the critical value of packaging reservation time*/
    long PACKAGE_RESERVE_CRITICAL_TIME = 6000L;

    /** MapInitial value */
    int INIT_CAPACITY_32 = 32;
    int INIT_CAPACITY_16 = 16;
    int INIT_CAPACITY_8 = 8;
    int INIT_CAPACITY_4 = 4;
    int INIT_CAPACITY_2 = 2;

    /** nonceInitial value */
    byte[] DEFAULT_NONCE = HexUtil.decode("0000000000000000");

    int CACHED_SIZE = 50000;

    /** Store transactions in the queue to be packagedmap Maximum limit*/
    int PACKABLE_TX_MAX_SIZE = 400000;

    int PACKAGE_TX_MAX_COUNT = 12000;//12000

    long PACKAGE_MODULE_VALIDATOR_RESERVE_TIME = 800L;
}
