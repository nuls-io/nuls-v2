/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.rpc.model;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/10/19
 * @description
 */
public class Module {
    private String name;
    private String uri;
    private List<Rpc> rpcList;
    private List<String> dependsModule;

    public Module() {
    }

    public Module(String name, String uri, List<Rpc> rpcList, List<String> dependsModule) {
        this.name = name;
        this.uri = uri;
        this.rpcList = rpcList;
        this.dependsModule = dependsModule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<Rpc> getRpcList() {
        return rpcList;
    }

    public void setRpcList(List<Rpc> rpcList) {
        this.rpcList = rpcList;
    }

    public List<String> getDependsModule() {
        return dependsModule;
    }

    public void setDependsModule(List<String> dependsModule) {
        this.dependsModule = dependsModule;
    }
}
