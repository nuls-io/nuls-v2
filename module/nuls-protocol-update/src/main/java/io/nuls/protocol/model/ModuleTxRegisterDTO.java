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

package io.nuls.protocol.model;

import io.nuls.core.rpc.protocol.TxDefine;

import java.util.List;

/**
 * 各个模块需要向交易管理模块注册自己的每个交易的验证器,整个模块的验证器
 *
 * @author: qinyifeng
 * @date: 2018/11/30
 */
public class ModuleTxRegisterDTO {
    /**
     * 协议版本
     */
    private short version;
    /**
     * 模块编码
     */
    private String moduleCode;
    /**
     * 模块统一验证器
     */
    private String moduleValidator;

    /**
     * Transaction commit cmd name
     */
    private String moduleCommit;

    /**
     * Transaction validator cmd name
     */
    private String moduleRollback;

    /**
     * 交易验证器
     */
    private List<TxDefine> list;

    public ModuleTxRegisterDTO() {

    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleValidator() {
        return moduleValidator;
    }

    public void setModuleValidator(String moduleValidator) {
        this.moduleValidator = moduleValidator;
    }

    public List<TxDefine> getList() {
        return list;
    }

    public void setList(List<TxDefine> list) {
        this.list = list;
    }

    public String getModuleCommit() {
        return moduleCommit;
    }

    public void setModuleCommit(String moduleCommit) {
        this.moduleCommit = moduleCommit;
    }

    public String getModuleRollback() {
        return moduleRollback;
    }

    public void setModuleRollback(String moduleRollback) {
        this.moduleRollback = moduleRollback;
    }

    @Override
    public String toString() {
        return "ModuleTxRegisterDTO{" +
                "moduleCode='" + moduleCode + '\'' +
                ", moduleValidator='" + moduleValidator + '\'' +
                ", moduleCommit='" + moduleCommit + '\'' +
                ", moduleRollback='" + moduleRollback + '\'' +
                ", list=" + list +
                '}';
    }
}
