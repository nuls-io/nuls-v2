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
 * 握手请求
 * Handshake request
 *
 * @author tangyi
 * @date 2018/11/15
 */

public class NegotiateConnection {
    /**
     * module Abbreviation
     */
    @JsonProperty
    private String Abbreviation;

    /**
     * Protocol version
     */
    @JsonProperty
    private String ProtocolVersion;

    /**
     * A String that represents the algorithm that will be used to receive and send messages if CompressionRate is greater than 0.
     * The default is zlib which a library is available in most development languages.
     */
    @JsonProperty
    private String CompressionAlgorithm;

    /**
     * An integer between 0 and 9 that establishes the compression level in which the messages should be sent and received for this connection.
     * 0 means no compression while 9 maximum compression
     */
    @JsonProperty
    private String CompressionRate;

    @JsonIgnore
    public String getAbbreviation() {
        return Abbreviation;
    }

    @JsonIgnore
    public void setAbbreviation(String Abbreviation) {
        this.Abbreviation = Abbreviation;
    }

    @JsonIgnore
    public String getProtocolVersion() {
        return ProtocolVersion;
    }

    @JsonIgnore
    public void setProtocolVersion(String ProtocolVersion) {
        this.ProtocolVersion = ProtocolVersion;
    }

    @JsonIgnore
    public String getCompressionAlgorithm() {
        return CompressionAlgorithm;
    }

    @JsonIgnore
    public void setCompressionAlgorithm(String CompressionAlgorithm) {
        this.CompressionAlgorithm = CompressionAlgorithm;
    }

    @JsonIgnore
    public String getCompressionRate() {
        return CompressionRate;
    }

    @JsonIgnore
    public void setCompressionRate(String CompressionRate) {
        this.CompressionRate = CompressionRate;
    }
}
