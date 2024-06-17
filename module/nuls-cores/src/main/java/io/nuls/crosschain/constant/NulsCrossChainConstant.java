package io.nuls.crosschain.constant;

/**
 * Cross chain module constant management class
 * @author tag
 * 2019/04/08
 */
public interface NulsCrossChainConstant {
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
     * The database indicates that
     * */
    String DB_NAME_CONSUME_LANGUAGE = "language";
    String DB_NAME_CONSUME_CONGIF = "config";
    /**Cross chain transaction processing status*/
    String DB_NAME_CTX_STATE ="ctx_state";
    /**Newly created and verified transactions*/
    String DB_NAME_CTX_STATUS = "new_ctx_status";
    /**protocol conversionHASHCorresponding table*/
    String DB_NAME_CONVERT_CTX = "convert_ctx";
    /**protocol conversionHASHCorresponding table*/
    String DB_NAME_CONVERT_HASH_CTX = "convert_hash_ctx";
    /**Cross chain transactions that have been submitted and have been approved by Byzantium within the chain but have not yet been broadcasted to other chains*/
    String DB_NAME_OTHER_COMMITED_CTX = "commit_other_ctx";
    /**List of cross chain transactions to be sent at a specified height*/
    String DB_NAME_SEND_HEIGHT = "send_height";
    /**Broadcast transaction height*/
    String DB_NAME_SENDED_HEIGHT = "sended_height";
    /**Registered cross chain chain list*/
    String DB_NAME_REGISTERED_CHAIN ="registered_chain";
    /**Verify the chain information of failed transaction broadcast due to changes in verifier*/
    String DB_NAME_BROAD_FAILED ="verifier_broad_fail";
    /**Verify the chain information of failed transaction broadcast due to changes in verifier*/
    String DB_NAME_CROSS_CHANGE_FAILED ="cross_change_broad_fail";
    /**Local Verifier Information Table*/
    String DB_NAME_LOCAL_VERIFIER ="local_verifier";

    /**After resetting the validator list of this chain for transactions, the list of validators before the change will be stored in this table*/
    String DB_NAME_OLD_LOCAL_VERIFIER ="old_local_verifier";



    /**
     * config
     * */
    String SYS_FILE_ENCODING = "file.encoding";

    /**
     * MapInitial value
     * */
    int  INIT_CAPACITY =16;

    /**
     * log name
     * */
    String COMMON_LOG_NAME = "common";

    int CHAIN_ID_MIN = 0;

    /**
     * Non locked transactions
     */
    byte UNLOCKED_TX = (byte) 0;

    /**
     * Cross chain transactions are fixed as non unlocked transactions
     */
    byte CORSS_TX_LOCKED = 0;

    int INIT_CAPACITY_8 = 8;

    int INIT_CAPACITY_16 = 16;

    String RPC_VERSION = "1.0";

    String VERSION = "1.0";

    byte[] CROSS_TOKEN_NONCE = new byte[]{0,0,0,0,0,0,0,0};

    /**
     * Byzantine timeout
     */
    long BYZANTINE_TIMEOUT = 10 * 1000L;

    int BYZANTINE_TRY_COUNT = 5;

    int FAULT_TOLERANT_RATIO = 10;

    int VERIFIER_CANCEL_MAX_RATE =30;

    int MAGIC_NUM_100 =100;

    long RPC_TIME_OUT = 5 * 1000L;

    int CTX_STAGE_WAIT_RECEIVE = 1;
    Integer CTX_STATE_PROCESSING = 2;


    int NODE_TYPE_CURRENT_CHAIN = 1;
    int NODE_TYPE_OTHER_CHAIN = 2;


    /**
     * cmd
     * Query the registered cross chain transaction for this case
     * */
    String VERIFIER_SPLIT = ",";

    String STRING_SPLIT = "_";
}
