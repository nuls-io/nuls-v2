package io.nuls.transaction.constant;

import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.rpc.model.ModuleE;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public interface TxErrorCode extends CommonCodeConstanst {

    ErrorCode HASH_ERROR = ErrorCode.init(ModuleE.TX.abbr + "_0001");
    ErrorCode FROM_ADDRESS_NOT_MATCH_CHAIN = ErrorCode.init(ModuleE.TX.abbr + "_0002");
    ErrorCode TO_ADDRESS_NOT_MATCH_CHAIN = ErrorCode.init(ModuleE.TX.abbr + "_0003");
    ErrorCode INSUFFICIENT_FEE = ErrorCode.init(ModuleE.TX.abbr + "_0004");
    ErrorCode ASSET_ERROR = ErrorCode.init(ModuleE.TX.abbr + "_0005");
    ErrorCode COINFROM_NOT_FOUND = ErrorCode.init(ModuleE.TX.abbr + "_0006");
    ErrorCode COINTO_NOT_FOUND = ErrorCode.init(ModuleE.TX.abbr + "_0007");
    ErrorCode COINFROM_HAS_DUPLICATE_COIN = ErrorCode.init(ModuleE.TX.abbr + "_0008");
    ErrorCode COINTO_HAS_DUPLICATE_COIN = ErrorCode.init(ModuleE.TX.abbr + "_0009");
    ErrorCode COINFROM_NOT_SAME_CHAINID = ErrorCode.init(ModuleE.TX.abbr + "_00010");
    ErrorCode COINTO_NOT_SAME_CHAINID = ErrorCode.init(ModuleE.TX.abbr + "_0011");
    ErrorCode COINDATA_NOT_FOUND = ErrorCode.init(ModuleE.TX.abbr + "_0012");

    ErrorCode TX_ALREADY_EXISTS = ErrorCode.init(ModuleE.TX.abbr + "_0013");
    ErrorCode TX_NOT_EXIST = ErrorCode.init(ModuleE.TX.abbr + "_0014");
    ErrorCode DESERIALIZE_TX_ERROR = ErrorCode.init(ModuleE.TX.abbr + "_0015");
    ErrorCode DESERIALIZE_COINDATA_ERROR = ErrorCode.init(ModuleE.TX.abbr + "_0016");
    ErrorCode SIGN_ADDRESS_NOT_MATCH_COINFROM = ErrorCode.init(ModuleE.TX.abbr + "_0017");
    ErrorCode HEIGHT_UPDATE_UNABLE_TO_REPACKAGE  = ErrorCode.init(ModuleE.TX.abbr + "_0018");
    ErrorCode PACKAGE_TIME_OUT = ErrorCode.init(ModuleE.TX.abbr + "_0019");

    ErrorCode CHAIN_NOT_FOUND = ErrorCode.init(ModuleE.TX.abbr + "_0020");
    ErrorCode TX_TYPE_INVALID = ErrorCode.init(ModuleE.TX.abbr + "_0021");
    ErrorCode TX_DATA_VALIDATION_ERROR = ErrorCode.init(ModuleE.TX.abbr + "_0022");
    ErrorCode TX_SIZE_TOO_LARGE = ErrorCode.init(ModuleE.TX.abbr + "_0023");
    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init(ModuleE.TX.abbr + "_0024");

    ErrorCode TX_LEDGER_VERIFY_FAIL = ErrorCode.init(ModuleE.TX.abbr + "_0025");
    ErrorCode ORPHAN_TX = ErrorCode.init(ModuleE.TX.abbr + "_0026");
    ErrorCode TX_REPEATED = ErrorCode.init(ModuleE.TX.abbr + "_0027");


    ErrorCode REMOTE_RESPONSE_DATA_NOT_FOUND = ErrorCode.init(ModuleE.TX.abbr + "_0028");


}
