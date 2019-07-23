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

package io.nuls.core.rpc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 对外提供的方法的具体信息
 * Detail information on methods provided to the outside world
 *
 * @author tangyi
 * @date 2018/10/15
 */

public class CmdDetail {

    @JsonProperty
    private String MethodName;
    @JsonProperty
    private String MethodDescription;
    @JsonProperty
    private String MethodMinEvent;
    @JsonProperty
    private String MethodMinPeriod;
    @JsonProperty
    private String MethodScope;
    @JsonProperty
    private List<CmdParameter> Parameters;

    @JsonIgnore
    private double version;

    @JsonIgnore
    private String invokeClass;

    @JsonIgnore
    private String invokeMethod;

    @JsonIgnore
    private CmdPriority priority;
    @JsonIgnore
    public String getMethodName() {
        return MethodName;
    }
    @JsonIgnore
    public void setMethodName(String methodName) {
        this.MethodName = methodName;
    }
    @JsonIgnore
    public String getMethodDescription() {
        return MethodDescription;
    }
    @JsonIgnore
    public void setMethodDescription(String methodDescription) {
        this.MethodDescription = methodDescription;
    }
    @JsonIgnore
    public String getMethodMinEvent() {
        return MethodMinEvent;
    }
    @JsonIgnore
    public void setMethodMinEvent(String methodMinEvent) {
        this.MethodMinEvent = methodMinEvent;
    }
    @JsonIgnore
    public String getMethodMinPeriod() {
        return MethodMinPeriod;
    }
    @JsonIgnore
    public void setMethodMinPeriod(String methodMinPeriod) {
        this.MethodMinPeriod = methodMinPeriod;
    }
    @JsonIgnore
    public String getMethodScope() {
        return MethodScope;
    }
    @JsonIgnore
    public void setMethodScope(String methodScope) {
        this.MethodScope = methodScope;
    }
    @JsonIgnore
    public List<CmdParameter> getParameters() {
        return Parameters;
    }
    @JsonIgnore
    public void setParameters(List<CmdParameter> parameters) {
        this.Parameters = parameters;
    }
    @JsonIgnore
    public double getVersion() {
        return version;
    }
    @JsonIgnore
    public void setVersion(double version) {
        this.version = version;
    }
    @JsonIgnore
    public String getInvokeClass() {
        return invokeClass;
    }
    @JsonIgnore
    public void setInvokeClass(String invokeClass) {
        this.invokeClass = invokeClass;
    }
    @JsonIgnore
    public String getInvokeMethod() {
        return invokeMethod;
    }
    @JsonIgnore
    public void setInvokeMethod(String invokeMethod) {
        this.invokeMethod = invokeMethod;
    }
    @JsonIgnore
    public CmdPriority getPriority() {
        return priority;
    }
    @JsonIgnore
    public void setPriority(CmdPriority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "CmdDetail{" +
                "MethodName='" + MethodName + '\'' +
                ", MethodDescription='" + MethodDescription + '\'' +
                ", MethodMinEvent='" + MethodMinEvent + '\'' +
                ", MethodMinPeriod='" + MethodMinPeriod + '\'' +
                ", MethodScope='" + MethodScope + '\'' +
                ", Parameters=" + Parameters +
                ", version=" + version +
                ", invokeClass='" + invokeClass + '\'' +
                ", invokeMethod='" + invokeMethod + '\'' +
                ", priority='" + priority.getPriority() + '\'' +
                '}';
    }
}
