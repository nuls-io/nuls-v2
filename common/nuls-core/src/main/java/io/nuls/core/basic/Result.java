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
package io.nuls.core.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;

import java.io.Serializable;

/**
 * @author vivi
 */
public class Result<T> implements Serializable {

    private boolean success;

    private String msg;

    private ErrorCode errorCode;

    private T data;

    public Result(boolean success) {
        this.success = success;
    }

    public Result(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public Result(boolean success, ErrorCode errorCode, T data) {
        this.success = success;
        this.errorCode = errorCode;
        this.data = data;
    }

    public Result(boolean success, ErrorCode errorCode) {
        this.success = success;
        this.errorCode = errorCode;
    }

    public Result(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    @JsonIgnore
    public boolean isFailed() {
        return !success;
    }

    public Result<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getMsg() {
        if (StringUtils.isBlank(msg)) {
            return errorCode.getMsg();
        }
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("result:{");
        buffer.append("\"success\": ").append(success).append(",");
        buffer.append("\"msg\": \"").append(msg).append("\",");
        if (errorCode == null) {
            buffer.append("\"errorCode\": \"\",");
        } else {
            buffer.append("\"errorCode\": \"").append(errorCode.getCode()).append("\",");
        }
        if (data != null) {
            try {
                buffer.append("\"entity\":").append(JSONUtils.obj2json(data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static Result getSuccess(ErrorCode successCode) {
        return new Result(true, successCode);
    }

    public static <T> Result getSuccess(T t) {

        return new Result(true, CommonCodeConstanst.SUCCESS, t);
    }

    public static Result getFailed(ErrorCode errorCode) {
        return new Result(false, errorCode, null);
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }
}
