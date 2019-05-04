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

package io.nuls.network.model;


import io.nuls.base.data.BaseNulsData;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.tools.constant.ErrorCode;

/**
 * NetworkEventResult
 *
 * @author lan
 * @date 2018/11/01
 */
public class NetworkEventResult {

    private boolean success;
    private ErrorCode errorCode;
    private BaseNulsData resultData;

    public NetworkEventResult(boolean success, ErrorCode errorCode, BaseNulsData resultData) {
        this.success = success;
        this.errorCode = errorCode;
        this.resultData = resultData;
    }

    public NetworkEventResult(boolean success, ErrorCode errorCode) {
        this.success = success;
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public BaseNulsData getResultData() {
        return resultData;
    }

    public void setResultData(BaseNulsData resultData) {
        this.resultData = resultData;
    }

    public static NetworkEventResult getResultSuccess() {
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS);
    }

    public static NetworkEventResult getResultSuccess(BaseNulsData resultData) {
        return new NetworkEventResult(true, NetworkErrorCode.SUCCESS, resultData);
    }

    public static NetworkEventResult getResultFail(ErrorCode errorCode) {
        return new NetworkEventResult(false, errorCode);
    }

    public static NetworkEventResult getResultFail(ErrorCode errorCode, BaseNulsData resultData) {
        return new NetworkEventResult(false, errorCode, resultData);
    }
}
