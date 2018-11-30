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

package io.nuls.rpc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 对外提供的方法的具体信息
 * Detail information on methods provided to the outside world
 *
 * @author tangyi
 * @date 2018/10/15
 * @description
 */
@ToString
@NoArgsConstructor
public class CmdDetail {
    @Getter
    @Setter
    private String methodName;
    @Getter
    @Setter
    private String methodDescription;
    @Getter
    @Setter
    private String methodMinEvent;
    @Getter
    @Setter
    private String methodMinPeriod;
    @Getter
    @Setter
    private String methodScope;
    @Getter
    @Setter
    private List<CmdParameter> parameters;
    @Getter
    @Setter
    @JsonIgnore
    private double version;
    @Getter
    @Setter
    @JsonIgnore
    private String invokeClass;
    @Getter
    @Setter
    @JsonIgnore
    private String invokeMethod;
}
