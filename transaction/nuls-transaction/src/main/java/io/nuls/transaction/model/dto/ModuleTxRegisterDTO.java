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

package io.nuls.transaction.model.dto;

import java.util.List;

/**
 * 各个模块需要向交易管理模块注册自己的每个交易的验证器，整个模块的验证器
 * @author: qinyifeng
 * @date: 2018/11/30
 */
public class ModuleTxRegisterDTO {

    private int chainId;
    /**
     * 模块编码
     */
    private String moduleCode;
    /**
     * 模块统一验证器
     */
    private String moduleValidator;
    /**
     * 交易验证器
     */
    private List<TxRegisterDTO> list;

    public ModuleTxRegisterDTO() {

    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
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

    public List<TxRegisterDTO> getList() {
        return list;
    }

    public void setList(List<TxRegisterDTO> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "ModuleTxRegisterDTO{" +
                "moduleCode='" + moduleCode + '\'' +
                ", moduleValidator='" + moduleValidator + '\'' +
                ", list=" + list +
                '}';
    }
}
