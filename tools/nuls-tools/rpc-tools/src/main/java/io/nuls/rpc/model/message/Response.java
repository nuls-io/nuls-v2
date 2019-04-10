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
package io.nuls.rpc.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.rpc.info.Constants;


/**
 * 远程调用的方法的结果
 * Result of a remotely invoked method
 *
 * @author tangyi
 * @date 2018/11/15
 */

public class Response {
    /**
     * This is the original request ID referred by a Request message
     */
    private String RequestId;

    /**
     * The time that the target service took to process the request in milliseconds.
     */
    private String ResponseProcessingTime;

    /**
     * The response status, 1 if successful, 0 otherwise.
     */
    private String ResponseStatus;

    /**
     * A string that could offer more clarification about the result of the process.
     */
    private String ResponseComment;

    /**
     * The maximum number of objects that the response contains per request.
     */
    private String ResponseMaxSize;

    /**
     * An object array that contains the result of the method processed, one object per request
     */
    private Object ResponseData;

    /**
     * 回复是否正确 / Whether the response is correct
     *
     * @return boolean
     */
    @JsonIgnore
    public boolean isSuccess() {
        return Constants.BOOLEAN_TRUE.equals(ResponseStatus);
    }

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String RequestId) {
        this.RequestId = RequestId;
    }

    public String getResponseProcessingTime() {
        return ResponseProcessingTime;
    }

    public void setResponseProcessingTime(String ResponseProcessingTime) {
        this.ResponseProcessingTime = ResponseProcessingTime;
    }

    public String getResponseStatus() {
        return ResponseStatus;
    }

    public void setResponseStatus(String ResponseStatus) {
        this.ResponseStatus = ResponseStatus;
    }

    public String getResponseComment() {
        return ResponseComment;
    }

    public void setResponseComment(String ResponseComment) {
        this.ResponseComment = ResponseComment;
    }

    public String getResponseMaxSize() {
        return ResponseMaxSize;
    }

    public void setResponseMaxSize(String ResponseMaxSize) {
        this.ResponseMaxSize = ResponseMaxSize;
    }

    public Object getResponseData() {
        return ResponseData;
    }

    public void setResponseData(Object ResponseData) {
        this.ResponseData = ResponseData;
    }
}
