/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.contract.constant;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
public interface ContractCmdConstant {
    /**
     * module cmd
     */
    String BATCH_BEGIN = "sc_batch_begin";
    String INVOKE_CONTRACT = "sc_invoke_contract";
    String BATCH_BEFORE_END = "sc_batch_before_end";
    String BATCH_END = "sc_batch_end";
    String CREATE_VALIDATOR = "sc_create_validator";
    String CALL_VALIDATOR = "sc_call_validator";
    String DELETE_VALIDATOR = "sc_delete_validator";
    String INITIAL_ACCOUNT_TOKEN = "sc_initial_account_token";
    String REGISTER_CMD_FOR_CONTRACT = "sc_register_cmd_for_contract";
    String TRIGGER_PAYABLE_FOR_CONSENSUS_CONTRACT = "sc_trigger_payable_for_consensus_contract";

    /**
     * user cmd
     */
    String CREATE = "sc_create";
    String PRE_CREATE = "sc_pre_create";
    String VALIDATE_CREATE = "sc_validate_create";
    String CALL = "sc_call";
    String VALIDATE_CALL = "sc_validate_call";
    String DELETE = "sc_delete";
    String VALIDATE_DELETE = "sc_validate_delete";
    String TRANSFER = "sc_transfer";
    String TRANSFER_FEE = "sc_transfer_fee";
    String TOKEN_TRANSFER = "sc_token_transfer";
    String TOKEN_BALANCE = "sc_token_balance";
    String INVOKE_VIEW = "sc_invoke_view";
    String CONSTRUCTOR = "sc_constructor";
    String IMPUTED_CREATE_GAS = "sc_imputed_create_gas";
    String IMPUTED_CALL_GAS = "sc_imputed_call_gas";
    String CONTRACT_INFO = "sc_contract_info";
    String CONTRACT_RESULT = "sc_contract_result";
    String CONTRACT_TX = "sc_contract_tx";
    String TOKEN_ASSETS_LIST = "sc_token_assets_list";
    String UPLOAD = "sc_upload";
    String TOKEN_TRANSFER_LIST = "sc_token_transfer_list";
    String ACCOUNT_CONTRACTS = "sc_account_contracts";
}
