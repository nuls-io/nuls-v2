/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
        // Requestor for initializing contract calls to external module registered commands
        this.requestAndResponseInterface = new RequestAndResponseInterface() {
            @Override
            public Response requestAndResponse(String moduleCode, String cmdName, Map argsMap) throws Exception {
                return ResponseMessageProcessor.requestAndResponse(moduleCode, cmdName, argsMap);
            }
        };
    }

    /**
     * Does not support duplicate name methods for registering different modules
     * Overloading methods that do not support the same module registration
     * Registered command parameter types, To be defined asStringorString Array
     * The return value type after calling this command, To be defined asStringorString Array
     *
     * @param moduleCmdRegisterDto Registration information
     * @return Whether the execution was successful or not
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
        Result result;
        for(CmdRegisterDto dto : cmdRegisterList) {
            result = this.registerCmd(chain, moduleCode, dto.getCmdName(), dto.getCmdRegisterMode(), dto.getArgNames(), dto.getCmdRegisterReturnType());
            if(result.isFailed()) {
                return result;
            }
        }
        return getSuccess();
    }

    /**
     *
     * @param chain      Chain Object
     * @param moduleCode Module code
     * @param cmdName    The command name provided by the module
     * @param cmdMode    Create transaction or Query data
     * @param argNames   The parameter name of the command, Registered command parameter types, To be defined asStringorString Array
     * @param returnType The return value type after calling this command, To be defined asStringorString Array
     * @return Whether the execution was successful or not
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
                // Different modules have registered duplicatecmd
                Log.error("Different modules registered duplicate cmd, cmdName is {}, registerd module is {}, registering module is {}", cmdName, cmdRegister.getModuleCode(), moduleCode);
                return Result.getFailed(ContractErrorCode.DUPLICATE_REGISTER_CMD);
            }
        }
        // If not, register; if present, overwrite
        cmdRegister = new CmdRegister(moduleCode, cmdName, CmdRegisterMode.getMode(cmdMode), argNames, cmdRegisterReturnType);
        if (Log.isDebugEnabled()) {
            Log.debug("registered cmd info: {}", cmdRegister);
        }
        cmdRegisterMap.put(cmdName, cmdRegister);
        return getSuccess();
    }

    /**
     * Obtain complete registration information based on the command name
     *
     * @param chainId chainID
     * @param cmdName Command Name
     * @return Registration information
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
     * Requestor for calling commands registered with external modules for contract calls
     *
     * @param moduleCode Module code
     * @param cmdName    Command Name
     * @param args       parameter
     * @return response
     * @throws Exception
     */
    public Response requestAndResponse(String moduleCode, String cmdName, Map args) throws Exception {
        return requestAndResponseInterface.requestAndResponse(moduleCode, cmdName, args);
    }

}
