package io.nuls.poc.constant;

import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;

/**
 * 共识模块错误码对应类
 * @author tag
 * 2018/11/12
 * */
public interface ConsensusErrorCode extends CommonCodeConstanst {
    ErrorCode DATA_ERROR = ErrorCode.init("cc_0001");
    ErrorCode TX_NOT_EXIST = ErrorCode.init("cc_0002");
    ErrorCode AGENT_NOT_EXIST = ErrorCode.init("cc_0003");
    ErrorCode DATA_NOT_EXIST = ErrorCode.init("cc_0004");
    ErrorCode ADDRESS_ERROR = ErrorCode.init("cc_0005");
    ErrorCode PARAM_ERROR = ErrorCode.init("cc_0006");
    ErrorCode AGENTADDR_AND_PACKING_SAME = ErrorCode.init("cc_0007");
    ErrorCode REWARDADDR_AND_PACKING_SAME = ErrorCode.init("cc_0008");
    ErrorCode COMMISSION_RATE_OUT_OF_RANGE = ErrorCode.init("cc_0009");
    ErrorCode DEPOSIT_OUT_OF_RANGE = ErrorCode.init("cc_0010");
    ErrorCode DEPOSIT_ERROR = ErrorCode.init("cc_0011");
    ErrorCode TX_DATA_VALIDATION_ERROR = ErrorCode.init("cc_0012");
    ErrorCode AGENT_EXIST = ErrorCode.init("cc_0013");
    ErrorCode AGENT_PACKING_EXIST = ErrorCode.init("cc_0014");
    ErrorCode LACK_OF_CREDIT = ErrorCode.init("cc_0015");
    ErrorCode TRANSACTION_REPEATED = ErrorCode.init("cc_0016");
    ErrorCode DEPOSIT_OVER_AMOUNT= ErrorCode.init("cc_0017");
    ErrorCode DEPOSIT_NOT_ENOUGH= ErrorCode.init("cc_0018");
    ErrorCode SAVE_FAILED= ErrorCode.init("cc_0019");
    ErrorCode ROLLBACK_FAILED= ErrorCode.init("cc_0020");
    ErrorCode MERKEL_HASH_ERROR= ErrorCode.init("cc_0021");
    ErrorCode BLOCK_ROUND_VALIDATE_ERROR= ErrorCode.init("cc_0022");
    ErrorCode BANANCE_NOT_ENNOUGH= ErrorCode.init("cc_0023");
    ErrorCode CHAIN_NOT_EXIST= ErrorCode.init("cc_0024");
    ErrorCode BLOCK_PUNISH_VALID_ERROR = ErrorCode.init("cc_0025");
    ErrorCode BLOCK_SIGNATURE_ERROR = ErrorCode.init("cc_0026");
    ErrorCode BLOCK_RED_PUNISH_ERROR = ErrorCode.init("cc_0027");
    ErrorCode BLOCK_COINBASE_VALID_ERROR = ErrorCode.init("cc_0028");
    ErrorCode TRANSACTION_LIST_IS_NULL = ErrorCode.init("cc_0029");
    ErrorCode INTERFACE_CALL_FAILED = ErrorCode.init("cc_0030");
    ErrorCode ACCOUNT_VALID_ERROR = ErrorCode.init("cc_0031");
    ErrorCode TX_SIGNTURE_ERROR = ErrorCode.init("cc_0032");
    ErrorCode COIN_DATA_VALID_ERROR = ErrorCode.init("cc_0033");
    ErrorCode DEPOSIT_WAS_CANCELED = ErrorCode.init("cc_0034");
    ErrorCode DEPOSIT_NEVER_CANCELED = ErrorCode.init("cc_0035");
}
