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
package io.nuls.contract.rpc.resource;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.enums.ContractStatus;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.manager.ContractTokenBalanceManager;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractTokenInfo;
import io.nuls.contract.model.dto.*;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.rpc.call.TransactionCall;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.storage.ContractTokenTransferStorageService;
import io.nuls.contract.util.ContractLedgerUtil;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.ProgramStatus;
import io.nuls.core.basic.Page;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.contract.constant.ContractCmdConstant.*;
import static io.nuls.contract.constant.ContractConstant.*;
import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.*;
import static io.nuls.core.constant.TxType.CONTRACT_RETURN_GAS;
import static io.nuls.core.constant.TxType.CONTRACT_TRANSFER;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
@Component
public class ContractResource extends BaseCmd {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractTxService contractTxService;
    @Autowired
    private ContractTokenTransferStorageService contractTokenTransferStorageService;
    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @CmdAnnotation(cmd = CREATE, version = 1.0, description = "发布合约/create contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "password", parameterDes = "账户密码"),
        @Parameter(parameterName = "alias", parameterDes = "合约别名"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS限制"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS单价"),
        @Parameter(parameterName = "contractCode", parameterDes = "智能合约代码(字节码的Hex编码字符串)"),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "参数列表", canNull = true),
        @Parameter(parameterName = "remark", parameterDes = "交易备注", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象，包含两个属性", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", description = "发布合约的交易hash"),
                    @Key(name = "contractAddress", description = "生成的合约地址")
    }))
    public Response create(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            String password = (String) params.get("password");
            String alias = (String) params.get("alias");
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractCode = (String) params.get("contractCode");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;
            String remark = (String) params.get("remark");

            if (gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if(!FormatValidUtils.validAlias(alias)) {
                return failed(CONTRACT_ALIAS_FORMAT_ERROR);
            }

            if (StringUtils.isBlank(contractCode)) {
                return failed(ContractErrorCode.NULL_PARAMETER);
            }

            byte[] contractCodeBytes = HexUtil.decode(contractCode);

            ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.contractCreateTx(chainId, sender, alias, gasLimit, price, contractCodeBytes, convertArgs, password, remark);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = IMPUTED_CREATE_GAS, version = 1.0, description = "预估发布合约消耗的GAS/imputed create gas")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "contractCode", parameterDes = "智能合约代码(字节码的Hex编码字符串)"),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "参数列表", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回消耗的gas值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "gasLimit", valueType = Long.class, description = "消耗的gas值，执行失败返回数值1")
    }))
    public Response imputedCreateGas(Map<String, Object> params) {
        try {
            Map<String, Object> resultMap = MapUtil.createHashMap(1);
            resultMap.put("gasLimit", 1);
            boolean isImputed = false;
            Result result = null;
            do {
                Integer chainId = (Integer) params.get("chainId");
                ChainManager.chainHandle(chainId);
                String sender = (String) params.get("sender");
                String contractCode = (String) params.get("contractCode");
                List argsList = (List) params.get("args");
                Object[] args = argsList != null ? argsList.toArray() : null;
                if (!AddressTool.validAddress(chainId, sender)) {
                    break;
                }
                if (StringUtils.isBlank(contractCode)) {
                    break;
                }
                byte[] senderBytes = AddressTool.getAddress(sender);
                byte[] contractCodeBytes = HexUtil.decode(contractCode);
                ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
                String[][] convertArgs = null;
                if (method != null) {
                    convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
                }
                result = contractTxService.validateContractCreateTx(chainId, senderBytes, MAX_GASLIMIT, CONTRACT_MINIMUM_PRICE, contractCodeBytes, convertArgs);
                if (result.isFailed()) {
                    break;
                }
                isImputed = true;
            } while (false);
            if (isImputed) {
                ProgramResult programResult = (ProgramResult) result.getData();
                long gasUsed = programResult.getGasUsed();
                // 预估1.5倍Gas
                gasUsed += gasUsed >> 1;
                gasUsed = gasUsed > MAX_GASLIMIT ? MAX_GASLIMIT : gasUsed;
                resultMap.put("gasLimit", gasUsed);
            }

            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = VALIDATE_CREATE, version = 1.0, description = "验证发布合约/validate create contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS限制"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS单价"),
        @Parameter(parameterName = "contractCode", parameterDes = "智能合约代码(字节码的Hex编码字符串)"),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "参数列表", canNull = true)
    })
    @ResponseData(description = "无特定返回值，没有错误即验证成功")
    public Response validateCreate(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractCode = (String) params.get("contractCode");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;

            if (gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if (StringUtils.isBlank(contractCode)) {
                return failed(ContractErrorCode.NULL_PARAMETER);
            }

            byte[] contractCodeBytes = HexUtil.decode(contractCode);

            ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.validateContractCreateTx(chainId, AddressTool.getAddress(sender), gasLimit, price, contractCodeBytes, convertArgs);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = CALL, version = 1.0, description = "call contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "password", parameterDes = "调用者账户密码"),
        @Parameter(parameterName = "value", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS限制"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS单价"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址"),
        @Parameter(parameterName = "methodName", parameterDes = "合约方法"),
        @Parameter(parameterName = "methodDesc", parameterDes = "合约方法描述，若合约内方法没有重载，则此参数可以为空", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "参数列表", canNull = true),
        @Parameter(parameterName = "remark", parameterDes = "交易备注", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "调用合约的交易hash")
    }))
    public Response call(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            String password = (String) params.get("password");
            Object valueObj = params.get("value");
            if(valueObj == null) {
                valueObj = "0";
            }
            BigInteger value = new BigInteger(valueObj.toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;
            String remark = (String) params.get("remark");

            if (value.compareTo(BigInteger.ZERO) < 0 || gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            if (StringUtils.isBlank(methodName)) {
                return failed(NULL_PARAMETER);
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.contractCallTx(chainId, sender, value, gasLimit, price, contractAddress, methodName, methodDesc, convertArgs, password, remark);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = VALIDATE_CALL, version = 1.0, description = "validate call contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "value", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS限制"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS单价"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址"),
        @Parameter(parameterName = "methodName", parameterDes = "合约方法"),
        @Parameter(parameterName = "methodDesc", parameterDes = "合约方法描述，若合约内方法没有重载，则此参数可以为空", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "参数列表", canNull = true)
    })
    @ResponseData(description = "无特定返回值，没有错误即验证成功")
    public Response validateCall(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            Object valueObj = params.get("value");
            if(valueObj == null) {
                valueObj = "0";
            }
            BigInteger value = new BigInteger(valueObj.toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;

            if (value.compareTo(BigInteger.ZERO) < 0 || gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            if (StringUtils.isBlank(methodName)) {
                return failed(NULL_PARAMETER);
            }

            byte[] senderBytes = AddressTool.getAddress(sender);
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.validateContractCallTx(chainId, senderBytes, value, gasLimit, price, contractAddressBytes, methodName, methodDesc, convertArgs);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = PREVIEW_CALL, version = 1.0, description = "preview call contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "value", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS限制"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GAS单价"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址"),
        @Parameter(parameterName = "methodName", parameterDes = "合约方法"),
        @Parameter(parameterName = "methodDesc", parameterDes = "合约方法描述，若合约内方法没有重载，则此参数可以为空", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "参数列表", canNull = true)
    })
    @ResponseData(description = "返回合约执行结果", responseType = @TypeDescriptor(value = ContractResultDto.class))
    public Response previewCall(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            Object valueObj = params.get("value");
            if(valueObj == null) {
                valueObj = "0";
            }
            BigInteger value = new BigInteger(valueObj.toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;

            if (value.compareTo(BigInteger.ZERO) < 0 || gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            if (StringUtils.isBlank(methodName)) {
                return failed(NULL_PARAMETER);
            }

            byte[] senderBytes = AddressTool.getAddress(sender);
            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result<ContractResult> result = contractTxService.previewContractCallTx(chainId, senderBytes, value, gasLimit, price, contractAddressBytes, methodName, methodDesc, convertArgs);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            ContractResult contractResult = result.getData();
            ContractResultDto contractResultDto = new ContractResultDto(chainId, contractResult, gasLimit);
            List<ContractTokenTransferDto> tokenTransfers = contractResultDto.getTokenTransfers();
            List<ContractTokenTransferDto> realTokenTransfers = this.filterRealTokenTransfers(chainId, tokenTransfers);
            contractResultDto.setTokenTransfers(realTokenTransfers);
            return success(contractResultDto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = IMPUTED_CALL_GAS, version = 1.0, description = "imputed call gas")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "value", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "调用者向合约地址转入的主网资产金额，没有此业务时填BigInteger.ZERO"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址"),
        @Parameter(parameterName = "methodName", parameterDes = "合约方法"),
        @Parameter(parameterName = "methodDesc", parameterDes = "合约方法描述，若合约内方法没有重载，则此参数可以为空", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "参数列表", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回消耗的gas值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "gasLimit", valueType = Long.class, description = "消耗的gas值，执行失败返回数值1")
    }))
    public Response imputedCallGas(Map<String, Object> params) {
        try {
            Map<String, Object> resultMap = MapUtil.createHashMap(1);
            resultMap.put("gasLimit", 1);
            boolean isImputed = false;
            Result result = null;
            do {
                Integer chainId = (Integer) params.get("chainId");
                ChainManager.chainHandle(chainId);
                String sender = (String) params.get("sender");
                Object valueObj = params.get("value");
                if(valueObj == null) {
                    valueObj = "0";
                }
                BigInteger value = new BigInteger(valueObj.toString());
                String contractAddress = (String) params.get("contractAddress");
                String methodName = (String) params.get("methodName");
                String methodDesc = (String) params.get("methodDesc");
                List argsList = (List) params.get("args");
                Object[] args = argsList != null ? argsList.toArray() : null;
                if (value.compareTo(BigInteger.ZERO) < 0) {
                    break;
                }
                if (!AddressTool.validAddress(chainId, sender)) {
                    break;
                }
                if (!AddressTool.validAddress(chainId, contractAddress)) {
                    break;
                }
                if (StringUtils.isBlank(methodName)) {
                    break;
                }
                byte[] senderBytes = AddressTool.getAddress(sender);
                byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
                if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                    break;
                }
                BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
                // 当前区块状态根
                byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
                ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
                String[][] convertArgs = null;
                if (method != null) {
                    convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
                }
                result = contractTxService.validateContractCallTx(chainId, senderBytes, value, MAX_GASLIMIT, CONTRACT_MINIMUM_PRICE, contractAddressBytes, methodName, methodDesc, convertArgs);
                if (result.isFailed()) {
                    break;
                }
                isImputed = true;
            } while (false);

            if (isImputed) {
                ProgramResult programResult = (ProgramResult) result.getData();
                long gasUsed = programResult.getGasUsed();
                // 预估1.5倍Gas
                gasUsed += gasUsed >> 1;
                gasUsed = gasUsed > MAX_GASLIMIT ? MAX_GASLIMIT : gasUsed;
                resultMap.put("gasLimit", gasUsed);
            }

            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = DELETE, version = 1.0, description = "delete contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "password", parameterDes = "交易账户密码"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址"),
        @Parameter(parameterName = "remark", parameterDes = "交易备注", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "txHash", description = "删除合约的交易hash")
    }))
    public Response delete(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            String contractAddress = (String) params.get("contractAddress");
            String password = (String) params.get("password");
            String remark = (String) params.get("remark");
            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }
            Result result = contractTxService.contractDeleteTx(chainId, sender, contractAddress, password, remark);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = VALIDATE_DELETE, version = 1.0, description = "validate delete contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "sender", parameterDes = "交易创建者账户地址"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址")
    })
    @ResponseData(description = "无特定返回值，没有错误即验证成功")
    public Response validateDelete(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            String contractAddress = (String) params.get("contractAddress");
            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }
            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }
            Result result = contractTxService.validateContractDeleteTx(chainId, sender, contractAddress);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = TRANSFER, version = 1.0, description = "从账户地址向合约地址转账(主链资产)/transfer NULS from sender to contract address")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "address", parameterDes = "转出者账户地址"),
        @Parameter(parameterName = "toAddress", parameterDes = "转入的合约地址"),
        @Parameter(parameterName = "password", parameterDes = "转出者账户密码"),
        @Parameter(parameterName = "amount", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "转出的主链资产金额"),
        @Parameter(parameterName = "remark", parameterDes = "交易备注", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "txHash", description = "交易hash")
    }))
    public Response transfer(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("address");
            String contractAddress = (String) params.get("toAddress");
            String password = (String) params.get("password");
            BigInteger value = new BigInteger(params.get("amount").toString());
            String remark = (String) params.get("remark");

            if (value.compareTo(BigInteger.ZERO) <= 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR, "amount error");
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            Result<ContractAddressInfoPo> contractAddressInfoResult = contractHelper.getContractAddressInfo(chainId, contractAddressBytes);
            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if (po == null) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }
            if (!po.isAcceptDirectTransfer()) {
                return failed(CONTRACT_NO_ACCEPT_DIRECT_TRANSFER);
            }

            Map<String, Object> gasParams = new HashMap<>();
            gasParams.put(Constants.CHAIN_ID, chainId);
            gasParams.put("sender", sender);
            gasParams.put("value", value);
            gasParams.put("contractAddress", contractAddress);
            gasParams.put("methodName", BALANCE_TRIGGER_METHOD_NAME);
            gasParams.put("methodDesc", BALANCE_TRIGGER_METHOD_DESC);

            Response response = this.imputedCallGas(gasParams);
            if (!response.isSuccess()) {
                return response;
            }
            Map<String, Object> responseData = (Map<String, Object>) response.getResponseData();
            Long gasLimit = Long.valueOf(responseData.get("gasLimit").toString());
            Result result = contractTxService.contractCallTx(chainId, sender, value, gasLimit, CONTRACT_MINIMUM_PRICE, contractAddress,
                    BALANCE_TRIGGER_METHOD_NAME,
                    BALANCE_TRIGGER_METHOD_DESC,
                    null, password, remark);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = TOKEN_TRANSFER, version = 1.0, description = "NRC20-token转账/transfer NRC20-token from address to toAddress")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "address", parameterDes = "转出者账户地址"),
        @Parameter(parameterName = "toAddress", parameterDes = "转入地址"),
        @Parameter(parameterName = "contractAddress", parameterDes = "token合约地址"),
        @Parameter(parameterName = "password", parameterDes = "转出者账户密码"),
        @Parameter(parameterName = "amount", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "转出的token资产金额"),
        @Parameter(parameterName = "remark", parameterDes = "交易备注", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "txHash", description = "交易hash")
    }))
    public Response tokenTransfer(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String from = (String) params.get("address");
            String to = (String) params.get("toAddress");
            String contractAddress = (String) params.get("contractAddress");
            String password = (String) params.get("password");
            BigInteger value = new BigInteger(params.get("amount").toString());
            String remark = (String) params.get("remark");

            if (value.compareTo(BigInteger.ZERO) < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, from)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, to)) {
                return failed(ADDRESS_ERROR);
            }

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            Result<ContractAddressInfoPo> contractAddressInfoResult = contractHelper.getContractAddressInfo(chainId, contractAddressBytes);
            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if (po == null) {
                return failed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }
            if (!po.isNrc20()) {
                return failed(ContractErrorCode.CONTRACT_NOT_NRC20);
            }
            Object[] argsObj = new Object[]{to, value.toString()};

            List list = new ArrayList();
            list.add(argsObj[0]);
            list.add(argsObj[1]);
            Map<String, Object> gasParams = new HashMap<>();
            gasParams.put(Constants.CHAIN_ID, chainId);
            gasParams.put("sender", from);
            gasParams.put("value", 0);
            gasParams.put("contractAddress", contractAddress);
            gasParams.put("methodName", NRC20_METHOD_TRANSFER);
            gasParams.put("args", list);
            Response response = this.imputedCallGas(gasParams);
            if (!response.isSuccess()) {
                return response;
            }
            Map<String, Object> responseData = (Map<String, Object>) response.getResponseData();
            Long gasLimit = Long.valueOf(responseData.get("gasLimit").toString());

            Result result = contractTxService.contractCallTx(chainId, from, BigInteger.ZERO, gasLimit, CONTRACT_MINIMUM_PRICE, contractAddress,
                    ContractConstant.NRC20_METHOD_TRANSFER, null,
                    ContractUtil.twoDimensionalArray(argsObj), password, remark);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = TOKEN_BALANCE, version = 1.0, description = "NRC20代币余额详情/NRC20-token balance")
    @Parameters(description = "参数", value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址"),
        @Parameter(parameterName = "address", parameterDes = "账户地址")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = ContractTokenInfoDto.class))
    public Response tokenBalance(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String contractAddress = (String) params.get("contractAddress");
            String address = (String) params.get("address");

            // 当前区块
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            Result<ContractTokenInfo> result = contractHelper.getContractToken(chainId, blockHeader, address, contractAddress);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            ContractTokenInfo data = result.getData();
            ContractTokenInfoDto dto = null;
            if (data != null) {
                dto = new ContractTokenInfoDto(data);
                dto.setStatus(data.getStatus().status());
            }

            return success(dto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = INVOKE_VIEW, version = 1.0, description = "invoke view contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址"),
        @Parameter(parameterName = "methodName", parameterDes = "合约方法"),
        @Parameter(parameterName = "methodDesc", parameterDes = "合约方法描述，若合约内方法没有重载，则此参数可以为空", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "参数列表", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "result", description = "视图方法的调用结果")
    }))
    public Response invokeView(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            if (StringUtils.isBlank(methodName)) {
                return failed(NULL_PARAMETER);
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            if (method == null) {
                return failed(ContractErrorCode.CONTRACT_METHOD_NOT_EXIST);
            }
            if (!method.isView()) {
                return failed(ContractErrorCode.CONTRACT_NON_VIEW_METHOD);
            }

            ProgramResult programResult = contractHelper.invokeCustomGasViewMethod(chainId, blockHeader, contractAddressBytes, methodName, methodDesc,
                    ContractUtil.twoDimensionalArray(args, method.argsType2Array()));

            Log.info("view method cost gas: " + programResult.getGasUsed());

            if (!programResult.isSuccess()) {
                Log.error("error msg: {}, statck trace: {}", programResult.getErrorMessage(), programResult.getStackTrace());
                Result result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                Result newResult = checkVmResultAndReturn(programResult.getErrorMessage(), result);
                // result没有变化
                if (newResult == result) {
                    return wrapperFailed(result);
                } else {
                    // Exceeded the maximum GAS limit for contract calls
                    return wrapperFailed(newResult);
                }
            } else {
                Map<String, String> resultMap = MapUtil.createLinkedHashMap(2);
                resultMap.put("result", programResult.getResult());
                return success(resultMap);
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = CONSTRUCTOR, version = 1.0, description = "contract code constructor")
    @Parameters(description = "参数", value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "contractCode", parameterDes = "智能合约代码(字节码的Hex编码字符串)")
    })
    @ResponseData(name = "返回值", description = "合约构造函数详情", responseType = @TypeDescriptor(value = ContractConstructorInfoDto.class))
    public Response constructor(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String contractCode = (String) params.get("contractCode");

            if (StringUtils.isBlank(contractCode)) {
                return failed(NULL_PARAMETER);
            }
            byte[] contractCodeBytes = HexUtil.decode(contractCode);
            ContractConstructorInfoDto contractInfoDto = contractHelper.getConstructor(chainId, contractCodeBytes);
            if (contractInfoDto == null || contractInfoDto.getConstructor() == null) {
                return failed(ContractErrorCode.ILLEGAL_CONTRACT);
            }
            return success(contractInfoDto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = CONTRACT_INFO, version = 1.0, description = "合约信息详情/contract info")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "contractAddress", parameterDes = "合约地址")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = ContractInfoDto.class))
    public Response contractInfo(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String contractAddress = (String) params.get("contractAddress");

            if (!AddressTool.validAddress(chainId, contractAddress)) {
                return failed(ADDRESS_ERROR);
            }

            byte[] contractAddressBytes = AddressTool.getAddress(contractAddress);
            if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddressBytes)) {
                return failed(CONTRACT_ADDRESS_NOT_EXIST);
            }

            Result<ContractAddressInfoPo> contractAddressInfoResult = contractHelper.getContractAddressInfo(chainId, contractAddressBytes);
            if (contractAddressInfoResult.isFailed()) {
                return wrapperFailed(contractAddressInfoResult);
            }

            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if (po == null) {
                return failed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }

            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);

            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramExecutor track = contractHelper.getProgramExecutor(chainId).begin(prevStateRoot);
            ProgramStatus status = track.status(contractAddressBytes);
            List<ProgramMethod> methods = track.method(contractAddressBytes);
            if(methods != null && !methods.isEmpty()) {
                methods = methods.stream().filter(m -> {
                    if (BALANCE_TRIGGER_METHOD_NAME.equals(m.getName())
                            && BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC.equals(m.getDesc())) {
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            ContractInfoDto dto = new ContractInfoDto();
            try {
                byte[] createTxHash = po.getCreateTxHash();
                NulsHash create = new NulsHash(createTxHash);
                dto.setCreateTxHash(create.toHex());
            } catch (Exception e) {
                Log.error("createTxHash parse error.", e);
            }

            dto.setAddress(contractAddress);
            dto.setCreater(AddressTool.getStringAddressByBytes(po.getSender()));
            dto.setAlias(po.getAlias());
            dto.setCreateTime(po.getCreateTime());
            dto.setBlockHeight(po.getBlockHeight());
            dto.setTokenType(po.getTokenType());
            dto.setNrc20(po.isNrc20());
            if (po.isNrc20()) {
                dto.setNrc20TokenName(po.getNrc20TokenName());
                dto.setNrc20TokenSymbol(po.getNrc20TokenSymbol());
                dto.setDecimals(po.getDecimals());
                dto.setTotalSupply(ContractUtil.bigInteger2String(po.getTotalSupply()));
            }
            dto.setStatus(status.name());
            dto.setMethod(methods);
            dto.setDirectPayable(po.isAcceptDirectTransfer());
            return success(dto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = CONTRACT_RESULT_LIST, version = 1.0, description = "contract result list")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "hashList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "交易hash列表")
    })
    @ResponseData(name = "返回值", description = "返回交易的合约执行结果列表", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash1 or hash2 or hash3...", valueType = ContractResultDto.class, description = "以交易hash列表中的hash值作为key，这里的key name是动态的")
    }))
    public Response contractResultList(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            List<String> hashList = (List<String>) params.get("hashList");

            if (hashList == null || hashList.isEmpty()) {
                return failed(NULL_PARAMETER);
            }

            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(hashList.size());
            ContractResultDto contractResultDto;
            for(String hash : hashList) {
                NulsHash txHash = NulsHash.fromHex(hash);
                Transaction tx = TransactionCall.getConfirmedTx(chainId, hash);
                if (tx == null) {
                    continue;
                } else if (!ContractUtil.isContractTransaction(tx)) {
                    continue;
                }
                ContractBaseTransaction tx1 = ContractUtil.convertContractTx(tx);
                contractResultDto = this.makeContractResultDto(chainId, tx1, txHash);
                if (contractResultDto == null) {
                    continue;
                }
                List<ContractTokenTransferDto> tokenTransfers = contractResultDto.getTokenTransfers();
                List<ContractTokenTransferDto> realTokenTransfers = this.filterRealTokenTransfers(chainId, tokenTransfers);
                contractResultDto.setTokenTransfers(realTokenTransfers);
                resultMap.put(hash, contractResultDto);
            }
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = CONTRACT_RESULT, version = 1.0, description = "contract result")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "hash", parameterDes = "交易hash")
    })
    @ResponseData(description = "返回合约执行结果", responseType = @TypeDescriptor(value = ContractResultDto.class))
    public Response contractResult(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String hash = (String) params.get("hash");

            if (StringUtils.isBlank(hash)) {
                return failed(NULL_PARAMETER);
            }
            if (!NulsHash.validHash(hash)) {
                return failed(PARAMETER_ERROR);
            }

            ContractResultDto contractResultDto = null;
            boolean flag = true;
            String msg = EMPTY;
            do {
                NulsHash txHash = NulsHash.fromHex(hash);
                Transaction tx = TransactionCall.getConfirmedTx(chainId, hash);
                if (tx == null) {
                    flag = false;
                    msg = TX_NOT_EXIST.getMsg();
                    break;
                } else {
                    if (!ContractUtil.isContractTransaction(tx)) {
                        flag = false;
                        msg = ContractErrorCode.NON_CONTRACTUAL_TRANSACTION.getMsg();
                        break;
                    }
                }
                ContractBaseTransaction tx1 = ContractUtil.convertContractTx(tx);
                contractResultDto = this.makeContractResultDto(chainId, tx1, txHash);
                if (contractResultDto == null) {
                    flag = false;
                    msg = DATA_NOT_FOUND.getMsg();
                    break;
                }
            } while (false);
            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(2);
            resultMap.put("flag", flag);
            if (!flag && StringUtils.isNotBlank(msg)) {
                resultMap.put("msg", msg);
            }
            if (flag && contractResultDto != null) {
                List<ContractTokenTransferDto> tokenTransfers = contractResultDto.getTokenTransfers();
                List<ContractTokenTransferDto> realTokenTransfers = this.filterRealTokenTransfers(chainId, tokenTransfers);
                contractResultDto.setTokenTransfers(realTokenTransfers);
                resultMap.put("data", contractResultDto);
            }
            if (!flag) {
                return failed(msg);
            }
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    private ContractResultDto makeContractResultDto(int chainId, ContractBaseTransaction tx1, NulsHash txHash) throws NulsException, IOException {
        ContractResultDto contractResultDto = null;
        if (tx1.getType() == CONTRACT_TRANSFER || tx1.getType() == CONTRACT_RETURN_GAS) {
            return null;
        }
        ContractResult contractExecuteResult = contractService.getContractExecuteResult(chainId, txHash);
        if (contractExecuteResult != null) {
            contractResultDto = new ContractResultDto(chainId, contractExecuteResult, tx1);
            tx1.setBlockHeight(contractExecuteResult.getBlockHeight());
        }
        return contractResultDto;
    }

    private List<ContractTokenTransferDto> filterRealTokenTransfers(int chainId, List<ContractTokenTransferDto> tokenTransfers) {
        if (tokenTransfers == null || tokenTransfers.isEmpty()) {
            return tokenTransfers;
        }
        List<ContractTokenTransferDto> resultDto = new ArrayList<>();
        Map<String, ContractAddressInfoPo> cache = MapUtil.createHashMap(tokenTransfers.size());
        for (ContractTokenTransferDto tokenTransfer : tokenTransfers) {
            try {
                if (StringUtils.isBlank(tokenTransfer.getName())) {
                    String contractAddress = tokenTransfer.getContractAddress();
                    ContractAddressInfoPo po = cache.get(contractAddress);
                    if (po == null) {
                        po = contractHelper.getContractAddressInfo(
                                chainId, AddressTool.getAddress(contractAddress)).getData();
                        cache.put(contractAddress, po);
                    }
                    if (po == null || !po.isNrc20()) {
                        continue;
                    }
                    tokenTransfer.setNrc20Info(po);
                    resultDto.add(tokenTransfer);
                }
            } catch (Exception e) {
                Log.error(e);
            }
        }
        return resultDto;
    }

    @CmdAnnotation(cmd = CONTRACT_TX, version = 1.0, description = "合约交易/contract tx")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "hash", parameterDes = "交易hash")
    })
    @ResponseData(description = "返回合约交易, 包含合约执行结果", responseType = @TypeDescriptor(value = ContractTransactionDto.class))
    public Response contractTx(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String hash = (String) params.get("hash");

            if (StringUtils.isBlank(hash)) {
                return failed(NULL_PARAMETER);
            }
            if (!NulsHash.validHash(hash)) {
                return failed(PARAMETER_ERROR);
            }

            NulsHash txHash = NulsHash.fromHex(hash);
            Transaction tx = TransactionCall.getConfirmedTx(chainId, hash);
            if (tx == null) {
                return failed(TX_NOT_EXIST);
            } else {
                if (!ContractUtil.isContractTransaction(tx)) {
                    return failed(NON_CONTRACTUAL_TRANSACTION);
                }
            }
            ContractBaseTransaction tx1 = ContractUtil.convertContractTx(tx);
            tx1.setStatus(TxStatusEnum.CONFIRMED);
            // 获取合约执行结果
            ContractResultDto contractResultDto = this.makeContractResultDto(chainId, tx1, txHash);
            ContractTransactionDto txDto = new ContractTransactionDto(chainId, tx1);
            // 计算交易实际发生的金额
            calTransactionValue(txDto);
            if (contractResultDto != null) {
                List<ContractTokenTransferDto> tokenTransfers = contractResultDto.getTokenTransfers();
                List<ContractTokenTransferDto> realTokenTransfers = this.filterRealTokenTransfers(chainId, tokenTransfers);
                contractResultDto.setTokenTransfers(realTokenTransfers);
                txDto.setContractResult(contractResultDto);
            }

            return success(txDto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    private void calTransactionValue(ContractTransactionDto txDto) {
        if (txDto == null) {
            return;
        }
        List<InputDto> inputDtoList = txDto.getInputs();
        Set<String> inputAdressSet = new HashSet<>(inputDtoList.size());
        for (InputDto inputDto : inputDtoList) {
            inputAdressSet.add(inputDto.getAddress());
        }
        BigInteger value = BigInteger.ZERO;
        List<OutputDto> outputDtoList = txDto.getOutputs();
        for (OutputDto outputDto : outputDtoList) {
            if (inputAdressSet.contains(outputDto.getAddress())) {
                continue;
            }
            value = value.add(new BigInteger(outputDto.getAmount()));
        }
        txDto.setValue(bigInteger2String(value));
    }

    @CmdAnnotation(cmd = TOKEN_ASSETS_LIST, version = 1.0, description = "token资产集合/token assets list")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "address", parameterDes = "账户地址"),
        @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "页码", canNull = true),
        @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "每页大小", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Page对象，这里只描述Page对象中的集合",
        responseType = @TypeDescriptor(value = List.class, collectionElement = ContractTokenInfoDto.class)
    )
    public Response tokenAssetsList(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String address = (String) params.get("address");
            Integer pageNumber = (Integer) params.get("pageNumber");
            Integer pageSize = (Integer) params.get("pageSize");

            if (null == pageNumber || pageNumber == 0) {
                pageNumber = 1;
            }
            if (null == pageSize || pageSize == 0) {
                pageSize = 10;
            }
            if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
                return failed(PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, address)) {
                return failed(ADDRESS_ERROR);
            }

            ContractTokenBalanceManager tokenBalanceManager = contractHelper.getChain(chainId).getContractTokenBalanceManager();
            Result<List<ContractTokenInfo>> tokenListResult = tokenBalanceManager.getAllTokensByAccount(address);
            if (tokenListResult.isFailed()) {
                return wrapperFailed(tokenListResult);
            }

            List<ContractTokenInfo> tokenInfoList = tokenListResult.getData();

            List<ContractTokenInfoDto> tokenInfoDtoList = new ArrayList<>();
            Page<ContractTokenInfoDto> page = new Page<>(pageNumber, pageSize, tokenInfoList.size());
            int start = pageNumber * pageSize - pageSize;
            if (start >= page.getTotal()) {
                return success(page);
            }

            int end = start + pageSize;
            if (end > page.getTotal()) {
                end = (int) page.getTotal();
            }

            if (tokenInfoList.size() > 0) {
                for (int i = start; i < end; i++) {
                    ContractTokenInfo info = tokenInfoList.get(i);
                    tokenInfoDtoList.add(new ContractTokenInfoDto(info));
                }
            }
            if (tokenInfoDtoList != null && tokenInfoDtoList.size() > 0) {
                BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
                byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
                ProgramExecutor track = contractHelper.getProgramExecutor(chainId).begin(prevStateRoot);
                for (ContractTokenInfoDto tokenInfo : tokenInfoDtoList) {
                    tokenInfo.setStatus(track.status(AddressTool.getAddress(tokenInfo.getContractAddress())).ordinal());
                }
            }
            page.setList(tokenInfoDtoList);

            return success(page);
        } catch (NulsException e) {
            Log.error(e);
            return failed(e.getErrorCode());
        }
    }

    @CmdAnnotation(cmd = TOKEN_TRANSFER_LIST, version = 1.0, description = "token转账交易列表/token transfer list")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "address", parameterDes = "账户地址"),
        @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "页码", canNull = true),
        @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "每页大小", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Page对象，这里只描述Page对象中的集合",
        responseType = @TypeDescriptor(value = List.class, collectionElement = ContractTokenTransferTransactionDto.class)
    )
    public Response tokenTransferList(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String address = (String) params.get("address");
            Integer pageNumber = (Integer) params.get("pageNumber");
            Integer pageSize = (Integer) params.get("pageSize");

            if (null == pageNumber || pageNumber == 0) {
                pageNumber = 1;
            }
            if (null == pageSize || pageSize == 0) {
                pageSize = 10;
            }
            if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
                return failed(PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, address)) {
                return failed(ADDRESS_ERROR);
            }
            byte[] addressBytes = AddressTool.getAddress(address);

            Result<List<ContractTokenTransferInfoPo>> tokenTransferInfoListResult = contractTxService.getTokenTransferInfoList(chainId, address);
            if (tokenTransferInfoListResult.isFailed()) {
                return wrapperFailed(tokenTransferInfoListResult);
            }

            List<ContractTokenTransferInfoPo> list = tokenTransferInfoListResult.getData();
            if (list == null) {
                list = new ArrayList<>();
            }
            if (list.size() > 0) {
                list.sort(new Comparator<ContractTokenTransferInfoPo>() {
                    @Override
                    public int compare(ContractTokenTransferInfoPo o1, ContractTokenTransferInfoPo o2) {
                        return o1.compareTo(o2.getTime());
                    }
                });
            }

            List<ContractTokenTransferTransactionDto> result = new ArrayList<>();
            Page<ContractTokenTransferTransactionDto> page = new Page<>(pageNumber, pageSize, list.size());
            int start = pageNumber * pageSize - pageSize;
            if (start >= page.getTotal()) {
                return success(page);
            }

            int end = start + pageSize;
            if (end > page.getTotal()) {
                end = (int) page.getTotal();
            }

            ContractTokenTransferInfoPo info;
            for (int i = start; i < end; i++) {
                info = list.get(i);
                result.add(new ContractTokenTransferTransactionDto(info, addressBytes));
            }

            page.setList(result);

            return success(page);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = ACCOUNT_CONTRACTS, version = 1.0, description = "账户的合约地址列表/account contract list")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
        @Parameter(parameterName = "address", parameterDes = "账户地址"),
        @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "页码", canNull = true),
        @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "每页大小", canNull = true)
    })
    @ResponseData(name = "返回值", description = "返回一个Page对象，这里只描述Page对象中的集合",
        responseType = @TypeDescriptor(value = List.class, collectionElement = ContractAddressDto.class)
    )
    public Response accountContracts(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String address = (String) params.get("address");
            Integer pageNumber = (Integer) params.get("pageNumber");
            Integer pageSize = (Integer) params.get("pageSize");

            if (null == pageNumber || pageNumber == 0) {
                pageNumber = 1;
            }
            if (null == pageSize || pageSize == 0) {
                pageSize = 10;
            }
            if (pageNumber < 0 || pageSize < 0 || pageSize > 100) {
                return failed(PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, address)) {
                return failed(ADDRESS_ERROR);
            }

            byte[] addressBytes = AddressTool.getAddress(address);


            LinkedHashMap<String, ContractAddressDto> resultMap = new LinkedHashMap<>();
            // 该账户创建的未确认的合约
            LinkedList<Map<String, String>> list = contractHelper.getChain(chainId).getContractTxCreateUnconfirmedManager().getLocalUnconfirmedCreateContractTransaction(address);
            if (list != null) {
                String contractAddress;
                Long time;
                ContractAddressDto dto;
                String success;
                for (Map<String, String> map : list) {
                    contractAddress = map.get("contractAddress");
                    time = Long.valueOf(map.get("time"));
                    dto = new ContractAddressDto();
                    dto.setContractAddress(contractAddress);
                    dto.setCreateTime(time);

                    success = map.get("success");
                    if (StringUtils.isNotBlank(success)) {
                        // 合约创建失败
                        dto.setStatus(ContractStatus.CREATION_FAILED.status());
                        dto.setMsg(map.get("msg"));
                    } else {
                        dto.setStatus(ContractStatus.NOT_EXISTS_OR_CONFIRMING.status());
                    }
                    resultMap.put(contractAddress, dto);
                }
            }
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            long height = blockHeader.getHeight();
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            ProgramExecutor track = contractHelper.getChain(chainId).getProgramExecutor().begin(prevStateRoot);
            byte[] contractAddressBytes;
            String contractAddress;

            // 获取该账户创建的合约地址
            Result<List<ContractAddressInfoPo>> contractInfoListResult = contractAddressStorageService.getContractInfoList(chainId, addressBytes);

            List<ContractAddressInfoPo> contractAddressInfoPoList = contractInfoListResult.getData();
            if (contractAddressInfoPoList != null && contractAddressInfoPoList.size() > 0) {
                contractAddressInfoPoList.sort(new Comparator<ContractAddressInfoPo>() {
                    @Override
                    public int compare(ContractAddressInfoPo o1, ContractAddressInfoPo o2) {
                        return o1.compareTo(o2.getCreateTime());
                    }
                });
                for (ContractAddressInfoPo po : contractAddressInfoPoList) {
                    contractAddressBytes = po.getContractAddress();
                    contractAddress = AddressTool.getStringAddressByBytes(contractAddressBytes);
                    resultMap.put(contractAddress, new ContractAddressDto(po, height, track.status(contractAddressBytes).ordinal()));
                }
            }
            List<ContractAddressDto> infoList = new ArrayList<>(resultMap.values());
            List<ContractAddressDto> contractAddressDtoList = new ArrayList<>();
            Page<ContractAddressDto> page = new Page<>(pageNumber, pageSize, infoList.size());
            int start = pageNumber * pageSize - pageSize;
            if (start >= page.getTotal()) {
                return success(page);
            }
            int end = start + pageSize;
            if (end > page.getTotal()) {
                end = (int) page.getTotal();
            }
            if (infoList.size() > 0) {
                for (int i = start; i < end; i++) {
                    contractAddressDtoList.add(infoList.get(i));
                }
            }
            page.setList(contractAddressDtoList);

            return success(page);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = UPLOAD, version = 1.0, description = "合约代码jar包上传/upload")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "jarFileData", parameterDes = "文件描述和文件字节流转换Base64编码字符串（文件描述和Base64字符串以逗号隔开）")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "constructor", valueType = ProgramMethod.class, description = "合约构造函数详情"),
        @Key(name = "isNrc20", valueType = Boolean.class, description = "是否是NRC20合约"),
        @Key(name = "code", description = "智能合约代码(字节码的Hex编码字符串)")
    }))
    public Response upload(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String jarFileData = (String) params.get("jarFileData");
            if (StringUtils.isBlank(jarFileData)) {
                return failed(NULL_PARAMETER);
            }
            String[] arr = jarFileData.split(",");
            if (arr.length != 2) {
                return failed(PARAMETER_ERROR);
            }

            String body = arr[1];
            byte[] contractCode = Base64.getDecoder().decode(body);
            ContractConstructorInfoDto contractInfoDto = contractHelper.getConstructor(chainId, contractCode);
            if (contractInfoDto == null || contractInfoDto.getConstructor() == null) {
                return failed(ILLEGAL_CONTRACT);
            }
            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(3);
            resultMap.put("constructor", contractInfoDto.getConstructor());
            resultMap.put("isNrc20", contractInfoDto.isNrc20());
            resultMap.put("code", HexUtil.encode(contractCode));

            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

}
