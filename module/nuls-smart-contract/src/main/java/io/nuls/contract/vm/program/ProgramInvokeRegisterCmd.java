/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.vm.program;

import io.nuls.contract.enums.CmdRegisterMode;

import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-04-25
 */
public class ProgramInvokeRegisterCmd {

    private String cmdName;
    private Map args;
    private CmdRegisterMode cmdRegisterMode;
    private ProgramNewTx programNewTx;

    public ProgramInvokeRegisterCmd() {
    }

    public ProgramInvokeRegisterCmd(String cmdName, Map args, CmdRegisterMode cmdRegisterMode) {
        this.cmdName = cmdName;
        this.args = args;
        this.cmdRegisterMode = cmdRegisterMode;
    }

    public String getCmdName() {
        return cmdName;
    }

    public void setCmdName(String cmdName) {
        this.cmdName = cmdName;
    }

    public Map getArgs() {
        return args;
    }

    public void setArgs(Map args) {
        this.args = args;
    }

    public CmdRegisterMode getCmdRegisterMode() {
        return cmdRegisterMode;
    }

    public void setCmdRegisterMode(CmdRegisterMode cmdRegisterMode) {
        this.cmdRegisterMode = cmdRegisterMode;
    }

    public ProgramNewTx getProgramNewTx() {
        return programNewTx;
    }

    public void setProgramNewTx(ProgramNewTx programNewTx) {
        this.programNewTx = programNewTx;
    }
}
