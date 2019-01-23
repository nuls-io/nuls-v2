package io.nuls.transaction.constant;

import io.nuls.tools.constant.ErrorCode;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TxErrorCode {

    ErrorCode SUCCESS = ErrorCode.init("10000");
    ErrorCode FAILED = ErrorCode.init("10001");
    ErrorCode SYS_UNKOWN_EXCEPTION = ErrorCode.init("10002");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("10003");
    ErrorCode THREAD_REPETITION = ErrorCode.init("10004");
    ErrorCode LANGUAGE_CANNOT_SET_NULL = ErrorCode.init("10005");
    ErrorCode IO_ERROR = ErrorCode.init("10006");
    ErrorCode DATA_SIZE_ERROR = ErrorCode.init("10007");
    ErrorCode CONFIG_ERROR = ErrorCode.init("10008");
    ErrorCode SIGNATURE_ERROR = ErrorCode.init("10009");
    ErrorCode REQUEST_DENIED = ErrorCode.init("10010");
    ErrorCode DATA_SIZE_ERROR_EXTEND = ErrorCode.init("10011");
    ErrorCode PARAMETER_ERROR = ErrorCode.init("10012");
    ErrorCode NULL_PARAMETER = ErrorCode.init("10013");
    ErrorCode DATA_ERROR = ErrorCode.init("10014");
    ErrorCode DATA_NOT_FOUND = ErrorCode.init("10015");
    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init("10016");
    ErrorCode PARSE_JSON_FAILD = ErrorCode.init("10017");
    ErrorCode FILE_OPERATION_FAILD = ErrorCode.init("10018");
    ErrorCode ILLEGAL_ACCESS_EXCEPTION = ErrorCode.init("10019");
    ErrorCode INSTANTIATION_EXCEPTION = ErrorCode.init("10020");
    ErrorCode UPGRADING = ErrorCode.init("10021");
    ErrorCode NOT_UPGRADING = ErrorCode.init("10022");
    ErrorCode VERSION_NOT_NEWEST = ErrorCode.init("10023");
    ErrorCode SERIALIZE_ERROR = ErrorCode.init("10024");
    ErrorCode DESERIALIZE_ERROR = ErrorCode.init("10025");
    ErrorCode HASH_ERROR = ErrorCode.init("10026");
    ErrorCode INSUFFICIENT_BALANCE = ErrorCode.init("10027");
    ErrorCode ADDRESS_IS_BLOCK_HOLE = ErrorCode.init("10028");
    ErrorCode ADDRESS_IS_NOT_THE_CURRENT_CHAIN = ErrorCode.init("10029");
    ErrorCode VALIDATORS_NOT_FULLY_EXECUTED = ErrorCode.init("10030");
    ErrorCode BLOCK_IS_NULL = ErrorCode.init("10031");
    ErrorCode VERSION_TOO_LOW = ErrorCode.init("10032");
    ErrorCode PUBKEY_REPEAT = ErrorCode.init("10033");
    ErrorCode COIN_OWNER_ERROR = ErrorCode.init("10034");
    ErrorCode NONEWVER = ErrorCode.init("10035");
    ErrorCode INSUFFICIENT_FEE = ErrorCode.init("10036");
    ErrorCode ASSET_NOT_EXIST = ErrorCode.init("10037");
    ErrorCode CROSS_TX_PAYER_CHAINID_MISMATCH = ErrorCode.init("10038");
    ErrorCode COINFROM_NOT_FOUND = ErrorCode.init("10039");
    ErrorCode COINTO_NOT_FOUND = ErrorCode.init("10040");
    ErrorCode IS_MULTI_SIGNATURE_ADDRESS = ErrorCode.init("10041");
    ErrorCode IS_NOT_MULTI_SIGNATURE_ADDRESS = ErrorCode.init("10042");
    ErrorCode ONLY_ONE_MULTI_SIGNATURE_ADDRESS_ALLOWED = ErrorCode.init("10043");
    ErrorCode CROSS_TX_PAYEE_CHAINID_NOT_SAME = ErrorCode.init("10044");
    ErrorCode COINDATA_IS_INCOMPLETE = ErrorCode.init("10045");
    ErrorCode PAYEE_AND_PAYER_IS_THE_SAME_CHAIN = ErrorCode.init("10046");
    ErrorCode TRANSACTION_ALREADY_EXISTS = ErrorCode.init("10047");
    ErrorCode ADDRESS_NOT_BELONG_TO_MULTI_SIGN_ACCOUNT = ErrorCode.init("10048");
    ErrorCode DESERIALIZE_TX_ERROR = ErrorCode.init("10049");
    ErrorCode DESERIALIZE_COINDATA_ERROR = ErrorCode.init("10050");
    ErrorCode CHAIN_NOT_FOUND = ErrorCode.init("10051");
    ErrorCode CHAINID_ERROR = ErrorCode.init("10052");
    ErrorCode ASSETID_ERROR = ErrorCode.init("10053");
    ErrorCode TX_COMMIT_FAIL = ErrorCode.init("10054");
    ErrorCode TX_ROLLBACK_FAIL = ErrorCode.init("10055");
    ErrorCode TX_VERIFY_FAIL = ErrorCode.init("10056");
    ErrorCode COINDATA_VERIFY_FAIL = ErrorCode.init("10057");
    ErrorCode COINFROM_HAS_DUPLICATE_COIN = ErrorCode.init("10058");
    ErrorCode COINTO_HAS_DUPLICATE_COIN = ErrorCode.init("10059");
    ErrorCode CALLING_REMOTE_INTERFACE_FAILED = ErrorCode.init("10060");
    ErrorCode SIGN_ADDRESS_NOT_MATCH_COINFROM = ErrorCode.init("10061");




    ErrorCode DB_TABLE_EXIST = ErrorCode.init("20009");
    ErrorCode DB_TABLE_NOT_EXIST = ErrorCode.init("20010");
    ErrorCode DB_TABLE_CREATE_ERROR = ErrorCode.init("20011");
    ErrorCode DB_SAVE_BATCH_ERROR = ErrorCode.init("20012");
    ErrorCode DB_SAVE_ERROR = ErrorCode.init("20013");
    ErrorCode DB_UPDATE_ERROR = ErrorCode.init("20014");
    ErrorCode DB_QUERY_ERROR = ErrorCode.init("20015");
    ErrorCode DB_DELETE_ERROR = ErrorCode.init("20016");

    ErrorCode UTXO_UNUSABLE = ErrorCode.init("31001");
    ErrorCode UTXO_STATUS_CHANGE = ErrorCode.init("31002");
    ErrorCode INVALID_INPUT = ErrorCode.init("31004");
    ErrorCode INVALID_AMOUNT = ErrorCode.init("31005");
    ErrorCode ORPHAN_TX = ErrorCode.init("31006");
    ErrorCode ORPHAN_BLOCK = ErrorCode.init("31007");
    ErrorCode TX_DATA_VALIDATION_ERROR = ErrorCode.init("31008");
    ErrorCode FEE_NOT_RIGHT = ErrorCode.init("31009");
    ErrorCode ROLLBACK_TRANSACTION_FAILED = ErrorCode.init("31010");
    ErrorCode TRANSACTION_REPEATED = ErrorCode.init("31011");
    ErrorCode TOO_SMALL_AMOUNT = ErrorCode.init("31012");
    ErrorCode TX_SIZE_TOO_LARGE = ErrorCode.init("31013");
    ErrorCode SAVE_TX_ERROR = ErrorCode.init("31014");
    ErrorCode TX_NOT_EXIST = ErrorCode.init("31015");
    ErrorCode COINDATA_NOT_FOUND = ErrorCode.init("31016");
    ErrorCode TX_TYPE_ERROR = ErrorCode.init("31017");
    ErrorCode TX_NOT_EFFECTIVE = ErrorCode.init("31018");
    ErrorCode TX_NOT_EFFECTIVE_HEIGHT = ErrorCode.init("31019");
}
