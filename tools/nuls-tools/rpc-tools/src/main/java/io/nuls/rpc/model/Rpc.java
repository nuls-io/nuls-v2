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

/**
 * @author tangyi
 * @date 2018/10/15
 * @description
 */

public class Rpc {

    private String cmd;
    private double version;
    private String invokeClass;
    private String invokeMethod;
    private boolean preCompatible;

    public Rpc() {
    }

    public Rpc(String cmd, double version, String invokeClass, String invokeMethod, boolean preCompatible) {
        this.cmd = cmd;
        this.version = version;
        this.invokeClass = invokeClass;
        this.invokeMethod = invokeMethod;
        this.preCompatible = preCompatible;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
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

    public boolean isPreCompatible() {
        return preCompatible;
    }

    public void setPreCompatible(boolean preCompatible) {
        this.preCompatible = preCompatible;
    }

    @Override
    public String toString() {
        return "[cmd=" + cmd + "][version=" + version + "][preCompatible=" + preCompatible + "][invokeClass=" + invokeClass + "][invokeMethod=" + invokeMethod + "]";
    }
}
