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

package io.nuls.contract.model.bo;

import io.nuls.contract.enums.CmdRegisterType;

import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-04-24
 */
public class CmdRegister {

    /**
     * module code
     */
    private String moduleCode;
    /**
     * cmd
     */
    private String cmdName;
    /**
     * cmd
     */
    private CmdRegisterType cmdRegisterType;
    /**
     * cmd args
     */
    private List<String> argNames;
    /**
     * cmd return value's type
     */
    private String returnType;

    public CmdRegister(String moduleCode, String cmdName, CmdRegisterType cmdRegisterType, List<String> argNames, String returnType) {
        this.moduleCode = moduleCode;
        this.cmdName = cmdName;
        this.cmdRegisterType = cmdRegisterType;
        this.argNames = argNames;
        this.returnType = returnType;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getCmdName() {
        return cmdName;
    }

    public void setCmdName(String cmdName) {
        this.cmdName = cmdName;
    }

    public CmdRegisterType getCmdRegisterType() {
        return cmdRegisterType;
    }

    public void setCmdRegisterType(CmdRegisterType cmdRegisterType) {
        this.cmdRegisterType = cmdRegisterType;
    }

    public List<String> getArgNames() {
        return argNames;
    }

    public void setArgNames(List<String> argNames) {
        this.argNames = argNames;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
}
