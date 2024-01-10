/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.core.constant.ErrorCode;

/**
 * Validate transaction result combination encapsulation Return results
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
     * Account book business verification successful
     * 1.The ledger returned successfully and is not an orphan transaction
     * @return
     */
    public boolean businessSuccess() {
        return success && !orphan;
    }

    /**
     * Orphan Trading
     * @return
     */
    public boolean isOrphan() {
        return success && orphan;
    }

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

}
