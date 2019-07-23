package io.nuls.crosschain.nuls.constant;


import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;


/**
 * 跨链模块错误码管理类
 * @author tag
 * 2019/04/08
 * */
public interface NulsCrossChainErrorCode extends CommonCodeConstanst {
    ErrorCode PAYEE_AND_PAYER_IS_THE_SAME_CHAIN = ErrorCode.init("cc_0001");
    ErrorCode COINFROM_NOT_FOUND = ErrorCode.init("cc_0002");
    ErrorCode CROSS_TX_PAYER_CHAIN_NOT_SAME = ErrorCode.init("cc_0003");
    ErrorCode CROSS_TX_PAYEE_CHAIN_NOT_SAME = ErrorCode.init("cc_0004");
    ErrorCode INSUFFICIENT_FEE = ErrorCode.init("cc_0005");
    ErrorCode ONLY_ONE_MULTI_SIGNATURE_ADDRESS_ALLOWED = ErrorCode.init("cc_0006");
    ErrorCode IS_NOT_MULTI_SIGNATURE_ADDRESS = ErrorCode.init("cc_0007");
    ErrorCode IS_MULTI_SIGNATURE_ADDRESS = ErrorCode.init("cc_0008");
    ErrorCode ACCOUNT_NOT_ENCRYPTED = ErrorCode.init("cc_0009");
    ErrorCode ADDRESS_IS_NOT_THE_CURRENT_CHAIN = ErrorCode.init("cc_0010");
    ErrorCode INSUFFICIENT_BALANCE = ErrorCode.init("cc_0011");
    ErrorCode COINDATA_IS_INCOMPLETE = ErrorCode.init("cc_0012");
    ErrorCode INTERFACE_CALL_FAILED = ErrorCode.init("cc_0013");
    ErrorCode CHAIN_NOT_EXIST = ErrorCode.init("cc_0014");
    ErrorCode COINDATA_VERIFY_FAIL = ErrorCode.init("cc_0015");
    ErrorCode TX_VERIFY_FAIL = ErrorCode.init("cc_0016");
    ErrorCode TX_DATA_VALIDATION_ERROR = ErrorCode.init("cc_0017");
    ErrorCode TX_COMMIT_FAIL = ErrorCode.init("cc_0018");
    ErrorCode TX_ROLLBACK_FAIL = ErrorCode.init("cc_0019");
    ErrorCode NOT_BELONG_TO_CURRENT_CHAIN = ErrorCode.init("cc_0020");
    ErrorCode CURRENT_CHAIN_UNREGISTERED_CROSS_CHAIN = ErrorCode.init("cc_0021");
    ErrorCode TARGET_CHAIN_UNREGISTERED_CROSS_CHAIN = ErrorCode.init("cc_0022");
    ErrorCode ASSET_UNREGISTERED_CROSS_CHAIN = ErrorCode.init("cc_0023");
    ErrorCode CROSS_CHAIN_NETWORK_UNAVAILABLE = ErrorCode.init("cc_0024");
    ErrorCode CHAIN_UNREGISTERED = ErrorCode.init("cc_0025");
    ErrorCode CHAIN_UNREGISTERED_VERIFIER = ErrorCode.init("cc_0026");
    ErrorCode CTX_SIGN_BYZANTINE_FAIL = ErrorCode.init("cc_0027");
    ErrorCode CROSS_ASSERT_VALID_ERROR = ErrorCode.init("cc_0028");
    ErrorCode TO_ADDRESS_ERROR = ErrorCode.init("cc_0029");
}
