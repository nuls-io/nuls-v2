/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.constant;

import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.rpc.model.ModuleE;

/**
 * @author: PierreLuo
 * @date: 2018/6/17
 */
public interface ContractErrorCode extends CommonCodeConstanst {
    
    ErrorCode CONTRACT_EXECUTE_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0001");
    ErrorCode CONTRACT_ADDRESS_NOT_EXIST = ErrorCode.init(ModuleE.SC.getPrefix() + "_0002");
    ErrorCode CONTRACT_TX_CREATE_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0003");
    ErrorCode ILLEGAL_CONTRACT_ADDRESS = ErrorCode.init(ModuleE.SC.getPrefix() + "_0004");
    ErrorCode NON_CONTRACTUAL_TRANSACTION = ErrorCode.init(ModuleE.SC.getPrefix() + "_0005");
    ErrorCode NON_CONTRACTUAL_TRANSACTION_NO_TRANSFER = ErrorCode.init(ModuleE.SC.getPrefix() + "_0006");
    ErrorCode CONTRACT_NAME_FORMAT_INCORRECT = ErrorCode.init(ModuleE.SC.getPrefix() + "_0007");
    ErrorCode CONTRACT_NOT_NRC20 = ErrorCode.init(ModuleE.SC.getPrefix() + "_0008");
    ErrorCode CONTRACT_NON_VIEW_METHOD = ErrorCode.init(ModuleE.SC.getPrefix() + "_0009");
    ErrorCode ILLEGAL_CONTRACT = ErrorCode.init(ModuleE.SC.getPrefix() + "_0010");
    ErrorCode CONTRACT_DUPLICATE_TOKEN_NAME = ErrorCode.init(ModuleE.SC.getPrefix() + "_0011");
    ErrorCode CONTRACT_NRC20_SYMBOL_FORMAT_INCORRECT = ErrorCode.init(ModuleE.SC.getPrefix() + "_0012");
    ErrorCode CONTRACT_LOCK = ErrorCode.init(ModuleE.SC.getPrefix() + "_0013");
    ErrorCode CONTRACT_NRC20_MAXIMUM_DECIMALS = ErrorCode.init(ModuleE.SC.getPrefix() + "_0014");
    ErrorCode CONTRACT_NRC20_MAXIMUM_TOTAL_SUPPLY = ErrorCode.init(ModuleE.SC.getPrefix() + "_0015");
    ErrorCode CONTRACT_MINIMUM_PRICE_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0016");
    ErrorCode CONTRACT_DELETE_BALANCE = ErrorCode.init(ModuleE.SC.getPrefix() + "_0017");
    ErrorCode CONTRACT_DELETE_CREATER = ErrorCode.init(ModuleE.SC.getPrefix() + "_0018");
    ErrorCode CONTRACT_DELETED = ErrorCode.init(ModuleE.SC.getPrefix() + "_0019");
    ErrorCode CONTRACT_GAS_LIMIT = ErrorCode.init(ModuleE.SC.getPrefix() + "_0020");
    ErrorCode CONTRACT_NOT_EXECUTE_VIEW = ErrorCode.init(ModuleE.SC.getPrefix() + "_0021");
    ErrorCode CONTRACT_NO_ACCEPT_DIRECT_TRANSFER = ErrorCode.init(ModuleE.SC.getPrefix() + "_0022");
    ErrorCode CONTRACT_METHOD_NOT_EXIST = ErrorCode.init(ModuleE.SC.getPrefix() + "_0023");
    ErrorCode AMOUNT_LOCK_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0024");
    ErrorCode INSUFFICIENT_BALANCE_TO_CONTRACT = ErrorCode.init(ModuleE.SC.getPrefix() + "_0025");
    ErrorCode CONTRACT_CREATOR_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0026");
    ErrorCode CONTRACT_CALLER_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0027");
    ErrorCode CONTRACT_DELETER_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0028");
    ErrorCode CONTRACT_OWNER_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0029");
    ErrorCode CONTRACT_BALANCE_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0030");
    ErrorCode CONTRACT_RECEIVER_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0031");
    ErrorCode INSUFFICIENT_TOKEN_BALANCE = ErrorCode.init(ModuleE.SC.getPrefix() + "_0032");
    ErrorCode DUPLICATE_REGISTER_CMD = ErrorCode.init(ModuleE.SC.getPrefix() + "_0033");
    ErrorCode CMD_REGISTER_NEW_TX_RETURN_TYPE_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0034");
    ErrorCode TRIGGER_PAYABLE_FOR_CONSENSUS_CONTRACT_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0035");
    ErrorCode CONTRACT_GAS_LIMIT_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0036");
    ErrorCode CONTRACT_COIN_ASSETS_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0037");
    ErrorCode CONTRACT_COIN_TO_EMPTY_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0038");
    ErrorCode CONTRACT_ALIAS_FORMAT_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0039");

    ErrorCode INSUFFICIENT_BALANCE = ErrorCode.init(ModuleE.SC.getPrefix() + "_0100");
    ErrorCode FEE_NOT_RIGHT = ErrorCode.init(ModuleE.SC.getPrefix() + "_0101");
    ErrorCode TOO_SMALL_AMOUNT = ErrorCode.init(ModuleE.SC.getPrefix() + "_0102");
    ErrorCode TX_NOT_EXIST = ErrorCode.init(ModuleE.SC.getPrefix() + "_0103");
    ErrorCode PASSWORD_IS_WRONG = ErrorCode.init(ModuleE.SC.getPrefix() + "_0104");
    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init(ModuleE.SC.getPrefix() + "_0105");
    ErrorCode ADDRESS_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_0106");

    ErrorCode CONTRACT_OTHER_ERROR = ErrorCode.init(ModuleE.SC.getPrefix() + "_9999");
}
