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
    ErrorCode DOWNLOAD_VERSION_FAILD = ErrorCode.init("10016");
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




    ErrorCode DB_TABLE_EXIST = ErrorCode.init("20009");
    ErrorCode DB_TABLE_NOT_EXIST = ErrorCode.init("20010");
    ErrorCode DB_TABLE_CREATE_ERROR = ErrorCode.init("20011");
    ErrorCode DB_SAVE_BATCH_ERROR = ErrorCode.init("20012");
    ErrorCode DB_SAVE_ERROR = ErrorCode.init("20013");
    ErrorCode DB_UPDATE_ERROR = ErrorCode.init("20014");
    ErrorCode DB_QUERY_ERROR = ErrorCode.init("20015");
    ErrorCode DB_DELETE_ERROR = ErrorCode.init("20016");
}
