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
package io.nuls.ledger.model;

import io.nuls.core.constant.ErrorCode;
import io.nuls.ledger.constant.LedgerErrorCode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/01/02
 **/
public class ValidateResult {
    public static final String VALIDATE_SUCCESS_DESC = "success";
    public static final String VALIDATE_ORPHAN_DESC = "address {%s},nonce {%s}!={%s} is orphan transaction";
    public static final String VALIDATE_DOUBLE_EXPENSES_DESC = "address {%s},nonce {%s} is double expenses";
    public static final String VALIDATE_FAIL_DESC = "address {%s},nonce {%s} validate fail:{%s}";
    public static final String VALIDATE_TX_EXIST_DESC = "address={%s},hash={%s} in packing";
    public static final String BALANCE_NOT_ENOUGH_DESC = "assetKey={%s}, balance={%s},balance is not enough";

    public static Map<ErrorCode, ValidateResult> validateResultMap = new HashMap<>(6);

    static {
        validateResultMap.put(LedgerErrorCode.SUCCESS, ValidateResult.getValidateResult(LedgerErrorCode.SUCCESS, VALIDATE_SUCCESS_DESC));
        validateResultMap.put(LedgerErrorCode.ORPHAN, ValidateResult.getValidateResult(LedgerErrorCode.ORPHAN, VALIDATE_ORPHAN_DESC));
        validateResultMap.put(LedgerErrorCode.DOUBLE_EXPENSES, ValidateResult.getValidateResult(LedgerErrorCode.DOUBLE_EXPENSES, VALIDATE_DOUBLE_EXPENSES_DESC));
        validateResultMap.put(LedgerErrorCode.VALIDATE_FAIL, ValidateResult.getValidateResult(LedgerErrorCode.VALIDATE_FAIL, VALIDATE_FAIL_DESC));
        validateResultMap.put(LedgerErrorCode.TX_EXIST, ValidateResult.getValidateResult(LedgerErrorCode.TX_EXIST, VALIDATE_TX_EXIST_DESC));
        validateResultMap.put(LedgerErrorCode.BALANCE_NOT_ENOUGH, ValidateResult.getValidateResult(LedgerErrorCode.BALANCE_NOT_ENOUGH, BALANCE_NOT_ENOUGH_DESC));
    }

    public ValidateResult(ErrorCode validateCode, String validateDesc) {
        this.validateCode = validateCode;
        this.validateDesc = validateDesc;
    }

    public static ValidateResult getValidateResult(ErrorCode validateCode, String validateDesc) {
        return new ValidateResult(validateCode, validateDesc);
    }

    public static ValidateResult getResult(ErrorCode errorCode, String[] args) {
        return new ValidateResult(errorCode, String.format(validateResultMap.get(errorCode).validateDesc, args));
    }

    public static ValidateResult getSuccess() {
        return validateResultMap.get(LedgerErrorCode.SUCCESS);
    }

    /**
     * 校验返回编码
     */
    private ErrorCode validateCode;
    /**
     * 校验返回描述
     */
    private String validateDesc;

    public ErrorCode getValidateCode() {
        return validateCode;
    }

    public void setValidateCode(ErrorCode validateCode) {
        this.validateCode = validateCode;
    }

    public String getValidateDesc() {
        return validateDesc;
    }

    public void setValidateDesc(String validateDesc) {
        this.validateDesc = validateDesc;
    }

    public boolean isSuccess() {
        return validateCode.getCode().equals(LedgerErrorCode.SUCCESS.getCode());
    }

    public boolean isOrphan() {
        return validateCode.getCode().equals(LedgerErrorCode.ORPHAN.getCode());
    }

    public ErrorCode toErrorCode() {
        return validateCode;
    }
}
