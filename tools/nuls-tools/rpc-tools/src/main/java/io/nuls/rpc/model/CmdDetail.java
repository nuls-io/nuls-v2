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

import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/15
 * @description
 */

public class CmdDetail {

    private String methodName;
    private String methodDescription;
    private int methodMinEvent;
    private int methodMinPeriod;
    private String methodScope;
    private List<CmdParameter> parameters;
    private double version;
    @JsonIgnore
    private String invokeClass;
    @JsonIgnore
    private String invokeMethod;

    public CmdDetail() {
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    public void setMethodDescription(String methodDescription) {
        this.methodDescription = methodDescription;
    }

    public int getMethodMinEvent() {
        return methodMinEvent;
    }

    public void setMethodMinEvent(int methodMinEvent) {
        this.methodMinEvent = methodMinEvent;
    }

    public int getMethodMinPeriod() {
        return methodMinPeriod;
    }

    public void setMethodMinPeriod(int methodMinPeriod) {
        this.methodMinPeriod = methodMinPeriod;
    }

    public String getMethodScope() {
        return methodScope;
    }

    public void setMethodScope(String methodScope) {
        this.methodScope = methodScope;
    }

    public List<CmdParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<CmdParameter> parameters) {
        this.parameters = parameters;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public String getInvokeClass() {
        return invokeClass;
    }

    public void setInvokeClass(String invokeClass) {
        this.invokeClass = invokeClass;
    }

    public String getInvokeMethod() {
        return invokeMethod;
    }

    public void setInvokeMethod(String invokeMethod) {
        this.invokeMethod = invokeMethod;
    }
}
