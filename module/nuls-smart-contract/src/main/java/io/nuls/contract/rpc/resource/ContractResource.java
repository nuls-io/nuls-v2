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
package io.nuls.contract.rpc.resource;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.core.basic.Page;
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
import io.nuls.contract.model.txdata.ContractData;
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
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.basic.VarInt;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ArraysTool;
import io.nuls.core.model.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

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

    @CmdAnnotation(cmd = CREATE, version = 1.0, description = "invoke contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    @Parameter(parameterName = "remark", parameterType = "String")
    public Response create(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            String password = (String) params.get("password");
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

            if (StringUtils.isBlank(contractCode)) {
                return failed(ContractErrorCode.NULL_PARAMETER);
            }

            byte[] contractCodeBytes = HexUtil.decode(contractCode);

            ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.contractCreateTx(chainId, sender, gasLimit, price, contractCodeBytes, convertArgs, password, remark);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = PRE_CREATE, version = 1.0, description = "pre create contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    @Parameter(parameterName = "remark", parameterType = "String")
    public Response preCreate(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            String password = (String) params.get("password");
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

            if (StringUtils.isBlank(contractCode)) {
                return failed(ContractErrorCode.NULL_PARAMETER);
            }

            byte[] contractCodeBytes = HexUtil.decode(contractCode);

            ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Result result = contractTxService.contractPreCreateTx(chainId, sender, gasLimit, price, contractCodeBytes, convertArgs, password, remark);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = IMPUTED_CREATE_GAS, version = 1.0, description = "imputed create gas")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
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

    @CmdAnnotation(cmd = VALIDATE_CREATE, version = 1.0, description = "validate create contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "value", parameterType = "BigInteger")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "methodName", parameterType = "String")
    @Parameter(parameterName = "methodDesc", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "remark", parameterType = "String")
    public Response call(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            BigInteger value = new BigInteger(params.get("value").toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;
            String password = (String) params.get("password");
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "value", parameterType = "BigInteger")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "methodName", parameterType = "String")
    @Parameter(parameterName = "methodDesc", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    public Response validateCall(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            BigInteger value = new BigInteger(params.get("value").toString());
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

    @CmdAnnotation(cmd = IMPUTED_CALL_GAS, version = 1.0, description = "imputed call gas")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "value", parameterType = "BigInteger")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "methodName", parameterType = "String")
    @Parameter(parameterName = "methodDesc", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
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
                BigInteger value = new BigInteger(params.get("value").toString());
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "remark", parameterType = "String")
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
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


    @CmdAnnotation(cmd = TRANSFER, version = 1.0, description = "transfer NULS from sender to contract address")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "toAddress", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "amount", parameterType = "BigInteger")
    @Parameter(parameterName = "remark", parameterType = "String")
    public Response transfer(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("address");
            String contractAddress = (String) params.get("toAddress");
            String password = (String) params.get("password");
            BigInteger value = new BigInteger(params.get("amount").toString());
            String remark = (String) params.get("remark");

            if (value.compareTo(BigInteger.ZERO) < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
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


    @CmdAnnotation(cmd = TRANSFER_FEE, version = 1.0, description = "transfer fee, transfer NULS from sender to contract address")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "toAddress", parameterType = "String")
    @Parameter(parameterName = "amount", parameterType = "BigInteger")
    @Parameter(parameterName = "remark", parameterType = "String")
    public Response transferFee(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("address");
            String contractAddress = (String) params.get("toAddress");
            BigInteger value = new BigInteger(params.get("amount").toString());
            String remark = (String) params.get("remark");

            if (value.compareTo(BigInteger.ZERO) < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
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
            Result result = contractTxService.callTxFee(chainId, sender, value, gasLimit, CONTRACT_MINIMUM_PRICE, contractAddress,
                    BALANCE_TRIGGER_METHOD_NAME,
                    BALANCE_TRIGGER_METHOD_DESC,
                    null, remark);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = TOKEN_TRANSFER, version = 1.0, description = "transfer NRC20-token from address to toAddress")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "toAddress", parameterType = "String")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "amount", parameterType = "BigInteger")
    @Parameter(parameterName = "remark", parameterType = "String")
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

    @CmdAnnotation(cmd = TOKEN_BALANCE, version = 1.0, description = "NRC20-token balance")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "address", parameterType = "String")
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
    @Parameter(parameterName = "methodName", parameterType = "String")
    @Parameter(parameterName = "methodDesc", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
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
            if (method == null || !method.isView()) {
                return failed(ContractErrorCode.CONTRACT_NON_VIEW_METHOD);
            }

            ProgramResult programResult = contractHelper.invokeCustomGasViewMethod(chainId, blockHeader, contractAddressBytes, methodName, methodDesc,
                    ContractUtil.twoDimensionalArray(args, method.argsType2Array()));

            Log.info("view method cost gas: " + programResult.getGasUsed());

            if (!programResult.isSuccess()) {
                Log.error(programResult.getStackTrace());
                Result result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                Result newResult = checkVmResultAndReturn(programResult.getErrorMessage(), result);
                // result没有变化
                if (newResult == result) {
                    return wrapperFailed(result);
                } else {
                    // Exceeded the maximum GAS limit for contract calls
                    return wrapperFailed(result);
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
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    public Response constructor(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String contractCode = (String) params.get("contractCode");

            if (StringUtils.isBlank(contractCode)) {
                return failed(NULL_PARAMETER);
            }
            byte[] contractCodeBytes = HexUtil.decode(contractCode);
            ContractInfoDto contractInfoDto = contractHelper.getConstructor(chainId, contractCodeBytes);
            if (contractInfoDto == null || contractInfoDto.getConstructor() == null) {
                return failed(ContractErrorCode.ILLEGAL_CONTRACT);
            }
            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(2);
            resultMap.put("constructor", contractInfoDto.getConstructor());
            resultMap.put("isNrc20", contractInfoDto.isNrc20());
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = CONTRACT_INFO, version = 1.0, description = "contract info")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "contractAddress", parameterType = "String")
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

            ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoResult.getData();
            if (contractAddressInfoPo == null) {
                return failed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
            }

            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);

            //if (contractAddressInfoPo.isLock(blockHeader.getHeight())) {
            //    return failed(ContractErrorCode.CONTRACT_LOCK);
            //}

            // 当前区块状态根
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramExecutor track = contractHelper.getProgramExecutor(chainId).begin(prevStateRoot);
            ProgramStatus status = track.status(contractAddressBytes);
            List<ProgramMethod> methods = track.method(contractAddressBytes);

            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(8);
            try {
                byte[] createTxHash = contractAddressInfoPo.getCreateTxHash();
                NulsHash create = new NulsHash(createTxHash);
                resultMap.put("createTxHash", create.toHex());
            } catch (Exception e) {
                Log.error("createTxHash parse error.", e);
            }

            resultMap.put("address", contractAddress);
            resultMap.put("creater", AddressTool.getStringAddressByBytes(contractAddressInfoPo.getSender()));
            resultMap.put("createTime", contractAddressInfoPo.getCreateTime());
            resultMap.put("blockHeight", contractAddressInfoPo.getBlockHeight());
            resultMap.put("isNrc20", contractAddressInfoPo.isNrc20());
            if (contractAddressInfoPo.isNrc20()) {
                resultMap.put("nrc20TokenName", contractAddressInfoPo.getNrc20TokenName());
                resultMap.put("nrc20TokenSymbol", contractAddressInfoPo.getNrc20TokenSymbol());
                resultMap.put("decimals", contractAddressInfoPo.getDecimals());
                resultMap.put("totalSupply", ContractUtil.bigInteger2String(contractAddressInfoPo.getTotalSupply()));
            }
            resultMap.put("status", status.name());
            resultMap.put("method", methods);
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = CONTRACT_RESULT, version = 1.0, description = "contract result")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "hash", parameterType = "String")
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
            if(!flag) {
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
            Result<ContractAddressInfoPo> contractAddressInfoResult =
                    contractHelper.getContractAddressInfo(chainId, contractExecuteResult.getContractAddress());
            ContractAddressInfoPo po = contractAddressInfoResult.getData();
            if (po != null && po.isNrc20()) {
                contractExecuteResult.setNrc20(true);
                if (contractExecuteResult.isSuccess()) {
                    contractResultDto = new ContractResultDto(chainId, contractExecuteResult, tx1);
                } else {
                    ContractData contractData = (ContractData) tx1.getTxDataObj();
                    byte[] sender = contractData.getSender();
                    byte[] infoKey = ArraysTool.concatenate(sender, txHash.getBytes(), new VarInt(0).encode());
                    Result<ContractTokenTransferInfoPo> tokenTransferResult = contractTokenTransferStorageService.getTokenTransferInfo(chainId, infoKey);
                    ContractTokenTransferInfoPo transferInfoPo = tokenTransferResult.getData();
                    contractResultDto = new ContractResultDto(chainId, contractExecuteResult, tx1, transferInfoPo);
                }
            } else {
                contractResultDto = new ContractResultDto(chainId, contractExecuteResult, tx1);
            }
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

    @CmdAnnotation(cmd = CONTRACT_TX, version = 1.0, description = "contract tx")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "hash", parameterType = "String")
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

    @CmdAnnotation(cmd = TOKEN_ASSETS_LIST, version = 1.0, description = "token assets list")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "pageNumber", parameterType = "int")
    @Parameter(parameterName = "pageSize", parameterType = "int")
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

    @CmdAnnotation(cmd = TOKEN_TRANSFER_LIST, version = 1.0, description = "token transfer list")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "pageNumber", parameterType = "int")
    @Parameter(parameterName = "pageSize", parameterType = "int")
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


    @CmdAnnotation(cmd = ACCOUNT_CONTRACTS, version = 1.0, description = "account contract list")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "pageNumber", parameterType = "int")
    @Parameter(parameterName = "pageSize", parameterType = "int")
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
                    dto.setCreate(true);
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
                    resultMap.put(contractAddress, new ContractAddressDto(po, height, true, track.status(contractAddressBytes).ordinal()));
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

    @CmdAnnotation(cmd = UPLOAD, version = 1.0, description = "upload")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "jarFileData", parameterType = "String")
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
            ContractInfoDto contractInfoDto = contractHelper.getConstructor(chainId, contractCode);
            if (contractInfoDto == null || contractInfoDto.getConstructor() == null) {
                return failed(ILLEGAL_CONTRACT);
            }
            Map<String, Object> resultMap = MapUtil.createLinkedHashMap(3);
            resultMap.put("constructor", contractInfoDto.getConstructor());
            resultMap.put("isNrc20", contractInfoDto.isNrc20());
            resultMap.put("code", HexUtil.encode(contractCode));

            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

}
