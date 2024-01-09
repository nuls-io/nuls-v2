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

    /** New transaction thread name*/
    String TX_THREAD = "newNetTxThread";
    /** Orphan transaction processing thread name*/
    String TX_ORPHAN_THREAD = "orphanTxThread";
    /** Unconfirmed transaction cleaning mechanism thread name */
    String TX_CLEAN_THREAD = "cleanTxThread";
    /** Verify transaction signature thread */
    String VERIFY_TX_SIGN_THREAD = "verifyTxSignThread";

    /** Orphan transaction processingtask, Initial delay value(second) */
    int TX_ORPHAN_TASK_INITIALDELAY = 1;
    /** Orphan transaction processingtask, Run cycle interval(second) */
    int TX_ORPHAN_TASK_PERIOD = 3;

    /** Unconfirmed transaction clearance mechanismtask,Initial delay value */
    int TX_CLEAN_TASK_INITIALDELAY = 10 * 60;
    /** Unconfirmed transaction clearance mechanismtask, Run cycle interval(second) */
    int TX_CLEAN_TASK_PERIOD = 3 * 60;

    /** The maximum number of times an orphan transaction can be reprocessed in the waiting queue during packaging. If this number is exceeded, the orphan transaction will no longer be processed(discard) */
    int PACKAGE_ORPHAN_MAXCOUNT = 5;
    int PACKAGE_ORPHAN_MAP_MAXCOUNT = 10000;
    /** When processing new transactions on the network, retrieve the maximum value of the new transaction from the set to be processed at once */
    int NET_TX_PROCESS_NUMBER_ONCE = 3000;

    /** The number of transactions validated for the ledger in a batch during packaging */
    int PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS = 2000;

    /** MapInitial value */
    int INIT_CAPACITY_32 = 32;
    int INIT_CAPACITY_16 = 16;
    int INIT_CAPACITY_8 = 8;
    int INIT_CAPACITY_4 = 4;
    int INIT_CAPACITY_2 = 2;

    /** nonceInitial value */
    byte[] DEFAULT_NONCE = HexUtil.decode("0000000000000000");

    int CACHED_SIZE = 50000;

    /** Store transactions in the queue to be packagedmap All transactionssize Maximum limit (B)*/
    int PACKABLE_TX_MAP_STRESS_DATA_SIZE = 150000 * 300;
    int PACKABLE_TX_MAP_HEAVY_DATA_SIZE = 200000 * 300;
    int PACKABLE_TX_MAP_MAX_DATA_SIZE = 250000 * 300;

    int ORPHAN_LIST_MAX_DATA_SIZE = 50000 * 300;

    int PACKAGE_TX_MAX_COUNT = 10000;
    /** The maximum number of allowed cross chain module transactions in a block*/
    int PACKAGE_CROSS_TX_MAX_COUNT = 500;
    /** The maximum allowed number of smart contract transactions in a block*/
    int PACKAGE_CONTRACT_TX_MAX_COUNT = 600;

    /**(millisecond) The time allocation during packaging is divided into two main parts
     1：Retrieve transactions and verify the ledger from the queue to be packaged.
     2：Call various module validators to verify transactions and obtain smart contract results.
     This configuration is fixed for the time reserved for the second part, with the remaining time reserved for the first part.
     */
    long PACKAGE_MODULE_VALIDATOR_RESERVE_TIME = 2000L;//1500L;


    long TIMEOUT = 600 * 1000L;
}
