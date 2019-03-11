/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.api.model.rpc;

/**
 * @author Niels
 */
public class RpcResultError {

    private int code;

    private String message;

    private Object data;

    public RpcResultError() {

    }

    public RpcResultError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public RpcResultError(RpcErrorCode rpcErrorCode) {
        this.code = rpcErrorCode.getCode();
        this.message = rpcErrorCode.getMessage();
    }

    public RpcResultError(RpcErrorCode rpcErrorCode, Object data) {
        this.code = rpcErrorCode.getCode();
        this.message = rpcErrorCode.getMessage();
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public RpcResultError setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RpcResultError setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public RpcResultError setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"code\":")
                .append(code);
        sb.append(",\"message\":")
                .append('\"').append(message).append('\"');
        sb.append(",\"entity\":")
                .append('\"').append(data).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
