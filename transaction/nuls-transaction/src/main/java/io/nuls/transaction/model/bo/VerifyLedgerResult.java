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

package io.nuls.transaction.model.bo;

import io.nuls.tools.constant.ErrorCode;

/**
 * 验证交易结果组合封装 返回结果
 * @author: Charlie
 * @date: 2019-01-11
 */
public class VerifyLedgerResult {


    private boolean success;

    private boolean orphan;

    private ErrorCode errorCode;


    private VerifyLedgerResult() {
    }

    private VerifyLedgerResult(boolean success, boolean orphan) {
        this.success = success;
        this.orphan = orphan;
    }

    private VerifyLedgerResult(boolean success, boolean orphan, ErrorCode errorCode) {
        this.success = success;
        this.orphan = orphan;
        this.errorCode = errorCode;
    }

    public static VerifyLedgerResult success(boolean orphan){
        return new VerifyLedgerResult(true, orphan);
    }

    public static VerifyLedgerResult fail(ErrorCode errorCode){
        return new VerifyLedgerResult(false, false, errorCode);
    }


    /**
     * 账本业务验证成功
     * 1.账本返回成功，并且不为孤儿交易
     * @return
     */
    public boolean businessSuccess() {
        return success && !orphan;
    }
//
//    /**
//     * 账本业务验证失败
//     * 1.账本返回失败，含有错误码的情况
//     * 2.账本返回成功，但是交易为孤儿交易
//     * @return
//     */
//    public boolean businessFail() {
//        return !success || orphan;
//    }
/*
    public static void main(String[] args) {
        boolean success1 = true;

        boolean orphan2 = false;
        System.out.println(success1 && orphan2);
    }*/

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean getOrphan() {
        return orphan;
    }

    public void setOrphan(boolean orphan) {
        this.orphan = orphan;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    //
//    /** 1校验通过，2孤儿交易 3双花 4 其他异常 5重复交易(已确认过)*/
//    private int code;
//    /** 校验返回描述*/
//    private String desc;
//
//    private ErrorCode errorCode;
//
//    public static final int SUCCESS = 1;
//    public static final int ORPHAN = 2;
//    public static final int DOUBLE_SPENDING = 3;
//    public static final int OTHER_EXCEPTION = 4;
//    public static final int CONFIRMED = 5;
//
//    public VerifyLedgerResult(int code, String desc) {
//        this.code = code;
//        this.desc = desc;
//        switch (code){
//            case 1:
//                errorCode = TxErrorCode.SUCCESS;
//                break;
//            case 2:
//                errorCode = TxErrorCode.ORPHAN_TX;
//                break;
//            case 3:
//                errorCode = TxErrorCode.TX_REPEATED;
//                break;
//            case 5:
//                errorCode = TxErrorCode.TX_ALREADY_EXISTS;
//                break;
//            default:
//                errorCode = TxErrorCode.TX_LEDGER_VERIFY_FAIL;
//        }
//    }
//    public VerifyLedgerResult(int code) {
//        this.code = code;
//    }
//
//    public boolean success(){
//        return this.code == SUCCESS;
//    }
//
//    public int getCode() {
//        return code;
//    }
//
//    public void setCode(int code) {
//        this.code = code;
//    }
//
//    public ErrorCode getErrorCode() {
//        return errorCode;
//    }
//
//    public void setErrorCode(ErrorCode errorCode) {
//        this.errorCode = errorCode;
//    }
//
//    public String getDesc() {
//      /*  if(StringUtils.isBlank(this.desc)){
//            switch (this.code){
//                case SUCCESS:
//                    return "验证通过";
//                case ORPHAN:
//                    return "孤儿交易";
//                case DOUBLE_SPENDING:
//                    return "双花交易";
//                case OTHER_EXCEPTION:
//                    return "其他验证不通过情况";
//            }
//        }*/
//        return desc;
//    }
//
//    public void setDesc(String desc) {
//        this.desc = desc;
//    }
}
