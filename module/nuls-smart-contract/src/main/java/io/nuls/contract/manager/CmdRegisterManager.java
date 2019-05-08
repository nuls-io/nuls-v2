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
import io.nuls.contract.enums.CmdRegisterMode;
import io.nuls.contract.enums.CmdRegisterReturnType;
import io.nuls.contract.manager.interfaces.RequestAndResponseInterface;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.CmdRegister;
import io.nuls.contract.model.dto.CmdRegisterDto;
import io.nuls.contract.model.dto.ModuleCmdRegisterDto;
import io.nuls.contract.util.Log;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.util.List;
import java.util.Map;

import static io.nuls.contract.util.ContractUtil.getSuccess;
import static io.nuls.core.constant.CommonCodeConstanst.DATA_NOT_FOUND;

/**
 * @author: PierreLuo
 * @date: 2019-04-24
 */
@Component
public class CmdRegisterManager implements InitializingBean {

    @Autowired
    private ChainManager chainManager;

    private RequestAndResponseInterface requestAndResponseInterface;

    @Override
    public void afterPropertiesSet() throws NulsException {
        // 初始化合约调用外部模块注册的命令的请求器
        this.requestAndResponseInterface = new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map argsMap) throws Exception {
                return ResponseMessageProcessor.requestAndResponse(moduleCode, cmdName, argsMap);
            }
        };
    }

    /**
     * 不支持不同模块注册的重名方法
     * 不支持相同模块注册的重载方法
     * 注册的命令参数类型, 需定义为String或String Array
     * 调用该命令后的返回值类型, 需定义为String或String Array
     *
     * @param moduleCmdRegisterDto 注册信息
     * @return 执行成功与否
     */
    public Result registerCmd(ModuleCmdRegisterDto moduleCmdRegisterDto) {
        int chainId = moduleCmdRegisterDto.getChainId();
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            Log.error("chain not found, chainId is {}", chainId);
            return Result.getFailed(DATA_NOT_FOUND);
        }
        String moduleCode = moduleCmdRegisterDto.getModuleCode();
        List<CmdRegisterDto> cmdRegisterList = moduleCmdRegisterDto.getCmdRegisterList();
        Result result = null;
        for(CmdRegisterDto dto : cmdRegisterList) {
            result = this.registerCmd(chain, moduleCode, dto.getCmdName(), dto.getCmdRegisterMode(), dto.getArgNames(), dto.getCmdRegisterReturnType());
            if(result.isFailed()) {
                //TODO pierre 清除当前注册的cmd信息
                return result;
            }
        }
        return getSuccess();
    }

    /**
     *
     * @param chain      链对象
     * @param moduleCode 模块代码
     * @param cmdName    模块提供的命令名称
     * @param cmdMode    创建交易 or 查询数据
     * @param argNames   命令的参数名, 注册的命令参数类型, 需定义为String或String Array
     * @param returnType 调用该命令后的返回值类型, 需定义为String或String Array
     * @return 执行成功与否
     * @see CmdRegisterMode
     * @see CmdRegisterReturnType
     */
    private Result registerCmd(Chain chain, String moduleCode, String cmdName, Integer cmdMode, List<String> argNames, Integer returnType) {
        CmdRegisterReturnType cmdRegisterReturnType = CmdRegisterReturnType.getType(returnType);
        if (cmdMode == CmdRegisterMode.NEW_TX.mode() && !CmdRegisterReturnType.STRING_ARRAY.equals(cmdRegisterReturnType)) {
            Log.error("The type of NEW_TX does not support non-string array return values, this return type is [{}]", cmdRegisterReturnType);
            return Result.getFailed(ContractErrorCode.CMD_REGISTER_NEW_TX_RETURN_TYPE_ERROR);
        }
        Map<String, CmdRegister> cmdRegisterMap = chain.getCmdRegisterMap();
        CmdRegister cmdRegister;
        if ((cmdRegister = cmdRegisterMap.get(cmdName)) != null) {
            if (!cmdRegister.getModuleCode().equals(moduleCode)) {
                // 不同模块注册了重复的cmd
                Log.error("Different modules registered duplicate cmd, cmdName is {}, registerd module is {}, registering module is {}", cmdName, cmdRegister.getModuleCode(), moduleCode);
                return Result.getFailed(ContractErrorCode.DUPLICATE_REGISTER_CMD);
            }
        }
        // 没有则注册，存在则覆盖
        cmdRegister = new CmdRegister(moduleCode, cmdName, CmdRegisterMode.getMode(cmdMode), argNames, cmdRegisterReturnType);
        if (Log.isDebugEnabled()) {
            Log.debug("registered cmd info: {}", cmdRegister);
        }
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
        if (chain == null) {
            Log.error("chain not found, chainId is {}", chainId);
            return null;
        }
        Map<String, CmdRegister> cmdRegisterMap = chain.getCmdRegisterMap();
        CmdRegister cmdRegister = cmdRegisterMap.get(cmdName);
        return cmdRegister;
    }

    /**
     * 合约调用外部模块注册的命令的请求器
     *
     * @param moduleCode 模块代码
     * @param cmdName    命令名称
     * @param args       参数
     * @return response
     * @throws Exception
     */
    public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
        return requestAndResponseInterface.requestAndResponse(moduleCode, cmdName, args);
    }

}
