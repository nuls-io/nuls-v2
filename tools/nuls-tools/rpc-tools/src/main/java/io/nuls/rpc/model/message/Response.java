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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 远程调用的方法的结果
 * Result of a remotely invoked method
 *
 * @author tangyi
 * @date 2018/11/15
 */
@ToString
@NoArgsConstructor
public class Response {
    /**
     * This is the original request ID referred by a Request message
     */
    @Getter
    @Setter
    private String requestId;

    /**
     * The time that the target service took to process the request in milliseconds.
     */
    @Getter
    @Setter
    private String responseProcessingTime;

    /**
     * The response status, 1 if successful, 0 otherwise.
     */
    @Getter
    @Setter
    private String responseStatus;

    /**
     * A string that could offer more clarification about the result of the process.
     */
    @Getter
    @Setter
    private String responseComment;

    /**
     * The maximum number of objects that the response contains per request.
     */
    @Getter
    @Setter
    private String responseMaxSize;

    /**
     * An object array that contains the result of the method processed, one object per request
     */
    @Getter
    @Setter
    private Object responseData;

    /**
     * 回复是否正确 / Whether the response is correct
     * @return boolean
     */
    @JsonIgnore
    public boolean isSuccess() {
        return Constants.BOOLEAN_TRUE.equals(responseStatus);
    }
}
