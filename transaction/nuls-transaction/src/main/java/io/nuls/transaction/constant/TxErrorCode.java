package io.nuls.transaction.constant;

import io.nuls.tools.constant.CommonCodeConstanst;
import io.nuls.tools.constant.ErrorCode;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TxErrorCode extends CommonCodeConstanst {


//    ErrorCode PARAMETER_ERROR = ErrorCode.init(ModuleE.TX.abbr + "_0001");

    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init("10016");
    /**打包获超时*/
    ErrorCode PACKAGE_TIME_OUT = ErrorCode.init("10020");
    ErrorCode HASH_ERROR = ErrorCode.init("10026");

    /** 交易发起地址与链不匹配
     * The coin-from address does not match the chain
     */
    ErrorCode FROM_ADDRESS_NOT_MATCH_CHAIN = ErrorCode.init("00005");
    /** 交易接收方地址与链不匹配
     * The coin-to address does not match the chain
     */
    ErrorCode TO_ADDRESS_NOT_MATCH_CHAIN = ErrorCode.init("00006");
    ErrorCode INSUFFICIENT_FEE = ErrorCode.init("10036");
    ErrorCode ASSET_ERROR = ErrorCode.init("10053");
    ErrorCode COINFROM_NOT_FOUND = ErrorCode.init("10039");
    ErrorCode COINTO_NOT_FOUND = ErrorCode.init("10040");
    ErrorCode COINFROM_HAS_DUPLICATE_COIN = ErrorCode.init("10058");
    ErrorCode COINTO_HAS_DUPLICATE_COIN = ErrorCode.init("10059");
    ErrorCode COINFROM_NOT_SAME_CHAINID = ErrorCode.init("10065");
    ErrorCode COINTO_NOT_SAME_CHAINID = ErrorCode.init("10066");
    ErrorCode COINDATA_NOT_FOUND = ErrorCode.init("31016");


    ErrorCode TX_ALREADY_EXISTS = ErrorCode.init("10047");
    ErrorCode TX_NOT_EXIST = ErrorCode.init("31015");

    ErrorCode DESERIALIZE_TX_ERROR = ErrorCode.init("10049");
    ErrorCode DESERIALIZE_COINDATA_ERROR = ErrorCode.init("10050");

    ErrorCode CHAIN_NOT_FOUND = ErrorCode.init("10051");


    ErrorCode SIGN_ADDRESS_NOT_MATCH_COINFROM = ErrorCode.init("10061");
    ErrorCode HEIGHT_UPDATE_UNABLE_TO_REPACKAGE  = ErrorCode.init("10063");

    ErrorCode TX_LEDGER_VERIFY_FAIL = ErrorCode.init("10064");
    ErrorCode ORPHAN_TX = ErrorCode.init("31006");
    ErrorCode TX_REPEATED = ErrorCode.init("31011");


    ErrorCode TX_TYPE_INVALID = ErrorCode.init("31018");
    ErrorCode TX_DATA_VALIDATION_ERROR = ErrorCode.init("31008");
    ErrorCode TX_SIZE_TOO_LARGE = ErrorCode.init("31013");

    ErrorCode TOO_SMALL_AMOUNT = ErrorCode.init("31012");
    ErrorCode INSUFFICIENT_BALANCE = ErrorCode.init("10027");

}
