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
import io.nuls.base.data.Address;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.enums.ContractStatus;
import io.nuls.contract.enums.TokenTypeStatus;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.manager.ChainManager;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractTokenInfo;
import io.nuls.contract.model.dto.*;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.tx.ContractBaseTransaction;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.rpc.call.TransactionCall;
import io.nuls.contract.service.ContractService;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.util.ContractLedgerUtil;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.*;
import io.nuls.core.basic.Page;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.crypto.KeccakHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

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
@NulsCoresCmd(module = ModuleE.SC)
public class ContractResource extends BaseCmd {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractTxService contractTxService;
    @Autowired
    private ContractAddressStorageService contractAddressStorageService;

    @CmdAnnotation(cmd = CREATE, version = 1.0, description = "Publish contract/create contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "password", parameterDes = "Account password"),
        @Parameter(parameterName = "alias", parameterDes = "Contract alias"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASlimit"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASunit price"),
        @Parameter(parameterName = "contractCode", parameterDes = "Smart Contract Code(BytecodeHexEncoding string)"),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true),
        @Parameter(parameterName = "remark", parameterDes = "Transaction notes", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing two properties", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "txHash", description = "Transactions for publishing contractshash"),
                    @Key(name = "contractAddress", description = "Generated contract address")
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

            if (gasLimit <= 0 || price <= 0) {
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

    @CmdAnnotation(cmd = IMPUTED_CREATE_GAS, version = 1.0, description = "Estimated release contract consumptionGAS/imputed create gas")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "contractCode", parameterDes = "Smart Contract Code(BytecodeHexEncoding string)"),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return consumedgasvalue", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "gasLimit", valueType = Long.class, description = "ConsumablegasValue, return value for execution failure1"),
            @Key(name = "errorMsg", valueType = String.class, description = "Error message for execution failure")
    }))
    public Response imputedCreateGas(Map<String, Object> params) {
        try {
            Map<String, Object> resultMap = MapUtil.createHashMap(1);
            resultMap.put("gasLimit", 1);
            String errorMsg = null;
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
                    errorMsg = result.getMsg();
                    break;
                }
                isImputed = true;
            } while (false);
            if (isImputed) {
                ProgramResult programResult = (ProgramResult) result.getData();
                long gasUsed = programResult.getGasUsed();
                // estimate1.5timesGas
                gasUsed += gasUsed >> 1;
                gasUsed = gasUsed > MAX_GASLIMIT ? MAX_GASLIMIT : gasUsed;
                resultMap.put("gasLimit", gasUsed);
            } else if (StringUtils.isNotBlank(errorMsg)) {
                resultMap.put("errorMsg", errorMsg);
            }

            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = VALIDATE_CREATE, version = 1.0, description = "Verify release contract/validate create contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASlimit"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASunit price"),
        @Parameter(parameterName = "contractCode", parameterDes = "Smart Contract Code(BytecodeHexEncoding string)"),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true)
    })
    @ResponseData(description = "No specific return value, validation successful without errors")
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
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "password", parameterDes = "Caller account password"),
        @Parameter(parameterName = "value", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "The caller transferred to the contract addressNULSAsset amount, filled in when this business is not availableBigInteger.ZERO"),
        @Parameter(parameterName = "multyAssetValues", requestType = @TypeDescriptor(value = String[][].class), parameterDes = "The amount of other assets transferred by the caller to the contract address, fill in the blank if there is no such business, rule: [[<value>,<assetChainId>,<assetId>]]"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASlimit"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASunit price"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
        @Parameter(parameterName = "methodName", parameterDes = "Contract method"),
        @Parameter(parameterName = "methodDesc", parameterDes = "Contract method description, if the method in the contract is not overloaded, this parameter can be empty", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true),
        @Parameter(parameterName = "remark", parameterDes = "Transaction notes", canNull = true),
        @Parameter(parameterName = "nulsValueToOthers", requestType = @TypeDescriptor(value = String[][].class), parameterDes = "The caller transferred to another account addressNULSAsset amount, fill in the blank without this business, rule: [[<value>,<address>]]")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "Transaction calling contracthash")
    }))
    public Response call(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            String password = (String) params.get("password");
            Object valueObj = params.get("value");
            valueObj = valueObj == null ? "0" : valueObj;
            BigInteger value = new BigInteger(valueObj.toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;

            List multyAssetValuesList = (List) params.get("multyAssetValues");
            Object[] multyAssetValues = multyAssetValuesList != null ? multyAssetValuesList.toArray() : null;
            List nulsValueToOthersList = (List) params.get("nulsValueToOthers");
            Object[] nulsValueToOthers = nulsValueToOthersList != null ? nulsValueToOthersList.toArray() : null;

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
            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            List<ProgramMultyAssetValue> multyAssetValueList = null;
            if (multyAssetValues != null) {
                Result<List<ProgramMultyAssetValue>> multyAssetValueListResult = convertMultyAssetValues(multyAssetValues);
                if (multyAssetValueListResult.isFailed()) {
                    return failed(multyAssetValueListResult.getErrorCode());
                }
                multyAssetValueList = multyAssetValueListResult.getData();
            }
            List<AccountAmountDto> nulsValueToOtherList = null;
            if (nulsValueToOthers != null) {
                Result<List<AccountAmountDto>> nulsValueToOtherListResult = convertNulsValueToOthers(nulsValueToOthers);
                if (nulsValueToOtherListResult.isFailed()) {
                    return failed(nulsValueToOtherListResult.getErrorCode());
                }
                nulsValueToOtherList = nulsValueToOtherListResult.getData();
            }

            Result result = contractTxService.contractCallTx(chainId, sender, value, gasLimit, price, contractAddress, methodName, methodDesc, convertArgs, password, remark, multyAssetValueList, nulsValueToOtherList);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    private Result<List<ProgramMultyAssetValue>> convertMultyAssetValues(Object[] multyAssetValues) {
        List<ProgramMultyAssetValue> results = null;
        String[][] convertMultyAssetValues = ContractUtil.twoDimensionalArray(multyAssetValues);
        if (convertMultyAssetValues != null && convertMultyAssetValues.length > 0) {
            results = new ArrayList<>();
            int assetChainId, assetId;
            for (String[] args : convertMultyAssetValues) {
                if (args == null || args.length != 3) {
                    return Result.getFailed(PARAMETER_ERROR);
                }
                assetChainId = Integer.parseInt(args[1]);
                assetId = Integer.parseInt(args[2]);
                if (assetChainId <= 0 || assetId <= 0) {
                    return Result.getFailed(PARAMETER_ERROR);
                }
                results.add(new ProgramMultyAssetValue(new BigInteger(args[0]), assetChainId, assetId));
            }
        }
        return Result.getSuccess(results);
    }

    private Result<List<AccountAmountDto>> convertNulsValueToOthers(Object[] nulsValueToOthers) {
        List<AccountAmountDto> results = null;
        String[][] convertNulsValueToOthers = ContractUtil.twoDimensionalArray(nulsValueToOthers);
        if (convertNulsValueToOthers != null && convertNulsValueToOthers.length > 0) {
            results = new ArrayList<>();
            for (String[] args : convertNulsValueToOthers) {
                if (args == null || args.length != 2) {
                    return Result.getFailed(PARAMETER_ERROR);
                }
                results.add(new AccountAmountDto(new BigInteger(args[0].trim()), args[1].trim()));
            }
        }
        return Result.getSuccess(results);
    }

    @CmdAnnotation(cmd = VALIDATE_CALL, version = 1.0, description = "validate call contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "value", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "The caller transferred to the contract addressNULSAsset amount, filled in when this business is not availableBigInteger.ZERO"),
        @Parameter(parameterName = "multyAssetValues", requestType = @TypeDescriptor(value = String[][].class), parameterDes = "The amount of other assets transferred by the caller to the contract address, fill in the blank if there is no such business, rule: [[<value>,<assetChainId>,<assetId>]]"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASlimit"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASunit price"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
        @Parameter(parameterName = "methodName", parameterDes = "Contract method"),
        @Parameter(parameterName = "methodDesc", parameterDes = "Contract method description, if the method in the contract is not overloaded, this parameter can be empty", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true)
    })
    @ResponseData(description = "No specific return value, validation successful without errors")
    public Response validateCall(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            Object valueObj = params.get("value");
            valueObj = valueObj == null ? "0" : valueObj;
            BigInteger value = new BigInteger(valueObj.toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            List argsList = (List) params.get("args");
            List multyAssetValuesList = (List) params.get("multyAssetValues");
            Object[] multyAssetValues = multyAssetValuesList != null ? multyAssetValuesList.toArray() : null;
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
            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            List<ProgramMultyAssetValue> multyAssetValueList = null;
            if (multyAssetValues != null) {
                Result<List<ProgramMultyAssetValue>> multyAssetValueListResult = convertMultyAssetValues(multyAssetValues);
                if (multyAssetValueListResult.isFailed()) {
                    return failed(multyAssetValueListResult.getErrorCode());
                }
                multyAssetValueList = multyAssetValueListResult.getData();
            }

            Result result = contractTxService.validateContractCallTx(chainId, senderBytes, value, gasLimit, price, contractAddressBytes, methodName, methodDesc, convertArgs, multyAssetValueList);

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
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "value", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "The caller transferred to the contract addressNULSAsset amount, filled in when this business is not availableBigInteger.ZERO"),
        @Parameter(parameterName = "multyAssetValues", requestType = @TypeDescriptor(value = String[][].class), parameterDes = "The amount of other assets transferred by the caller to the contract address, fill in the blank if there is no such business, rule: [[<value>,<assetChainId>,<assetId>]]"),
        @Parameter(parameterName = "gasLimit", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASlimit"),
        @Parameter(parameterName = "price", requestType = @TypeDescriptor(value = long.class), parameterDes = "GASunit price"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
        @Parameter(parameterName = "methodName", parameterDes = "Contract method"),
        @Parameter(parameterName = "methodDesc", parameterDes = "Contract method description, if the method in the contract is not overloaded, this parameter can be empty", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true)
    })
    @ResponseData(description = "Return contract execution results", responseType = @TypeDescriptor(value = ContractResultDto.class))
    public Response previewCall(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            Object valueObj = params.get("value");
            valueObj = valueObj == null ? "0" : valueObj;
            BigInteger value = new BigInteger(valueObj.toString());
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString());
            Long price = Long.parseLong(params.get("price").toString());
            String contractAddress = (String) params.get("contractAddress");
            String methodName = (String) params.get("methodName");
            String methodDesc = (String) params.get("methodDesc");
            List argsList = (List) params.get("args");
            Object[] args = argsList != null ? argsList.toArray() : null;
            List multyAssetValuesList = (List) params.get("multyAssetValues");
            Object[] multyAssetValues = multyAssetValuesList != null ? multyAssetValuesList.toArray() : null;

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
            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
            String[][] convertArgs = null;
            if (method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            List<ProgramMultyAssetValue> multyAssetValueList = null;
            if (multyAssetValues != null) {
                Result<List<ProgramMultyAssetValue>> multyAssetValueListResult = convertMultyAssetValues(multyAssetValues);
                if (multyAssetValueListResult.isFailed()) {
                    return failed(multyAssetValueListResult.getErrorCode());
                }
                multyAssetValueList = multyAssetValueListResult.getData();
            }

            Result<ContractResult> result = contractTxService.previewContractCallTx(chainId, senderBytes, value, gasLimit, price, contractAddressBytes, methodName, methodDesc, convertArgs, multyAssetValueList);

            if (result.isFailed()) {
                return wrapperFailed(result);
            }
            ContractResult contractResult = result.getData();
            ContractResultDto contractResultDto = new ContractResultDto(chainId, contractResult, gasLimit);
            //this.filterRealTokenTransfers(chainId, contractResultDto);
            return success(contractResultDto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = IMPUTED_CALL_GAS, version = 1.0, description = "imputed call gas")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "value", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "The caller transferred to the contract addressNULSAsset amount, filled in when this business is not availableBigInteger.ZERO"),
        @Parameter(parameterName = "multyAssetValues", requestType = @TypeDescriptor(value = String[][].class), parameterDes = "The amount of other assets transferred by the caller to the contract address, fill in the blank if there is no such business, rule: [[<value>,<assetChainId>,<assetId>]]"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
        @Parameter(parameterName = "methodName", parameterDes = "Contract method"),
        @Parameter(parameterName = "methodDesc", parameterDes = "Contract method description, if the method in the contract is not overloaded, this parameter can be empty", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return consumedgasvalue", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "gasLimit", valueType = Long.class, description = "ConsumablegasValue, return value for execution failure1"),
        @Key(name = "errorMsg", valueType = String.class, description = "Error message for execution failure")
    }))
    public Response imputedCallGas(Map<String, Object> params) {
        try {
            Map<String, Object> resultMap = MapUtil.createHashMap(1);
            resultMap.put("gasLimit", 1);
            String errorMsg = null;
            boolean isImputed = false;
            Result result = null;
            do {
                Integer chainId = (Integer) params.get("chainId");
                ChainManager.chainHandle(chainId);
                String sender = (String) params.get("sender");
                Object valueObj = params.get("value");
                valueObj = valueObj == null ? "0" : valueObj;
                BigInteger value = new BigInteger(valueObj.toString());
                String contractAddress = (String) params.get("contractAddress");
                String methodName = (String) params.get("methodName");
                String methodDesc = (String) params.get("methodDesc");
                List argsList = (List) params.get("args");
                Object[] args = argsList != null ? argsList.toArray() : null;
                List multyAssetValuesList = (List) params.get("multyAssetValues");
                Object[] multyAssetValues = multyAssetValuesList != null ? multyAssetValuesList.toArray() : null;
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
                // Current block state root
                byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
                ProgramMethod method = contractHelper.getMethodInfoByContractAddress(chainId, prevStateRoot, methodName, methodDesc, contractAddressBytes);
                String[][] convertArgs = null;
                if (method != null) {
                    convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
                }

                List<ProgramMultyAssetValue> multyAssetValueList = null;
                if (multyAssetValues != null) {
                    Result<List<ProgramMultyAssetValue>> multyAssetValueListResult = convertMultyAssetValues(multyAssetValues);
                    if (multyAssetValueListResult.isFailed()) {
                        return failed(multyAssetValueListResult.getErrorCode());
                    }
                    multyAssetValueList = multyAssetValueListResult.getData();
                }

                result = contractTxService.validateContractCallTx(chainId, senderBytes, value, MAX_GASLIMIT, CONTRACT_MINIMUM_PRICE, contractAddressBytes, methodName, methodDesc, convertArgs, multyAssetValueList);
                if (result.isFailed()) {
                    errorMsg = result.getMsg();
                    break;
                }
                isImputed = true;
            } while (false);

            if (isImputed) {
                ProgramResult programResult = (ProgramResult) result.getData();
                long gasUsed = programResult.getGasUsed();
                // estimate1.5timesGas
                gasUsed += gasUsed >> 1;
                gasUsed = gasUsed > MAX_GASLIMIT ? MAX_GASLIMIT : gasUsed;
                resultMap.put("gasLimit", gasUsed);
            } else if (StringUtils.isNotBlank(errorMsg)) {
                resultMap.put("errorMsg", errorMsg);
            }

            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = DELETE, version = 1.0, description = "delete contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "password", parameterDes = "Transaction account password"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
        @Parameter(parameterName = "remark", parameterDes = "Transaction notes", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "txHash", description = "Delete transactions for contractshash")
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
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "Transaction creator account address"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    })
    @ResponseData(description = "No specific return value, validation successful without errors")
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


    @CmdAnnotation(cmd = TRANSFER, version = 1.0, description = "Transfer from account address to contract address(Main chain assets)/transfer NULS from sender to contract address")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "address", parameterDes = "Transferor's account address"),
        @Parameter(parameterName = "toAddress", parameterDes = "Transferred contract address"),
        @Parameter(parameterName = "password", parameterDes = "Transferor account password"),
        @Parameter(parameterName = "amount", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "The amount of main chain assets transferred out"),
        @Parameter(parameterName = "remark", parameterDes = "Transaction notes", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "txHash", description = "transactionhash")
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
            gasParams.put("methodDesc", VOID_METHOD_DESC);

            Response response = this.imputedCallGas(gasParams);
            if (!response.isSuccess()) {
                return response;
            }
            Map<String, Object> responseData = (Map<String, Object>) response.getResponseData();
            Long gasLimit = Long.valueOf(responseData.get("gasLimit").toString());
            Result result = contractTxService.contractCallTx(chainId, sender, value, gasLimit, CONTRACT_MINIMUM_PRICE, contractAddress,
                    BALANCE_TRIGGER_METHOD_NAME,
                    VOID_METHOD_DESC,
                    null, password, remark, null, null);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = TOKEN_TRANSFER, version = 1.0, description = "NRC20-tokenTransfer/transfer NRC20-token from address to toAddress")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "address", parameterDes = "Transferor's account address"),
        @Parameter(parameterName = "toAddress", parameterDes = "Transfer address"),
        @Parameter(parameterName = "contractAddress", parameterDes = "tokenContract address"),
        @Parameter(parameterName = "password", parameterDes = "Transferor account password"),
        @Parameter(parameterName = "amount", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "Transferred outtokenAsset amount"),
        @Parameter(parameterName = "remark", parameterDes = "Transaction notes", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "txHash", description = "transactionhash")
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
                    ContractUtil.twoDimensionalArray(argsObj), password, remark, null, null);
            if (result.isFailed()) {
                return wrapperFailed(result);
            }

            return success(result.getData());
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = TOKEN_BALANCE, version = 1.0, description = "NRC20Token balance details/NRC20-token balance")
    @Parameters(description = "parameter", value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
        @Parameter(parameterName = "address", parameterDes = "Account address")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = ContractTokenInfoDto.class))
    public Response tokenBalance(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String contractAddress = (String) params.get("contractAddress");
            String address = (String) params.get("address");

            // Current Block
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
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
        @Parameter(parameterName = "methodName", parameterDes = "Contract method"),
        @Parameter(parameterName = "methodDesc", parameterDes = "Contract method description, if the method in the contract is not overloaded, this parameter can be empty", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true)
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "result", description = "The call result of the view method")
    }))
    public Response invokeView(Map<String, Object> params) {
        try {
            params.put("height", 0);
            return invokeViewByHeight(params);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = INVOKE_VIEW_BY_HEIGHT, version = 1.0, description = "invoke view contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "height", parameterDes = "height"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
        @Parameter(parameterName = "methodName", parameterDes = "Contract method"),
        @Parameter(parameterName = "methodDesc", parameterDes = "Contract method description, if the method in the contract is not overloaded, this parameter can be empty", canNull = true),
        @Parameter(parameterName = "args", requestType = @TypeDescriptor(value = Object[].class), parameterDes = "parameter list", canNull = true)
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "result", description = "The call result of the view method")
    }))
    public Response invokeViewByHeight(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            long height = 0;
            Object objectHeight = params.get("height");
            if (objectHeight != null) {
                height = Long.parseLong(objectHeight.toString());
            }
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
            BlockHeader blockHeader;
            if (height <= 0) {
                blockHeader = BlockCall.getLatestBlockHeader(chainId);
            } else {
                blockHeader = BlockCall.getBlockHeader(chainId, height);
            }
            // Current block state root
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

            if (Log.isDebugEnabled()) {
                Log.debug("view method cost gas: " + programResult.getGasUsed());
            }

            if (!programResult.isSuccess()) {
                Log.error("error msg: {}, statck trace: {}", programResult.getErrorMessage(), programResult.getStackTrace());
                Result result = Result.getFailed(ContractErrorCode.DATA_ERROR);
                result.setMsg(ContractUtil.simplifyErrorMsg(programResult.getErrorMessage()));
                Result newResult = checkVmResultAndReturn(programResult.getErrorMessage(), result);
                // resultNo changes
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

    @CmdAnnotation(cmd = CODE_HASH, version = 1.0, description = "get code hash of contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "result", description = "code hash(Keccak256)")
    }))
    public Response codeHash(Map<String, Object> params) {
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
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            byte[] codeHash = contractHelper.getContractCodeHash(chainId, prevStateRoot, contractAddressBytes);
            if (codeHash == null) {
                Result result = Result.getFailed(ContractErrorCode.DATA_NOT_FOUND);
                return wrapperFailed(result);
            } else {
                Map<String, String> resultMap = MapUtil.createLinkedHashMap(2);
                resultMap.put("result", HexUtil.encode(codeHash));
                return success(resultMap);
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = CONTRACT_CODE, version = 1.0, description = "get code of contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address"),
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "result", description = "code")
    }))
    public Response contractCode(Map<String, Object> params) {
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
            BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);
            byte[] code = contractHelper.getContractCode(chainId, prevStateRoot, contractAddressBytes);
            if (code == null) {
                Result result = Result.getFailed(ContractErrorCode.DATA_NOT_FOUND);
                return wrapperFailed(result);
            } else {
                Map<String, String> resultMap = MapUtil.createLinkedHashMap(2);
                resultMap.put("result", HexUtil.encode(code));
                return success(resultMap);
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = COMPUTE_ADDRESS, version = 1.0, description = "compute contract")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "sender", parameterDes = "sender"),
        @Parameter(parameterName = "codeHash", parameterDes = "codeHash"),
        @Parameter(parameterName = "salt", parameterDes = "salt")
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "contractAddress", description = "Contract address")
    }))
    public Response computeAddress(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String sender = (String) params.get("sender");
            String codeHash = (String) params.get("codeHash");
            List saltList = (List) params.get("salt");
            if (saltList == null || saltList.isEmpty()) {
                return failed(PARAMETER_ERROR);
            }
            int size = saltList.size();
            String[] salts = new String[size];
            for (int i=0;i<size;i++) {
                salts[i] = saltList.get(i).toString();
            }
            ProgramEncodePacked encodePacked = new ProgramEncodePacked((short) size, salts);
            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }
            ProgramCreateData createData = new ProgramCreateData(
                    AddressTool.getAddress(sender),
                    encodePacked.serialize(),
                    HexUtil.decode(codeHash));
            Address newAddress = new Address(chainId, BaseConstant.CONTRACT_ADDRESS_TYPE, SerializeUtils.sha256hash160(KeccakHash.keccakBytes(createData.serialize(), 256)));
            Map<String, String> resultMap = MapUtil.createLinkedHashMap(2);
            resultMap.put("contractAddress", newAddress.toString());
            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = CONSTRUCTOR, version = 1.0, description = "contract code constructor")
    @Parameters(description = "parameter", value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
        @Parameter(parameterName = "contractCode", parameterDes = "Smart Contract Code(BytecodeHexEncoding string)")
    })
    @ResponseData(name = "Return value", description = "Contract constructor details", responseType = @TypeDescriptor(value = ContractConstructorInfoDto.class))
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

    @CmdAnnotation(cmd = CONTRACT_INFO, version = 1.0, description = "Contract information details/contract info")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
        @Parameter(parameterName = "contractAddress", parameterDes = "Contract address")
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = ContractInfoDto.class))
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

            // Current block state root
            byte[] prevStateRoot = ContractUtil.getStateRoot(blockHeader);

            ProgramExecutor track = contractHelper.getProgramExecutor(chainId).begin(prevStateRoot);
            byte[] codeHash = track.contractCodeHash(contractAddressBytes);
            ProgramStatus status = track.status(contractAddressBytes);
            List<ProgramMethod> methods = track.method(contractAddressBytes);
            boolean isAcceptDirectTransferByOtherAsset = false;
            if(methods != null && !methods.isEmpty()) {
                int removeIndex = -1;
                boolean isQueriedPayableOtherAsset = false;
                for (int i = 0, size = methods.size(); i < size; i++) {
                    if (removeIndex > -1 && isQueriedPayableOtherAsset) break;
                    ProgramMethod m = methods.get(i);
                    if (BALANCE_TRIGGER_METHOD_NAME.equals(m.getName())
                            && BALANCE_TRIGGER_FOR_CONSENSUS_CONTRACT_METHOD_DESC.equals(m.getDesc())) {
                        removeIndex = i;
                    } else if (OTHER_ASSET_PAYABLE_METHOD_NAME.equals(m.getName())
                            && VOID_METHOD_DESC.equals(m.getDesc())) {
                        isAcceptDirectTransferByOtherAsset = m.isPayableMultyAsset();
                        isQueriedPayableOtherAsset = true;
                    }
                }
                if (removeIndex > -1) {
                    methods.remove(removeIndex);
                }

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
            boolean isNrc721 = TokenTypeStatus.NRC721.status() == po.getTokenType();
            boolean isNrc1155 = TokenTypeStatus.NRC1155.status() == po.getTokenType();
            if (po.isNrc20() || isNrc721 || isNrc1155) {
                dto.setNrc20TokenName(po.getNrc20TokenName());
                dto.setNrc20TokenSymbol(po.getNrc20TokenSymbol());
                if (po.isNrc20()) {
                    dto.setDecimals(po.getDecimals());
                    dto.setTotalSupply(ContractUtil.bigInteger2String(po.getTotalSupply()));
                } else if (isNrc1155) {
                    ProgramResult uriResult = contractHelper.invokeViewMethod(chainId, contractAddressBytes, "uri", "() return String", null);
                    String uri = uriResult.getResult();
                    dto.setTokenUri(uri);
                }
            }
            dto.setStatus(status.name());
            dto.setMethod(methods);
            dto.setDirectPayable(po.isAcceptDirectTransfer());
            dto.setDirectPayableByOtherAsset(isAcceptDirectTransferByOtherAsset);
            dto.setCodeHash(HexUtil.encode(codeHash));
            return success(dto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }


    @CmdAnnotation(cmd = CONTRACT_RESULT_LIST, version = 1.0, description = "contract result list")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "hashList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "transactionhashlist")
    })
    @ResponseData(name = "Return value", description = "Return the list of contract execution results for the transaction", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "hash1 or hash2 or hash3...", valueType = ContractResultDto.class, description = "TradinghashIn the listhashValue askeyHerekey nameIt is dynamic")
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
                ContractBaseTransaction tx1 = ContractUtil.convertContractTx(chainId, tx);
                if (tx1 == null) {
                    continue;
                }
                contractResultDto = this.makeContractResultDto(chainId, tx1, txHash);
                if (contractResultDto == null) {
                    continue;
                }
                //this.filterRealTokenTransfers(chainId, contractResultDto);
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
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "hash", parameterDes = "transactionhash")
    })
    @ResponseData(description = "Return contract execution results", responseType = @TypeDescriptor(value = ContractResultDto.class))
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
                ContractBaseTransaction tx1 = ContractUtil.convertContractTx(chainId, tx);
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
                //this.filterRealTokenTransfers(chainId, contractResultDto);
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

    @CmdAnnotation(cmd = CONTRACT_TX, version = 1.0, description = "Contract trading/contract tx")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "hash", parameterDes = "transactionhash")
    })
    @ResponseData(description = "Return contract transaction, Including contract execution results", responseType = @TypeDescriptor(value = ContractTransactionDto.class))
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
            ContractBaseTransaction tx1 = ContractUtil.convertContractTx(chainId, tx);
            tx1.setStatus(TxStatusEnum.CONFIRMED);
            // Obtain contract execution results
            ContractResultDto contractResultDto = this.makeContractResultDto(chainId, tx1, txHash);
            ContractTransactionDto txDto = new ContractTransactionDto(chainId, tx1);
            // Calculate the actual amount of the transaction
            calTransactionValue(txDto);
            if (contractResultDto != null) {
                //this.filterRealTokenTransfers(chainId, contractResultDto);
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

    @CmdAnnotation(cmd = ACCOUNT_CONTRACTS, version = 1.0, description = "List of contract addresses for the account/account contract list")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID"),
        @Parameter(parameterName = "address", parameterDes = "Account address"),
        @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "Page number", canNull = true),
        @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "Page size", canNull = true)
    })
    @ResponseData(name = "Return value", description = "Return aPageObject, only described herePageCollection in objects",
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
            // Unconfirmed contracts created by this account
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
                        // Contract creation failed
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

            // Obtain the contract address created by this account
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

    @CmdAnnotation(cmd = UPLOAD, version = 1.0, description = "Contract codejarPackage upload/upload")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
        @Parameter(parameterName = "jarFileData", parameterDes = "File description and file byte stream conversionBase64Encoding string（File description andBase64String separated by commas）")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
        @Key(name = "constructor", valueType = ProgramMethod.class, description = "Contract constructor details"),
        @Key(name = "isNrc20", valueType = Boolean.class, description = "Is itNRC20contract"),
        @Key(name = "code", description = "Smart Contract Code(BytecodeHexEncoding string)")
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

    @CmdAnnotation(cmd = CONTRACT_REWARD_LOG_BY_CONSENSUS, version = 1.0, description = "CONTRACT_REWARD_LOG_BY_CONSENSUS")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "address", parameterDes = "address")
    })
    @ResponseData(name = "Return value", description = "returnMap", responseType = @TypeDescriptor(value = Map.class))
    public Response getAssetsMapAboutContractRewardLogByConsensus(Map<String, Object> params) {
        try {
            Integer chainId = (Integer) params.get("chainId");
            ChainManager.chainHandle(chainId);
            String address = (String) params.get("address");
            Map<String, String> res = contractHelper.getAssetsMapAboutContractRewardLogByConsensus(chainId, AddressTool.getAddress(address));
            return success(res);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }
}
