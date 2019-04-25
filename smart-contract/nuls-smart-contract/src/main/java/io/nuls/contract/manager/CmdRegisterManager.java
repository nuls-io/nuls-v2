/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.manager;

import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.enums.CmdRegisterType;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.CmdRegister;
import io.nuls.contract.util.Log;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.STRING;
import static io.nuls.contract.util.ContractUtil.getSuccess;
import static io.nuls.tools.constant.CommonCodeConstanst.DATA_NOT_FOUND;

/**
 * @author: PierreLuo
 * @date: 2019-04-24
 */
@Component
public class CmdRegisterManager {

    @Autowired
    private ChainManager chainManager;

    /**
     * 不支持不同模块注册的重名方法
     * 不支持相同模块注册的重载方法
     * 注册的命令参数类型需定义为String或Collection<String>
     *
     * @param chainId 链ID
     * @param moduleCode 模块代码
     * @param cmdName 模块提供的命令名称
     * @param cmdType
     * @param argNames 命令的参数名, 注册的命令参数类型需定义为String或Collection<String>
     * @param returnType 调用该命令后的返回值类型
     * @return 执行成功与否
     */
    public Result registerCmd(int chainId, String moduleCode, String cmdName, Integer cmdType, List<String> argNames, String returnType) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null) {
            Log.error("chain not found, chainId is {}", chainId);
            return Result.getFailed(DATA_NOT_FOUND);
        }
        if(cmdType == CmdRegisterType.NEW_TX.type() && !STRING.equals(returnType)) {
            Log.error("The type of new tx does not support non-string return values, this return type is [{}]", returnType);
            return Result.getFailed(ContractErrorCode.CMD_REGISTER_NEW_TX_RETURN_TYPE_ERROR);
        }
        Map<String, CmdRegister> cmdRegisterMap = chain.getCmdRegisterMap();
        CmdRegister cmdRegister;
        if((cmdRegister = cmdRegisterMap.get(cmdName)) != null) {
            if(!cmdRegister.getModuleCode().equals(moduleCode)) {
                // 不同模块注册了重复的cmd
                Log.error("Different modules registered duplicate cmd, cmdName is {}, registerd module is {}, registering module is {}", cmdName, cmdRegister.getModuleCode(), moduleCode);
                return Result.getFailed(ContractErrorCode.DUPLICATE_REGISTER_CMD);
            }
        }
        // 没有则注册，存在则覆盖
        cmdRegister = new CmdRegister(moduleCode, cmdName, CmdRegisterType.getType(cmdType), argNames, returnType);
        Log.debug("registered cmd info: {}", cmdRegister);
        cmdRegisterMap.put(cmdName, cmdRegister);
        return getSuccess();
    }

    /**
     * 根据命令名获取注册的完整信息
     *
     * @param chainId 链ID
     * @param cmdName 命令名称
     * @return 注册信息
     */
    public CmdRegister getCmdRegisterByCmdName(int chainId, String cmdName) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null) {
            Log.error("chain not found, chainId is {}", chainId);
            return null;
        }
        Map<String, CmdRegister> cmdRegisterMap = chain.getCmdRegisterMap();
        CmdRegister cmdRegister = cmdRegisterMap.get(cmdName);
        return cmdRegister;
    }
}
