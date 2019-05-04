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
package io.nuls.core.rpc.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 远程调用的方法的结果
 * Result of a remotely invoked method
 *
 * @author tangyi
 * @date 2018/11/15
 */

public class Response {

    public static final int SUCCESS = 0;

    public static final String SUCCESS_MSG = "success";

    public static final int FAIL = 65536;

    /**
     * This is the original request ID referred by a Request message
     */
    @JsonProperty
    private String RequestID;

    /**
     * The time that the target service took to process the request in milliseconds.
     */
    @JsonProperty
    private String ResponseProcessingTime;

    /**
     * The response status, 1 if successful, 0 otherwise.
     */
    @JsonProperty
    private int ResponseStatus;

    /**
     * A string that could offer more clarification about the result of the process.
     */
    @JsonProperty
    private String ResponseComment;

    /**
     * The maximum number of objects that the response contains per request.
     */
    @JsonProperty
    private String ResponseMaxSize;

    /**
     * An object array that contains the result of the method processed, one object per request
     */
    @JsonProperty
    private Object ResponseData;

    @JsonProperty
    private String ResponseErrorCode;

    /**
     * 回复是否正确 / Whether the response is correct
     *
     * @return boolean
     */
    @JsonIgnore
    public boolean isSuccess() {
        return SUCCESS == ResponseStatus;
    }

    @JsonIgnore
    public String getRequestID() {
        return RequestID;
    }

    @JsonIgnore
    public void setRequestID(String RequestId) {
        this.RequestID = RequestId;
    }

    @JsonIgnore
    public String getResponseProcessingTime() {
        return ResponseProcessingTime;
    }

    @JsonIgnore
    public void setResponseProcessingTime(String ResponseProcessingTime) {
        this.ResponseProcessingTime = ResponseProcessingTime;
    }

    @JsonIgnore
    public int getResponseStatus() {
        return ResponseStatus;
    }

    @JsonIgnore
    public void setResponseStatus(int ResponseStatus) {
        this.ResponseStatus = ResponseStatus;
    }

    @JsonIgnore
    public String getResponseComment() {
        return ResponseComment;
    }

    @JsonIgnore
    public void setResponseComment(String ResponseComment) {
        this.ResponseComment = ResponseComment;
    }

    @JsonIgnore
    public String getResponseMaxSize() {
        return ResponseMaxSize;
    }

    @JsonIgnore
    public void setResponseMaxSize(String ResponseMaxSize) {
        this.ResponseMaxSize = ResponseMaxSize;
    }

    @JsonIgnore
    public Object getResponseData() {
        return ResponseData;
    }

    @JsonIgnore
    public void setResponseData(Object ResponseData) {
        this.ResponseData = ResponseData;
    }

    @JsonIgnore
    public String getResponseErrorCode() {
        return ResponseErrorCode;
    }

    @JsonIgnore
    public void setResponseErrorCode(String responseErrorCode) {
        ResponseErrorCode = responseErrorCode;
    }
}
