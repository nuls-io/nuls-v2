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
package io.nuls.contract.model.dto;

import io.nuls.contract.vm.program.ProgramInvokeRegisterCmd;
import io.nuls.contract.vm.program.ProgramNewTx;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-05-13
 */
@ApiModel
public class ContractInvokeRegisterCmdDto {
    @ApiModelProperty(description = "命令名称")
    private String cmdName;
    @ApiModelProperty(description = "命令参数，参数不固定，依据不同的命令而来，故此处不作描述，结构为 {参数名称=参数值}")
    private Map args;
    @ApiModelProperty(description = "注册的命令模式（QUERY\\_DATA or NEW\\_TX）")
    private String cmdRegisterMode;
    @ApiModelProperty(description = "生成的交易hash（当调用的命令模式是 NEW\\_TX 时，会生成交易）")
    private String newTxHash;

    public ContractInvokeRegisterCmdDto(String cmdName, Map args, String cmdRegisterMode, String newTxHash) {
        this.cmdName = cmdName;
        this.args = args;
        this.cmdRegisterMode = cmdRegisterMode;
        this.newTxHash = newTxHash;
    }

    public ContractInvokeRegisterCmdDto() {
    }

    public ContractInvokeRegisterCmdDto(ProgramInvokeRegisterCmd invokeRegisterCmd) {
        this.cmdName = invokeRegisterCmd.getCmdName();
        this.args = invokeRegisterCmd.getArgs();
        this.cmdRegisterMode = invokeRegisterCmd.getCmdRegisterMode().toString();
        ProgramNewTx programNewTx = invokeRegisterCmd.getProgramNewTx();
        if(programNewTx != null) {
            this.newTxHash = programNewTx.getTxHash();
        }
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

    public String getCmdRegisterMode() {
        return cmdRegisterMode;
    }

    public void setCmdRegisterMode(String cmdRegisterMode) {
        this.cmdRegisterMode = cmdRegisterMode;
    }

    public String getNewTxHash() {
        return newTxHash;
    }

    public void setNewTxHash(String newTxHash) {
        this.newTxHash = newTxHash;
    }
}
