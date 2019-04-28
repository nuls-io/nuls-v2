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

import io.nuls.contract.enums.CmdRegisterReturnType;
import io.nuls.contract.enums.CmdRegisterMode;

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
     * cmd mode
     */
    private CmdRegisterMode cmdRegisterMode;
    /**
     * cmd args
     */
    private List<String> argNames;
    /**
     * cmd return value's type
     */
    private CmdRegisterReturnType cmdRegisterReturnType;

    public CmdRegister(String moduleCode, String cmdName, CmdRegisterMode cmdRegisterMode, List<String> argNames, CmdRegisterReturnType cmdRegisterReturnType) {
        this.moduleCode = moduleCode;
        this.cmdName = cmdName;
        this.cmdRegisterMode = cmdRegisterMode;
        this.argNames = argNames;
        this.cmdRegisterReturnType = cmdRegisterReturnType;
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

    public CmdRegisterMode getCmdRegisterMode() {
        return cmdRegisterMode;
    }

    public void setCmdRegisterMode(CmdRegisterMode cmdRegisterMode) {
        this.cmdRegisterMode = cmdRegisterMode;
    }

    public List<String> getArgNames() {
        return argNames;
    }

    public void setArgNames(List<String> argNames) {
        this.argNames = argNames;
    }

    public CmdRegisterReturnType getCmdRegisterReturnType() {
        return cmdRegisterReturnType;
    }

    public void setCmdRegisterReturnType(CmdRegisterReturnType cmdRegisterReturnType) {
        this.cmdRegisterReturnType = cmdRegisterReturnType;
    }
}
