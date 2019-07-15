/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.account.constant;

import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;

/**
 * @author: qinyifeng
 */
public interface AccountErrorCode extends CommonCodeConstanst {

    ErrorCode PASSWORD_IS_WRONG = ErrorCode.init(ModuleE.AC.getPrefix() + "_0000");
    ErrorCode ACCOUNT_NOT_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0001");
    ErrorCode ACCOUNT_IS_ALREADY_ENCRYPTED = ErrorCode.init(ModuleE.AC.getPrefix() + "_0002");
    ErrorCode ACCOUNT_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0003");
    ErrorCode ADDRESS_ERROR = ErrorCode.init(ModuleE.AC.getPrefix() + "_0004");
    ErrorCode ALIAS_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0005");
    ErrorCode ALIAS_NOT_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0006");
    ErrorCode ACCOUNT_ALREADY_SET_ALIAS = ErrorCode.init(ModuleE.AC.getPrefix() + "_0007");
    ErrorCode ACCOUNT_UNENCRYPTED = ErrorCode.init(ModuleE.AC.getPrefix() + "_0008");
    ErrorCode ALIAS_CONFLICT = ErrorCode.init(ModuleE.AC.getPrefix() + "_0009");
    ErrorCode HAVE_ENCRYPTED_ACCOUNT = ErrorCode.init(ModuleE.AC.getPrefix() + "_0010");
    ErrorCode HAVE_UNENCRYPTED_ACCOUNT = ErrorCode.init(ModuleE.AC.getPrefix() + "_0011");
    ErrorCode PRIVATE_KEY_WRONG = ErrorCode.init(ModuleE.AC.getPrefix() + "_0012");
    ErrorCode ALIAS_ROLLBACK_ERROR = ErrorCode.init(ModuleE.AC.getPrefix() + "_0013");
    ErrorCode ACCOUNTKEYSTORE_FILE_NOT_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0014");
    ErrorCode ACCOUNTKEYSTORE_FILE_DAMAGED = ErrorCode.init(ModuleE.AC.getPrefix() + "_0015");
    ErrorCode ALIAS_FORMAT_WRONG = ErrorCode.init(ModuleE.AC.getPrefix() + "_0016");
    ErrorCode PASSWORD_FORMAT_WRONG = ErrorCode.init(ModuleE.AC.getPrefix() + "_0017");
    ErrorCode DECRYPT_ACCOUNT_ERROR = ErrorCode.init(ModuleE.AC.getPrefix() + "_0018");
    ErrorCode ACCOUNT_IS_ALREADY_ENCRYPTED_AND_LOCKED = ErrorCode.init(ModuleE.AC.getPrefix() + "_0019");
    ErrorCode REMARK_TOO_LONG = ErrorCode.init(ModuleE.AC.getPrefix() + "_0020");
    ErrorCode INPUT_TOO_SMALL = ErrorCode.init(ModuleE.AC.getPrefix() + "_0021");
    ErrorCode MUST_BURN_A_NULS = ErrorCode.init(ModuleE.AC.getPrefix() + "_0022");
    ErrorCode SIGN_COUNT_TOO_LARGE = ErrorCode.init(ModuleE.AC.getPrefix() + "_0023");
    ErrorCode IS_NOT_CURRENT_CHAIN_ADDRESS = ErrorCode.init(ModuleE.AC.getPrefix() + "_0024");
    ErrorCode IS_MULTI_SIGNATURE_ADDRESS = ErrorCode.init(ModuleE.AC.getPrefix() + "_0025");
    ErrorCode IS_NOT_MULTI_SIGNATURE_ADDRESS = ErrorCode.init(ModuleE.AC.getPrefix() + "_0026");
    ErrorCode ASSET_NOT_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0027");
    ErrorCode INSUFFICIENT_BALANCE = ErrorCode.init(ModuleE.AC.getPrefix() + "_0028");
    ErrorCode INSUFFICIENT_FEE = ErrorCode.init(ModuleE.AC.getPrefix() + "_0029");
    ErrorCode CHAIN_NOT_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0030");
    ErrorCode COINDATA_IS_INCOMPLETE = ErrorCode.init(ModuleE.AC.getPrefix() + "_0031");
    ErrorCode TX_NOT_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0032");
    ErrorCode TX_COINDATA_NOT_EXIST = ErrorCode.init(ModuleE.AC.getPrefix() + "_0033");
    ErrorCode TX_DATA_VALIDATION_ERROR = ErrorCode.init(ModuleE.AC.getPrefix() + "_0034");
    ErrorCode TX_TYPE_ERROR = ErrorCode.init(ModuleE.AC.getPrefix() + "_0035");
    ErrorCode TX_NOT_EFFECTIVE = ErrorCode.init(ModuleE.AC.getPrefix() + "_0036");
    ErrorCode TX_SIZE_TOO_LARGE = ErrorCode.init(ModuleE.AC.getPrefix() + "_0037");
    ErrorCode TX_COINFROM_NOT_FOUND = ErrorCode.init(ModuleE.AC.getPrefix() + "_0038");
    ErrorCode TX_COINTO_NOT_FOUND = ErrorCode.init(ModuleE.AC.getPrefix() + "_0039");
    ErrorCode CHAINID_ERROR = ErrorCode.init(ModuleE.AC.getPrefix() + "_0040");
    ErrorCode ASSETID_ERROR = ErrorCode.init(ModuleE.AC.getPrefix() + "_0041");
    ErrorCode SIGN_ADDRESS_NOT_MATCH = ErrorCode.init(ModuleE.AC.getPrefix() + "_0042");
    ErrorCode ADDRESS_ALREADY_SIGNED = ErrorCode.init(ModuleE.AC.getPrefix() + "_0043");
    ErrorCode COINTO_DUPLICATE_COIN = ErrorCode.init(ModuleE.AC.getPrefix() + "_0044");
    ErrorCode ALIAS_SAVE_ERROR = ErrorCode.init(ModuleE.AC.getPrefix() + "_0045");
    ErrorCode AMOUNT_TOO_SMALL = ErrorCode.init(ModuleE.AC.getPrefix() + "_0046");
    ErrorCode ADDRESS_TRANSFER_BAN = ErrorCode.init(ModuleE.AC.getPrefix() + "_0047");
    ErrorCode REMOTE_RESPONSE_DATA_NOT_FOUND = ErrorCode.init(ModuleE.AC.getPrefix() + "_0048");
    ErrorCode COINFROM_UNDERPAYMENT = ErrorCode.init(ModuleE.AC.getPrefix() + "_0049");
    ErrorCode ONLY_ONE_MULTI_SIGN_ADDRESS = ErrorCode.init(ModuleE.AC.getPrefix() + "_0050");
    ErrorCode COINDATA_CANNOT_HAS_CONTRACT_ADDRESS = ErrorCode.init(ModuleE.AC.getPrefix() + "_0051");
    ErrorCode CONTRACT_ADDRESS_CANNOT_CREATE_MULTISIG_ACCOUNT = ErrorCode.init(ModuleE.AC.getPrefix() + "_0052");
    ErrorCode MULTISIG_ADDRESS_CANNOT_CREATE_MULTISIG_ACCOUNT = ErrorCode.init(ModuleE.AC.getPrefix() + "_0053");
}
