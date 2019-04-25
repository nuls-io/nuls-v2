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

import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.constant.ValidateEnum;
import io.nuls.tools.constant.ErrorCode;

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
    public static Map<ValidateEnum, ValidateResult> validateResultMap = new HashMap<>(5);

    static {
        validateResultMap.put(ValidateEnum.SUCCESS_CODE, ValidateResult.getValidateResult(ValidateEnum.SUCCESS_CODE.getValue(), VALIDATE_SUCCESS_DESC));
        validateResultMap.put(ValidateEnum.ORPHAN_CODE, ValidateResult.getValidateResult(ValidateEnum.ORPHAN_CODE.getValue(), VALIDATE_ORPHAN_DESC));
        validateResultMap.put(ValidateEnum.DOUBLE_EXPENSES_CODE, ValidateResult.getValidateResult(ValidateEnum.DOUBLE_EXPENSES_CODE.getValue(), VALIDATE_DOUBLE_EXPENSES_DESC));
        validateResultMap.put(ValidateEnum.FAIL_CODE, ValidateResult.getValidateResult(ValidateEnum.FAIL_CODE.getValue(), VALIDATE_FAIL_DESC));
        validateResultMap.put(ValidateEnum.TX_EXIST_CODE, ValidateResult.getValidateResult(ValidateEnum.TX_EXIST_CODE.getValue(), VALIDATE_TX_EXIST_DESC));
    }

    public ValidateResult(int validateCode, String validateDesc) {
        this.validateCode = validateCode;
        this.validateDesc = validateDesc;
    }

    public static ValidateResult getValidateResult(int validateCode, String validateDesc) {
        return new ValidateResult(validateCode, validateDesc);
    }

    public static ValidateResult getResult(ValidateEnum type, String[] args) {
        return new ValidateResult(type.getValue(), String.format(validateResultMap.get(type).validateDesc, args));
    }

    public static ValidateResult getSuccess() {
        return validateResultMap.get(ValidateEnum.SUCCESS_CODE);
    }

    /**
     * 校验返回编码
     */
    private int validateCode;
    /**
     * 校验返回描述
     */
    private String validateDesc;

    public int getValidateCode() {
        return validateCode;
    }

    public void setValidateCode(int validateCode) {
        this.validateCode = validateCode;
    }

    public String getValidateDesc() {
        return validateDesc;
    }

    public void setValidateDesc(String validateDesc) {
        this.validateDesc = validateDesc;
    }

    public boolean isSuccess() {
        return validateCode == ValidateEnum.SUCCESS_CODE.getValue();
    }

    public boolean isOrphan() {
        return validateCode == ValidateEnum.ORPHAN_CODE.getValue();
    }

    public ErrorCode toErrorCode() {
        if (validateCode == ValidateEnum.DOUBLE_EXPENSES_CODE.getValue()) {
            return LedgerErrorCode.DOUBLE_EXPENSES;
        } else if (validateCode == ValidateEnum.TX_EXIST_CODE.getValue()) {
            return LedgerErrorCode.TX_EXIST;
        } else if (validateCode == ValidateEnum.FAIL_CODE.getValue()) {
            return LedgerErrorCode.VALIDATE_FAIL;
        } else {
            return LedgerErrorCode.SYS_UNKOWN_EXCEPTION;
        }
    }
}
