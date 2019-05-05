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
 * 握手确认
 * Handshake confirmation
 *
 * @author tangyi
 * @date 2018/11/15
 */

public class NegotiateConnectionResponse {
    @JsonProperty
    private String RequestID;
    /**
     * An unsigned small integer value, 0 if negotiation was a failure and 1 if it was successful
     */
    @JsonProperty
    private String NegotiationStatus;

    /**
     * A string value, useful to describe what exactly went wrong when the connection was rejected.
     */
    @JsonProperty
    private String NegotiationComment;

    @JsonIgnore
    public String getRequestID() {
        return RequestID;
    }

    @JsonIgnore
    public void setRequestID(String RequestId) {
        this.RequestID = RequestId;
    }

    @JsonIgnore
    public String getNegotiationStatus() {
        return NegotiationStatus;
    }

    @JsonIgnore
    public void setNegotiationStatus(String NegotiationStatus) {
        this.NegotiationStatus = NegotiationStatus;
    }

    @JsonIgnore
    public String getNegotiationComment() {
        return NegotiationComment;
    }

    @JsonIgnore
    public void setNegotiationComment(String NegotiationComment) {
        this.NegotiationComment = NegotiationComment;
    }
}
