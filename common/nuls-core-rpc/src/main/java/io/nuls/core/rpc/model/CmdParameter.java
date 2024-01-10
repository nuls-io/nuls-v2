/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.core.rpc.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameter information of externally provided methods
 * Parameter information of methods provided to the outside world
 *
 * @author tangyi
 * @date 2018/11/19
 */

public class CmdParameter {
    /**
     * Parameter Name
     * Parameter name
     */
    @JsonProperty
    private String ParameterName;

    /**
     * Parameter type（Can be of any type, including base or custom types）
     * Parameter type(can be any type, including base type or custom type)
     */
    @JsonProperty
    private String ParameterType;

    /**
     * The range of numerical type parameters, formatted as：[(\[]\d+,\d+[)\]] . for example：(1,100]express1< x <=100
     * The range of numerical type parameters, format: [(\[] d+, \ d+ [)]]. For example, (1,100] means 1 < x <=100
     */
    @JsonProperty
    private String ParameterValidRange;

    /**
     * The format of character type parameters, with values as regular expressions
     * Format of character type parameters with regular expression
     */
    @JsonProperty
    private String ParameterValidRegExp;

    public CmdParameter() {
    }

    public CmdParameter(String parameterName, String parameterType, String parameterValidRange, String parameterValidRegExp){
        this.ParameterName = parameterName;
        this.ParameterType = parameterType;
        this.ParameterValidRange = parameterValidRange;
        this.ParameterValidRegExp = parameterValidRegExp;
    }

    @JsonIgnore
    public String getParameterName() {
        return ParameterName;
    }
    @JsonIgnore
    public void setParameterName(String parameterName) {
        ParameterName = parameterName;
    }
    @JsonIgnore
    public String getParameterType() {
        return ParameterType;
    }
    @JsonIgnore
    public void setParameterType(String parameterType) {
        ParameterType = parameterType;
    }
    @JsonIgnore
    public String getParameterValidRange() {
        return ParameterValidRange;
    }
    @JsonIgnore
    public void setParameterValidRange(String parameterValidRange) {
        ParameterValidRange = parameterValidRange;
    }
    @JsonIgnore
    public String getParameterValidRegExp() {
        return ParameterValidRegExp;
    }
    @JsonIgnore
    public void setParameterValidRegExp(String parameterValidRegExp) {
        ParameterValidRegExp = parameterValidRegExp;
    }
}
