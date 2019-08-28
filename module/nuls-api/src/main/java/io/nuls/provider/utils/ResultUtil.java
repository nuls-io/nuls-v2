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
package io.nuls.provider.utils;

import io.nuls.base.api.provider.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.model.jsonrpc.RpcResultError;

/**
 * @author: PierreLuo
 * @date: 2019-06-28
 */
public class ResultUtil {

    public static RpcClientResult getRpcClientResult(Result result) {
        if (result.isFailed()) {
            return RpcClientResult.getFailed(new ErrorData(result.getStatus(), result.getMessage()));
        }
        Object obj;
        do {
            if ((obj = result.getData()) != null) {
                break;
            } else if ((obj = result.getList()) != null) {
                break;
            } else {
                obj = null;
            }
        } while (false);
        if (obj != null) {
            return RpcClientResult.getSuccess(obj);
        } else {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.DATA_NOT_FOUND.getCode(), CommonCodeConstanst.DATA_NOT_FOUND.getMsg()));
        }
    }

    public static RpcClientResult getRpcClientResult(io.nuls.core.basic.Result result) {
        if (result.isFailed()) {
            String msg = result.getMsg();
            if (msg == null) {
                msg = "";
            }
            ErrorCode errorCode = result.getErrorCode();
            if (errorCode == null) {
                errorCode = CommonCodeConstanst.DATA_ERROR;
            }
            return RpcClientResult.getFailed(new ErrorData(errorCode.getCode(), errorCode.getMsg() + ";" + msg));
        }
        return RpcClientResult.getSuccess(result.getData());
    }

    public static RpcClientResult getNulsExceptionRpcClientResult(NulsException e) {
        ErrorCode errorCode = e.getErrorCode();
        if (errorCode != null) {
            return RpcClientResult.getFailed(new ErrorData(errorCode.getCode(), e.format()));
        }
        return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.DATA_ERROR.getCode(), e.getMessage()));
    }

    public static Result getNulsExceptionResult(NulsException e) {
        ErrorCode errorCode = e.getErrorCode();
        if (errorCode != null) {
            return Result.fail(errorCode.getCode(), e.format());
        }
        return Result.fail(CommonCodeConstanst.DATA_ERROR.getCode(), e.getMessage());
    }

    public static RpcResult getJsonRpcResult(Result result) {
        RpcResult rpcResult = new RpcResult();
        if (result.isFailed()) {
            return rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        Object obj;
        do {
            if ((obj = result.getData()) != null) {
                break;
            } else if ((obj = result.getList()) != null) {
                break;
            } else {
                obj = null;
            }
        } while (false);
        if (obj != null) {
            rpcResult.setResult(obj);
        } else {
            rpcResult.setError(new RpcResultError(CommonCodeConstanst.DATA_NOT_FOUND.getCode(), CommonCodeConstanst.DATA_NOT_FOUND.getMsg(), null));
        }
        return rpcResult;
    }

    public static RpcResult getJsonRpcResult(io.nuls.core.basic.Result result) {
        RpcResult rpcResult = new RpcResult();
        if (result.isFailed()) {
            ErrorCode errorCode = result.getErrorCode();
            if (errorCode == null) {
                errorCode = CommonCodeConstanst.DATA_ERROR;
            }
            return rpcResult.setError(new RpcResultError(errorCode.getCode(), errorCode.getMsg(), result.getMsg()));
        }
        return rpcResult.setResult(result.getData());
    }

    public static RpcResult getNulsExceptionJsonRpcResult(NulsException e) {
        ErrorCode errorCode = e.getErrorCode();
        if (errorCode != null) {
            return RpcResult.failed(errorCode, e.format());
        }
        return RpcResult.failed(CommonCodeConstanst.DATA_ERROR, e.getMessage());
    }

}
