package io.nuls.core.constant;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-18 17:48
 * @Description: 功能描述
 */
public interface CommonCodeConstanst {

    ErrorCode SUCCESS = ErrorCode.init("10000");
    ErrorCode FAILED = ErrorCode.init("err_0001");
    ErrorCode SYS_UNKOWN_EXCEPTION = ErrorCode.init("err_0002");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("err_0003");
    ErrorCode THREAD_REPETITION = ErrorCode.init("err_0004");
    ErrorCode LANGUAGE_CANNOT_SET_NULL = ErrorCode.init("err_0005");
    ErrorCode IO_ERROR = ErrorCode.init("err_0006");
    ErrorCode DATA_SIZE_ERROR = ErrorCode.init("err_0007");
    ErrorCode CONFIG_ERROR = ErrorCode.init("err_0008");
    ErrorCode SIGNATURE_ERROR = ErrorCode.init("err_0009");
    ErrorCode REQUEST_DENIED = ErrorCode.init("err_0010");
    ErrorCode DATA_SIZE_ERROR_EXTEND = ErrorCode.init("err_0011");
    ErrorCode PARAMETER_ERROR = ErrorCode.init("err_0012");
    ErrorCode NULL_PARAMETER = ErrorCode.init("err_0013");
    ErrorCode DATA_ERROR = ErrorCode.init("err_0014");
    ErrorCode DATA_NOT_FOUND = ErrorCode.init("err_0015");
    ErrorCode DOWNLOAD_VERSION_FAILD = ErrorCode.init("err_0016");
    ErrorCode PARSE_JSON_FAILD = ErrorCode.init("err_0017");
    ErrorCode FILE_OPERATION_FAILD = ErrorCode.init("err_0018");
    ErrorCode SERIALIZE_ERROR = ErrorCode.init("err_0019");
    ErrorCode DESERIALIZE_ERROR = ErrorCode.init("err_0020");
    ErrorCode CMD_NOTFOUND = ErrorCode.init("err_0021");

    ErrorCode REQUEST_TIME_OUT = ErrorCode.init("err_0022");
    ErrorCode RPC_REQUEST_FAILD = ErrorCode.init("err_0023");

    ErrorCode DB_TABLE_EXIST = ErrorCode.init("err_2009");
    ErrorCode DB_TABLE_NOT_EXIST = ErrorCode.init("err_2010");
    ErrorCode DB_TABLE_CREATE_ERROR = ErrorCode.init("err_2011");
    ErrorCode DB_SAVE_BATCH_ERROR = ErrorCode.init("err_2012");
    ErrorCode DB_SAVE_ERROR = ErrorCode.init("err_2013");
    ErrorCode DB_UPDATE_ERROR = ErrorCode.init("err_2014");
    ErrorCode DB_QUERY_ERROR = ErrorCode.init("err_2015");
    ErrorCode DB_DELETE_ERROR = ErrorCode.init("err_2016");

}
