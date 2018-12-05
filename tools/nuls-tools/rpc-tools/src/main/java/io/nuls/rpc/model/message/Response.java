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
 * @description
 */
@ToString
@NoArgsConstructor
public class Response {
    @Getter
    @Setter
    private String requestId;
    @Getter
    @Setter
    private String responseProcessingTime;
    @Getter
    @Setter
    private String responseStatus;
    @Getter
    @Setter
    private String responseComment;
    @Getter
    @Setter
    private String responseMaxSize;
    @Getter
    @Setter
    private Object responseData;

    @JsonIgnore
    public boolean isSuccess() {
        return Constants.BOOLEAN_TRUE.equals(responseStatus);
    }
}
