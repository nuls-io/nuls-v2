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
package io.nuls.core.rpc.model.message;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Subscribing to multiple remote methods
 * Subscribe to multiple remote methods
 *
 * @author tangyi
 * @date 2018/11/15
 */

public class RegisterCompoundMethod {
    /**
     * This is a string identifying the virtual method
     */
    @JsonProperty
    private String CompoundMethodName;

    /**
     * A string describing the functionality of the method, the description will be available when querying the API for help
     */
    @JsonProperty
    private String CompoundMethodDescription;

    /**
     * This is an array containing the methods with their respective parameter aliases that make up the virtual method
     */
    @JsonProperty
    private List<Object> CompoundMethods;

    @JsonIgnore
    public String getCompoundMethodName() {
        return CompoundMethodName;
    }

    @JsonIgnore
    public void setCompoundMethodName(String CompoundMethodName) {
        this.CompoundMethodName = CompoundMethodName;
    }

    @JsonIgnore
    public String getCompoundMethodDescription() {
        return CompoundMethodDescription;
    }

    @JsonIgnore
    public void setCompoundMethodDescription(String CompoundMethodDescription) {
        this.CompoundMethodDescription = CompoundMethodDescription;
    }
    @JsonIgnore
    public List<Object> getCompoundMethods() {
        return CompoundMethods;
    }
    @JsonIgnore
    public void setCompoundMethods(List<Object> CompoundMethods) {
        this.CompoundMethods = CompoundMethods;
    }
}
