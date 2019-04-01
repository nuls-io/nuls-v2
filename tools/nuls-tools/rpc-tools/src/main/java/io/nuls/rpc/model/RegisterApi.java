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



import java.util.List;
import java.util.Map;

/**
 * 一个模块对外提供的所有方法的合集
 * A collection of all methods provided by a module
 *
 * @author tangyi
 * @date 2018/11/19
 */

public class RegisterApi {


    private List<CmdDetail> apiMethods;

    /**
     * Key: Role
     * Value: Version
     */
    private Map<String, String> dependencies;

    private Map<String, String> connectionInformation;

    private String moduleDomain;

    private Map<String, String[]> moduleRoles;

    private String moduleVersion;

    private String moduleAbbreviation;

    private String moduleName;

    public List<CmdDetail> getApiMethods() {
        return apiMethods;
    }

    public void setApiMethods(List<CmdDetail> apiMethods) {
        this.apiMethods = apiMethods;
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }

    public Map<String, String> getConnectionInformation() {
        return connectionInformation;
    }

    public void setConnectionInformation(Map<String, String> connectionInformation) {
        this.connectionInformation = connectionInformation;
    }

    public String getModuleDomain() {
        return moduleDomain;
    }

    public void setModuleDomain(String moduleDomain) {
        this.moduleDomain = moduleDomain;
    }

    public Map<String, String[]> getModuleRoles() {
        return moduleRoles;
    }

    public void setModuleRoles(Map<String, String[]> moduleRoles) {
        this.moduleRoles = moduleRoles;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getModuleAbbreviation() {
        return moduleAbbreviation;
    }

    public void setModuleAbbreviation(String moduleAbbreviation) {
        this.moduleAbbreviation = moduleAbbreviation;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
