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
    private String Abbreviation;

    /**
     * Protocol version
     */
    private String ProtocolVersion;

    /**
     * A String that represents the algorithm that will be used to receive and send messages if CompressionRate is greater than 0.
     * The default is zlib which a library is available in most development languages.
     */
    private String CompressionAlgorithm;

    /**
     * An integer between 0 and 9 that establishes the compression level in which the messages should be sent and received for this connection.
     * 0 means no compression while 9 maximum compression
     */
    private String CompressionRate;

    public String getAbbreviation() {
        return Abbreviation;
    }

    public void setAbbreviation(String Abbreviation) {
        this.Abbreviation = Abbreviation;
    }

    public String getProtocolVersion() {
        return ProtocolVersion;
    }

    public void setProtocolVersion(String ProtocolVersion) {
        this.ProtocolVersion = ProtocolVersion;
    }

    public String getCompressionAlgorithm() {
        return CompressionAlgorithm;
    }

    public void setCompressionAlgorithm(String CompressionAlgorithm) {
        this.CompressionAlgorithm = CompressionAlgorithm;
    }

    public String getCompressionRate() {
        return CompressionRate;
    }

    public void setCompressionRate(String CompressionRate) {
        this.CompressionRate = CompressionRate;
    }
}
