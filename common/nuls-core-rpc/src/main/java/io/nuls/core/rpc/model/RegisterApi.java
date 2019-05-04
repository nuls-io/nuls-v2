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
import java.util.Map;

/**
 * 一个模块对外提供的所有方法的合集
 * A collection of all methods provided by a module
 *
 * @author tangyi
 * @date 2018/11/19
 */

public class RegisterApi {


    @JsonProperty
    private List<CmdDetail> Methods;

    /**
     * Key: Role
     * Value: Version
     */
    @JsonProperty
    private Map<String, String> Dependencies;

    @JsonProperty
    private Map<String, String> ConnectionInformation;

    @JsonProperty
    private String ModuleDomain;

    @JsonProperty
    private Map<String, String[]> ModuleRoles;

    @JsonProperty
    private String ModuleVersion;

    @JsonProperty
    private String Abbreviation;

    @JsonProperty
    private String ModuleName;

    @JsonIgnore
    public List<CmdDetail> getMethods() {
        return Methods;
    }

    @JsonIgnore
    public void setMethods(List<CmdDetail> apiMethods) {
        this.Methods = apiMethods;
    }

    @JsonIgnore
    public Map<String, String> getDependencies() {
        return Dependencies;
    }

    @JsonIgnore
    public void setDependencies(Map<String, String> dependencies) {
        this.Dependencies = dependencies;
    }

    @JsonIgnore
    public Map<String, String> getConnectionInformation() {
        return ConnectionInformation;
    }

    @JsonIgnore
    public void setConnectionInformation(Map<String, String> connectionInformation) {
        this.ConnectionInformation = connectionInformation;
    }

    @JsonIgnore
    public String getModuleDomain() {
        return ModuleDomain;
    }

    @JsonIgnore
    public void setModuleDomain(String moduleDomain) {
        this.ModuleDomain = moduleDomain;
    }

    @JsonIgnore
    public Map<String, String[]> getModuleRoles() {
        return ModuleRoles;
    }

    @JsonIgnore
    public void setModuleRoles(Map<String, String[]> moduleRoles) {
        this.ModuleRoles = moduleRoles;
    }

    @JsonIgnore
    public String getModuleVersion() {
        return ModuleVersion;
    }

    @JsonIgnore
    public void setModuleVersion(String moduleVersion) {
        this.ModuleVersion = moduleVersion;
    }

    @JsonIgnore
    public String getAbbreviation() {
        return Abbreviation;
    }

    @JsonIgnore
    public void setAbbreviation(String moduleAbbreviation) {
        this.Abbreviation = moduleAbbreviation;
    }

    @JsonIgnore
    public String getModuleName() {
        return ModuleName;
    }

    @JsonIgnore
    public void setModuleName(String moduleName) {
        this.ModuleName = moduleName;
    }
}
