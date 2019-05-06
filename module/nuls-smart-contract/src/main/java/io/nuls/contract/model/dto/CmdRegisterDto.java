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

package io.nuls.contract.model.dto;

import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.enums.CmdRegisterReturnType;

import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-05-05
 */
public class CmdRegisterDto {

    /**
     * cmd
     */
    private String cmdName;
    /**
     * cmd mode
     * @see io.nuls.contract.enums.CmdRegisterMode
     */
    private int cmdRegisterMode;
    /**
     * cmd args
     */
    private List<String> argNames;
    /**
     * cmd return value's type
     * @see io.nuls.contract.enums.CmdRegisterReturnType
     */
    private int cmdRegisterReturnType;

    public CmdRegisterDto() {
    }

    public CmdRegisterDto(String cmdName, int cmdRegisterMode, List<String> argNames, int cmdRegisterReturnType) {
        this.cmdName = cmdName;
        this.cmdRegisterMode = cmdRegisterMode;
        this.argNames = argNames;
        this.cmdRegisterReturnType = cmdRegisterReturnType;
    }

    public String getCmdName() {
        return cmdName;
    }

    public void setCmdName(String cmdName) {
        this.cmdName = cmdName;
    }

    public int getCmdRegisterMode() {
        return cmdRegisterMode;
    }

    public void setCmdRegisterMode(int cmdRegisterMode) {
        this.cmdRegisterMode = cmdRegisterMode;
    }

    public List<String> getArgNames() {
        return argNames;
    }

    public void setArgNames(List<String> argNames) {
        this.argNames = argNames;
    }

    public int getCmdRegisterReturnType() {
        return cmdRegisterReturnType;
    }

    public void setCmdRegisterReturnType(int cmdRegisterReturnType) {
        this.cmdRegisterReturnType = cmdRegisterReturnType;
    }
}
